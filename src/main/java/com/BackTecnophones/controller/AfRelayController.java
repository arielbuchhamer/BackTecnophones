package com.BackTecnophones.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.client.AfRelayClient;
import com.BackTecnophones.exception.AfRelayException;
import com.BackTecnophones.model.facturacion.TipoComprobante;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/afrelay")
public class AfRelayController {
	private final AfRelayClient afRelayClient;

	public AfRelayController(AfRelayClient afRelayClient) {
		this.afRelayClient = afRelayClient;
	}

	@PostMapping("/debug")
	public Map<String, String> debugPost() {
		return Map.of("status", "ok", "method", "POST");
	}

	@GetMapping("/debug/config")
	public Map<String, Object> debugConfig() {
		return Map.of(
				"baseUrl", afRelayClient.baseUrl(),
				"bearerTokenConfigured", afRelayClient.tieneBearerTokenConfigurado(),
				"bearerTokenLength", afRelayClient.bearerTokenLength());
	}

	@GetMapping("/health/readiness")
	public String readiness() {
		try {
			return afRelayClient.readiness();
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@GetMapping("/health/liveness")
	public String liveness() {
		try {
			return afRelayClient.liveness();
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@GetMapping("/wsfe/ultimo-autorizado")
	public JsonNode ultimoAutorizado(
			@RequestParam Long cuit,
			@RequestParam Integer puntoVenta,
			@RequestParam TipoComprobante tipoComprobante) {
		try {
			return afRelayClient.obtenerUltimoAutorizado(cuit, puntoVenta, tipoComprobante);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/wsaa/login-cms")
	public JsonNode renovarTicketAcceso() {
		try {
			return afRelayClient.renovarTicketAcceso();
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/wsaa/login-cms-debug")
	public Map<String, String> renovarTicketAccesoDebug() {
		try {
			JsonNode response = afRelayClient.renovarTicketAcceso();
			return Map.of("status", "ok", "response", response.toString());
		} catch (Exception e) {
			return Map.of(
					"status", "error",
					"exception", e.getClass().getName(),
					"message", e.getMessage() == null ? "" : e.getMessage());
		}
	}

	@GetMapping("/wsfe/puntos-venta")
	public JsonNode puntosVenta(@RequestParam Long cuit) {
		try {
			return afRelayClient.obtenerPuntosVenta(cuit);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/wsfe/ultimo-autorizado")
	public JsonNode ultimoAutorizadoRaw(@RequestBody Map<String, Object> request) {
		try {
			return afRelayClient.postRaw("/wsfev1/FECompUltimoAutorizado", request);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@GetMapping("/wsfe/consultar")
	public JsonNode consultarComprobante(
			@RequestParam Long cuit,
			@RequestParam Integer puntoVenta,
			@RequestParam TipoComprobante tipoComprobante,
			@RequestParam Long numeroComprobante) {
		try {
			return afRelayClient.consultarComprobante(cuit, puntoVenta, tipoComprobante, numeroComprobante);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/wsfe/consultar")
	public JsonNode consultarComprobanteRaw(@RequestBody Map<String, Object> request) {
		try {
			return afRelayClient.consultarComprobante(request);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}

	@PostMapping("/wsfe/cae-solicitar")
	public JsonNode solicitarCaeRaw(@RequestBody Map<String, Object> request) {
		try {
			return afRelayClient.solicitarCae(request);
		} catch (AfRelayException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
		}
	}
}
