package com.BackTecnophones.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.mercadopago.MercadoPagoConfig;

import jakarta.annotation.PostConstruct;

// Setea el access token de MP al arrancar, asi el webhook funciona aunque la app se haya reiniciado
@Configuration
public class MercadoPagoConfiguration {
	@Value("${mercadopago.access-token}")
	private String accessToken;

	@PostConstruct
	public void init() {
		MercadoPagoConfig.setAccessToken(accessToken);
	}
}
