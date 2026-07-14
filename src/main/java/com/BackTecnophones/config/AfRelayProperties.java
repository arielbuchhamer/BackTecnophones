package com.BackTecnophones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "afrelay")
public class AfRelayProperties {
	private String baseUrl = "http://localhost:8000";
	private String bearerToken;
}
