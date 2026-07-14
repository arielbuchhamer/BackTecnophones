package com.BackTecnophones.model.facturacion;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class DatosFiscales {
	private String cae;
	private LocalDate caeVencimiento;
	private String resultado;
	private List<String> observaciones;
	private String qrPayload;
}
