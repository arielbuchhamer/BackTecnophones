package com.BackTecnophones.model.facturacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ComprobanteSolicitud {
	private String idempotencyKey;
	private DatosEmisor emisor;
	private DatosCliente cliente;
	private TipoComprobante tipoComprobante;
	private Integer puntoVenta;
	private Integer concepto;
	private LocalDate fecha;
	private String moneda;
	private BigDecimal cotizacion;
	private List<ComprobanteItem> items = new ArrayList<>();
	private ComprobanteImportes importes;
	private List<ComprobanteAsociado> comprobantesAsociados = new ArrayList<>();
}
