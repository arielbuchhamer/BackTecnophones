package com.BackTecnophones.model.facturacion;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class AfRelayRespuesta {
	private String endpoint;
	private JsonNode body;
}
