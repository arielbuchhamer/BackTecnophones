package com.BackTecnophones.client;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.BackTecnophones.config.AfRelayProperties;
import com.BackTecnophones.exception.AfRelayException;
import com.BackTecnophones.model.facturacion.TipoComprobante;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AfRelayClient {
	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final AfRelayProperties properties;

	public AfRelayClient(RestClient afRelayRestClient, ObjectMapper objectMapper, AfRelayProperties properties) {
		this.restClient = afRelayRestClient;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public JsonNode renovarTicketAcceso() {
		return postSinBody("/wsaa/loginCms");
	}

	public JsonNode obtenerPuntosVenta(Long cuit) {
		Map<String, Object> request = Map.of("Auth", Map.of("Cuit", cuit));

		return post("/wsfev1/FEParamGetPtosVenta", request);
	}

	public JsonNode obtenerUltimoAutorizado(Long cuit, Integer puntoVenta, TipoComprobante tipoComprobante) {
		Map<String, Object> request = Map.of(
				"Auth", Map.of("Cuit", cuit),
				"PtoVta", puntoVenta,
				"CbteTipo", tipoComprobante.getCodigoArca());

		return post("/wsfev1/FECompUltimoAutorizado", request);
	}

	public JsonNode solicitarCae(Map<String, Object> request) {
		return post("/wsfev1/FECAESolicitar", request);
	}

	public JsonNode consultarComprobante(Map<String, Object> request) {
		return post("/wsfev1/FECompConsultar", request);
	}

	public JsonNode consultarComprobante(Long cuit, Integer puntoVenta, TipoComprobante tipoComprobante, Long numeroComprobante) {
		Map<String, Object> request = Map.of(
				"Auth", Map.of("Cuit", cuit),
				"FeCompConsReq", Map.of(
						"PtoVta", puntoVenta,
						"CbteTipo", tipoComprobante.getCodigoArca(),
						"CbteNro", numeroComprobante));

		return consultarComprobante(request);
	}

	public String readiness() {
		return getText("/wsfev1/health/readiness");
	}

	public String liveness() {
		return getText("/health/liveness");
	}

	public JsonNode postRaw(String endpoint, Map<String, Object> request) {
		return post(endpoint, request);
	}

	public boolean tieneBearerTokenConfigurado() {
		return properties.getBearerToken() != null && !properties.getBearerToken().isBlank();
	}

	public int bearerTokenLength() {
		return properties.getBearerToken() == null ? 0 : properties.getBearerToken().length();
	}

	public String baseUrl() {
		return properties.getBaseUrl();
	}

	private String getText(String endpoint) {
		try {
			String response = restClient.get()
					.uri(endpoint)
					.retrieve()
					.body(String.class);

			return response == null ? "" : response;
		} catch (RestClientException e) {
			throw new AfRelayException("Error comunicando con AFRelay en " + endpoint, e);
		}
	}

	private JsonNode post(String endpoint, Object request) {
		try {
			String response = restClient.post()
					.uri(endpoint)
					.headers(this::addAuthorizationHeader)
					.body(request)
					.retrieve()
					.body(String.class);

			if (response == null || response.isBlank()) {
				return objectMapper.createObjectNode();
			}

			return objectMapper.readTree(response);
		} catch (RestClientResponseException e) {
			throw new AfRelayException("AFRelay respondio " + e.getStatusCode() + " en " + endpoint + ": " + e.getResponseBodyAsString(), e);
		} catch (RestClientException e) {
			throw new AfRelayException("Error comunicando con AFRelay en " + endpoint, e);
		} catch (JsonProcessingException e) {
			throw new AfRelayException("AFRelay respondio un JSON invalido en " + endpoint, e);
		}
	}

	private JsonNode postSinBody(String endpoint) {
		try {
			String response = restClient.post()
					.uri(endpoint)
					.headers(this::addAuthorizationHeader)
					.retrieve()
					.body(String.class);

			if (response == null || response.isBlank()) {
				return objectMapper.createObjectNode();
			}

			return objectMapper.readTree(response);
		} catch (RestClientResponseException e) {
			throw new AfRelayException("AFRelay respondio " + e.getStatusCode() + " en " + endpoint + ": " + e.getResponseBodyAsString(), e);
		} catch (RestClientException e) {
			throw new AfRelayException("Error comunicando con AFRelay en " + endpoint, e);
		} catch (JsonProcessingException e) {
			throw new AfRelayException("AFRelay respondio un JSON invalido en " + endpoint, e);
		}
	}

	private void addAuthorizationHeader(HttpHeaders headers) {
		if (properties.getBearerToken() != null && !properties.getBearerToken().isBlank()) {
			headers.setBearerAuth(properties.getBearerToken());
		}
	}
}
