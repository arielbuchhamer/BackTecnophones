package com.BackTecnophones.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.BackTecnophones.config.FacturacionProperties;
import com.BackTecnophones.exception.ComprobanteException;
import com.BackTecnophones.model.facturacion.ComprobanteImportes;
import com.BackTecnophones.model.facturacion.ComprobanteItem;
import com.BackTecnophones.model.facturacion.ComprobanteSolicitud;
import com.BackTecnophones.model.facturacion.TipoComprobante;
import com.BackTecnophones.model.facturacion.TipoDocumento;

@Service
public class ComprobanteFiscalValidator {
	private final FacturacionProperties facturacionProperties;

	public ComprobanteFiscalValidator(FacturacionProperties facturacionProperties) {
		this.facturacionProperties = facturacionProperties;
	}

	public void validar(ComprobanteSolicitud solicitud) {
		if (solicitud == null) {
			throw new ComprobanteException("La solicitud no puede ser nula");
		}
		if (isBlank(solicitud.getIdempotencyKey())) {
			throw new ComprobanteException("idempotencyKey es obligatorio");
		}
		if (solicitud.getEmisor() == null || isBlank(solicitud.getEmisor().getCuit())) {
			throw new ComprobanteException("emisor.cuit es obligatorio");
		}
		if (solicitud.getCliente() == null) {
			throw new ComprobanteException("cliente es obligatorio");
		}
		if (solicitud.getTipoComprobante() == null) {
			throw new ComprobanteException("tipoComprobante es obligatorio");
		}
		if (solicitud.getPuntoVenta() == null || solicitud.getPuntoVenta() <= 0) {
			throw new ComprobanteException("puntoVenta debe ser mayor a cero");
		}
		if (solicitud.getFecha() == null) {
			throw new ComprobanteException("fecha es obligatoria");
		}
		validarItems(solicitud.getItems());
		validarImportes(solicitud.getImportes());
		validarIdentificacionConsumidorFinal(solicitud);

		if (requiereComprobanteAsociado(solicitud.getTipoComprobante())
				&& (solicitud.getComprobantesAsociados() == null || solicitud.getComprobantesAsociados().isEmpty())) {
			throw new ComprobanteException("Las notas de credito/debito requieren comprobantesAsociados");
		}
	}

	private void validarItems(List<ComprobanteItem> items) {
		if (items == null || items.isEmpty()) {
			throw new ComprobanteException("items no puede estar vacio");
		}

		for (ComprobanteItem item : items) {
			if (isBlank(item.getDescripcion())) {
				throw new ComprobanteException("Cada item debe tener descripcion");
			}
			if (item.getCantidad() == null || item.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
				throw new ComprobanteException("Cada item debe tener cantidad mayor a cero");
			}
			if (item.getPrecioUnitario() == null || item.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
				throw new ComprobanteException("Cada item debe tener precioUnitario mayor o igual a cero");
			}
		}
	}

	private void validarImportes(ComprobanteImportes importes) {
		if (importes == null) {
			throw new ComprobanteException("importes es obligatorio");
		}
		if (importes.getTotal() == null || importes.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ComprobanteException("importes.total debe ser mayor a cero");
		}

		BigDecimal neto = orZero(importes.getNeto());
		BigDecimal iva = orZero(importes.getIva());
		BigDecimal tributos = orZero(importes.getTributos());
		BigDecimal exento = orZero(importes.getExento());
		BigDecimal noGravado = orZero(importes.getNoGravado());
		BigDecimal suma = neto.add(iva).add(tributos).add(exento).add(noGravado);

		if (suma.compareTo(importes.getTotal()) != 0) {
			throw new ComprobanteException("importes no coincide: neto + iva + tributos + exento + noGravado debe ser igual a total");
		}
	}

	private boolean requiereComprobanteAsociado(TipoComprobante tipoComprobante) {
		return tipoComprobante.name().startsWith("NOTA_");
	}

	// RG 4444 (umbral actualizado por RG 5866/2026): comprobantes B/C individuales no pueden
	// emitirse a Consumidor Final (DocTipo=99) si el total iguala o supera el umbral vigente.
	private void validarIdentificacionConsumidorFinal(ComprobanteSolicitud solicitud) {
		TipoComprobante tipoComprobante = solicitud.getTipoComprobante();
		if (!aplicaUmbralIdentificacion(tipoComprobante)) {
			return;
		}

		TipoDocumento tipoDocumento = solicitud.getCliente().getTipoDocumento();
		boolean esConsumidorFinal = tipoDocumento == null || tipoDocumento == TipoDocumento.CONSUMIDOR_FINAL;
		if (!esConsumidorFinal) {
			return;
		}

		BigDecimal umbral = facturacionProperties.getUmbralIdentificacionConsumidorFinal();
		BigDecimal total = solicitud.getImportes() == null ? null : solicitud.getImportes().getTotal();
		if (umbral == null || total == null) {
			return;
		}

		if (total.compareTo(umbral) >= 0) {
			throw new ComprobanteException(
					"El total ($" + total + ") iguala o supera el umbral de identificacion (RG 4444, $" + umbral
							+ "). Debe informarse un tipoDocumento y numeroDocumento reales del cliente.");
		}
	}

	private boolean aplicaUmbralIdentificacion(TipoComprobante tipoComprobante) {
		return tipoComprobante.name().endsWith("_B") || tipoComprobante.name().endsWith("_C");
	}

	private BigDecimal orZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
