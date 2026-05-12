package com.BackTecnophones.model.facturacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ComprobanteManualSolicitud {
	private String idempotencyKey;
	private LocalDate fecha;
	private DatosCliente cliente;
	private List<ComprobanteManualItem> items = new ArrayList<>();
}
