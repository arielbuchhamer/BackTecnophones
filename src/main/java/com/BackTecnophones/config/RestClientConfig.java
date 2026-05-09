package com.BackTecnophones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
	@Bean
	public RestClient afRelayRestClient(AfRelayProperties properties) {
		RestClient.Builder builder = RestClient.builder()
				.requestFactory(new SimpleClientHttpRequestFactory())
				.baseUrl(properties.getBaseUrl());

		if (properties.getBearerToken() != null && !properties.getBearerToken().isBlank()) {
			builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getBearerToken());
		}

		return builder.build();
	}
}
