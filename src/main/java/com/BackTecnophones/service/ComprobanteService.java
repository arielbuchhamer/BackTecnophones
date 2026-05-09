package com.BackTecnophones.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BackTecnophones.client.AfRelayClient;
import com.BackTecnophones.exception.AfRelayException;
import com.BackTecnophones.exception.ComprobanteException;
import com.BackTecnophones.model.facturacion.ComprobanteEmitido;
import com.BackTecnophones.model.facturacion.ComprobanteSolicitud;
import com.BackTecnophones.model.facturacion.CondicionIva;
import com.BackTecnophones.model.facturacion.DatosFiscales;
import com.BackTecnophones.model.facturacion.EstadoComprobante;
import com.BackTecnophones.model.facturacion.TipoDocumento;
import com.BackTecnophones.repository.ComprobanteEmitidoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class ComprobanteService {
	private static final DateTimeFormatter ARCA_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	private final ComprobanteEmitidoRepository comprobanteRepository;
	private final ComprobanteFiscalValidator validator;
	private final NumeracionComprobanteService numeracionService;
	private final AfRelayClient afRelayClient;
	private final ObjectMapper objectMapper;

	public ComprobanteService(
			ComprobanteEmitidoRepository comprobanteRepository,
			ComprobanteFiscalValidator validator,
			NumeracionComprobanteService numeracionService,
			AfRelayClient afRelayClient,
			ObjectMapper objectMapper) {
		this.comprobanteRepository = comprobanteRepository;
		this.validator = validator;
		this.numeracionService = numeracionService;
		this.afRelayClient = afRelayClient;
		this.objectMapper = objectMapper;
	}

	public ComprobanteEmitido generarComprobante(ComprobanteSolicitud solicitud) {
		validator.validar(solicitud);

		Optional<ComprobanteEmitido> existente = comprobanteRepository.findByIdempotencyKey(solicitud.getIdempotencyKey());
		if (existente.isPresent() && existente.get().getEstado() != EstadoComprobante.ERROR) {
			return existente.get();
		}

		ComprobanteEmitido comprobante = existente.orElseGet(ComprobanteEmitido::new);
		comprobante.setIdempotencyKey(solicitud.getIdempotencyKey());
		comprobante.setSolicitud(solicitud);
		comprobante.setPuntoVenta(solicitud.getPuntoVenta());
		comprobante.setTipoComprobante(solicitud.getTipoComprobante());
		comprobante.setEstado(EstadoComprobante.RECIBIDO);
		comprobante.setError(null);
		if (comprobante.getFechaCreacion() == null) {
			comprobante.setFechaCreacion(Instant.now());
		}
		comprobante = comprobanteRepository.save(comprobante);

		try {
			Long numero = numeracionService.proximoNumero(solicitud.getEmisor().getCuit(), solicitud.getPuntoVenta(), solicitud.getTipoComprobante());
			comprobante.setNumeroComprobante(numero);
			comprobante.setEstado(EstadoComprobante.VALIDADO);

			Map<String, Object> afRelayRequest = armarSolicitudAfRelay(solicitud, numero);
			JsonNode afRelayResponse = afRelayClient.solicitarCae(afRelayRequest);

			comprobante.setAfRelayRequestJson(toJson(afRelayRequest));
			comprobante.setAfRelayResponseJson(toJson(afRelayResponse));
			comprobante.setDatosFiscales(extraerDatosFiscales(afRelayResponse));

			String resultado = comprobante.getDatosFiscales().getResultado();
			if (!"A".equalsIgnoreCase(resultado)) {
				throw new ComprobanteException("ARCA no autorizo el comprobante. Resultado=" + resultado);
			}

			comprobante.setEstado(EstadoComprobante.AUTORIZADO);
			comprobante.setFechaAutorizacion(Instant.now());
			return comprobanteRepository.save(comprobante);
		} catch (AfRelayException | ComprobanteException e) {
			comprobante.setEstado(EstadoComprobante.ERROR);
			comprobante.setError(e.getMessage());
			comprobanteRepository.save(comprobante);
			throw e;
		}
	}

	public Optional<ComprobanteEmitido> findById(String id) {
		return comprobanteRepository.findById(id);
	}

	public Optional<ComprobanteEmitido> findByIdempotencyKey(String idempotencyKey) {
		return comprobanteRepository.findByIdempotencyKey(idempotencyKey);
	}

	public List<ComprobanteEmitido> findAll() {
		return comprobanteRepository.findAll();
	}

	private Map<String, Object> armarSolicitudAfRelay(ComprobanteSolicitud solicitud, Long numeroComprobante) {
		BigDecimal total = solicitud.getImportes().getTotal();
		BigDecimal neto = orZero(solicitud.getImportes().getNeto());
		BigDecimal iva = orZero(solicitud.getImportes().getIva());
		BigDecimal tributos = orZero(solicitud.getImportes().getTributos());
		BigDecimal exento = orZero(solicitud.getImportes().getExento());
		BigDecimal noGravado = orZero(solicitud.getImportes().getNoGravado());

		Map<String, Object> detalle = new LinkedHashMap<>();
		detalle.put("Concepto", solicitud.getConcepto() == null ? 1 : solicitud.getConcepto());
		detalle.put("DocTipo", codigoDocumento(solicitud));
		detalle.put("DocNro", numeroDocumento(solicitud));
		detalle.put("CbteDesde", numeroComprobante);
		detalle.put("CbteHasta", numeroComprobante);
		detalle.put("CbteFch", ARCA_DATE_FORMATTER.format(solicitud.getFecha()));
		detalle.put("ImpTotal", total);
		detalle.put("ImpTotConc", noGravado);
		detalle.put("ImpNeto", neto);
		detalle.put("ImpOpEx", exento);
		detalle.put("ImpTrib", tributos);
		detalle.put("ImpIVA", iva);
		detalle.put("MonId", solicitud.getMoneda() == null ? "PES" : solicitud.getMoneda());
		detalle.put("MonCotiz", solicitud.getCotizacion() == null ? BigDecimal.ONE : solicitud.getCotizacion());
		detalle.put("CondicionIVAReceptorId", condicionIvaReceptorId(solicitud.getCliente().getCondicionIva()));

		Map<String, Object> feDetReq = new LinkedHashMap<>();
		feDetReq.put("FECAEDetRequest", List.of(detalle));

		Map<String, Object> feCabReq = new LinkedHashMap<>();
		feCabReq.put("CantReg", 1);
		feCabReq.put("PtoVta", solicitud.getPuntoVenta());
		feCabReq.put("CbteTipo", solicitud.getTipoComprobante().getCodigoArca());

		Map<String, Object> feCaeReq = new LinkedHashMap<>();
		feCaeReq.put("FeCabReq", feCabReq);
		feCaeReq.put("FeDetReq", feDetReq);

		Map<String, Object> request = new LinkedHashMap<>();
		request.put("Auth", Map.of("Cuit", cuitComoLong(solicitud.getEmisor().getCuit())));
		request.put("FeCAEReq", feCaeReq);
		return request;
	}

	private DatosFiscales extraerDatosFiscales(JsonNode response) {
		DatosFiscales datos = new DatosFiscales();
		datos.setCae(buscarTexto(response, "CAE", "cae"));
		datos.setCaeVencimiento(parseArcaDate(buscarTexto(response, "CAEFchVto", "caeFchVto")));
		datos.setResultado(buscarTexto(response, "Resultado", "resultado"));
		datos.setObservaciones(List.of());
		return datos;
	}

	private Integer codigoDocumento(ComprobanteSolicitud solicitud) {
		TipoDocumento tipoDocumento = solicitud.getCliente().getTipoDocumento();
		if (tipoDocumento == null) {
			return TipoDocumento.CONSUMIDOR_FINAL.getCodigoArca();
		}

		return tipoDocumento.getCodigoArca();
	}

	private Long numeroDocumento(ComprobanteSolicitud solicitud) {
		if (solicitud.getCliente().getTipoDocumento() == TipoDocumento.CONSUMIDOR_FINAL
				|| solicitud.getCliente().getNumeroDocumento() == null
				|| solicitud.getCliente().getNumeroDocumento().isBlank()) {
			return 0L;
		}

		return Long.valueOf(soloDigitos(solicitud.getCliente().getNumeroDocumento()));
	}

	private Integer condicionIvaReceptorId(CondicionIva condicionIva) {
		if (condicionIva == null) {
			return 5;
		}

		return switch (condicionIva) {
			case RESPONSABLE_INSCRIPTO -> 1;
			case MONOTRIBUTISTA -> 6;
			case CONSUMIDOR_FINAL -> 5;
			case EXENTO -> 4;
			case NO_RESPONSABLE -> 15;
		};
	}

	private Long cuitComoLong(String cuit) {
		return Long.valueOf(soloDigitos(cuit));
	}

	private String soloDigitos(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

	private BigDecimal orZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private LocalDate parseArcaDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return LocalDate.parse(value, ARCA_DATE_FORMATTER);
	}

	private String buscarTexto(JsonNode node, String... fieldNames) {
		if (node == null || node.isNull()) {
			return null;
		}

		for (String fieldName : fieldNames) {
			JsonNode found = node.findValue(fieldName);
			if (found != null && !found.isNull()) {
				return found.asText();
			}
		}

		return null;
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new ComprobanteException("No se pudo serializar informacion fiscal", e);
		}
	}
}
