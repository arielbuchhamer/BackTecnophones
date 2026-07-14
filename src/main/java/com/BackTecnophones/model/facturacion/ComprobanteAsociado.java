package com.BackTecnophones.model.facturacion;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ComprobanteAsociado {
	private TipoComprobante tipoComprobante;
	private Integer puntoVenta;
	private Long numero;
	private String cuit;
	private LocalDate fecha;
}
