package com.BackTecnophones.controller;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.BackTecnophones.exception.AfRelayException;
import com.BackTecnophones.exception.ComprobanteException;
import com.BackTecnophones.model.facturacion.ComprobanteEmitido;
import com.BackTecnophones.model.facturacion.ComprobanteManualSolicitud;
import com.BackTecnophones.model.facturacion.ComprobanteSolicitud;
import com.BackTecnophones.service.ComprobanteService;

@RestController
@RequestMapping("/comprobantes")
public class ComprobanteController {
	private final ComprobanteService comprobanteService;

	public ComprobanteController(ComprobanteService comprobanteService) {
		this.comprobanteService = comprobanteService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ComprobanteEmitido crearComprobante(@RequestBody ComprobanteSolicitud solicitud) {
		try {
			return conPdfUrl(comprobanteService.generarComprobante(solicitud));
		} catch (ComprobanteException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/manual")
	@ResponseStatus(HttpStatus.CREATED)
	public ComprobanteEmitido crearComprobanteManual(@RequestBody ComprobanteManualSolicitud solicitud) {
		try {
			return conPdfUrl(comprobanteService.generarComprobanteManual(solicitud));
		} catch (ComprobanteException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@GetMapping
	public List<ComprobanteEmitido> obtenerComprobantes() {
		return comprobanteService.findAll().stream()
				.map(this::conPdfUrl)
				.toList();
	}

	@GetMapping("/{id}")
	public ComprobanteEmitido obtenerComprobante(@PathVariable String id) {
		return comprobanteService.findById(id)
				.map(this::conPdfUrl)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
	}

	@GetMapping("/idempotency/{idempotencyKey}")
	public ComprobanteEmitido obtenerPorIdempotencyKey(@PathVariable String idempotencyKey) {
		return comprobanteService.findByIdempotencyKey(idempotencyKey)
				.map(this::conPdfUrl)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
	}

	@GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Resource> obtenerPdf(@PathVariable String id) {
		ComprobanteEmitido comprobante = comprobanteService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));

		if (comprobante.getPdfPath() == null || comprobante.getPdfPath().isBlank()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "PDF no generado para este comprobante");
		}

		try {
			Path pdfPath = Path.of(comprobante.getPdfPath()).normalize();
			if (!Files.exists(pdfPath) || !Files.isRegularFile(pdfPath)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo PDF no encontrado");
			}

			Resource resource = new UrlResource(pdfPath.toUri());
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_PDF)
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo(comprobante) + "\"")
					.body(resource);
		} catch (MalformedURLException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ruta de PDF invalida", e);
		}
	}

	private ComprobanteEmitido conPdfUrl(ComprobanteEmitido comprobante) {
		if (comprobante.getId() != null) {
			String pdfUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/comprobantes/{id}/pdf")
					.buildAndExpand(comprobante.getId())
					.toUriString();
			comprobante.setPdfUrl(pdfUrl);
		}

		return comprobante;
	}

	private String nombreArchivo(ComprobanteEmitido comprobante) {
		String puntoVenta = String.format("%04d", comprobante.getPuntoVenta() == null ? 0 : comprobante.getPuntoVenta());
		String numero = String.format("%08d", comprobante.getNumeroComprobante() == null ? 0 : comprobante.getNumeroComprobante());
		return "factura-" + puntoVenta + "-" + numero + ".pdf";
	}
}
