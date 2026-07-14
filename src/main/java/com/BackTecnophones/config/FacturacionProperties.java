package com.BackTecnophones.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.BackTecnophones.model.facturacion.AlicuotaIva;
import com.BackTecnophones.model.facturacion.CondicionIva;
import com.BackTecnophones.model.facturacion.TipoComprobante;

import lombok.Data;

@Data
@Component
@PropertySource("classpath:facturacion.properties")
@ConfigurationProperties(prefix = "facturacion")
public class FacturacionProperties {
	private Integer puntoVenta = 1;
	private TipoComprobante tipoComprobante = TipoComprobante.FACTURA_B;
	private String moneda = "PES";
	private BigDecimal cotizacion = BigDecimal.ONE;
	private AlicuotaIva alicuotaIvaDefault = AlicuotaIva.IVA_21;
	private BigDecimal umbralIdentificacionConsumidorFinal = BigDecimal.valueOf(10_000_000);
	private Emisor emisor = new Emisor();

	@Data
	public static class Emisor {
		private String cuit;
		private String razonSocial = "TecnoPhones";
		private CondicionIva condicionIva = CondicionIva.RESPONSABLE_INSCRIPTO;
		private String domicilio;
		private String ingresosBrutos;
		private LocalDate inicioActividades;
	}
}
