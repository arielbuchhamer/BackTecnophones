package com.BackTecnophones.model.facturacion;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ComprobanteItem {
	private String descripcion;
	private BigDecimal cantidad;
	private BigDecimal precioUnitario;
	private AlicuotaIva alicuotaIva;
	private BigDecimal importeIva;
	private BigDecimal subtotal;
}
