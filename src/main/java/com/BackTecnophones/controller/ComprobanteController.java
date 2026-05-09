package com.BackTecnophones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.exception.AfRelayException;
import com.BackTecnophones.exception.ComprobanteException;
import com.BackTecnophones.model.facturacion.ComprobanteEmitido;
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
			return comprobanteService.generarComprobante(solicitud);
		} catch (ComprobanteException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@GetMapping
	public List<ComprobanteEmitido> obtenerComprobantes() {
		return comprobanteService.findAll();
	}

	@GetMapping("/{id}")
	public ComprobanteEmitido obtenerComprobante(@PathVariable String id) {
		return comprobanteService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
	}

	@GetMapping("/idempotency/{idempotencyKey}")
	public ComprobanteEmitido obtenerPorIdempotencyKey(@PathVariable String idempotencyKey) {
		return comprobanteService.findByIdempotencyKey(idempotencyKey)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comprobante no encontrado"));
	}
}
