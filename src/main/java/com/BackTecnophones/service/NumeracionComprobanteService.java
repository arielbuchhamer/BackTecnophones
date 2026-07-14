package com.BackTecnophones.service;

import org.springframework.stereotype.Service;

import com.BackTecnophones.client.AfRelayClient;
import com.BackTecnophones.model.facturacion.TipoComprobante;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class NumeracionComprobanteService {
	private final AfRelayClient afRelayClient;

	public NumeracionComprobanteService(AfRelayClient afRelayClient) {
		this.afRelayClient = afRelayClient;
	}

	public Long proximoNumero(String cuit, Integer puntoVenta, TipoComprobante tipoComprobante) {
		JsonNode response = afRelayClient.obtenerUltimoAutorizado(Long.valueOf(cuit), puntoVenta, tipoComprobante);
		long ultimo = buscarLong(response, "CbteNro", "cbteNro", "nroComprobante", "ultimoAutorizado");
		return ultimo + 1;
	}

	private long buscarLong(JsonNode node, String... fieldNames) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return 0L;
		}

		for (String fieldName : fieldNames) {
			JsonNode found = node.findValue(fieldName);
			if (found != null && found.canConvertToLong()) {
				return found.asLong();
			}
		}

		return 0L;
	}
}
