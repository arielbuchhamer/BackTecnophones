package com.BackTecnophones.model.facturacion;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DatosEmisor {
	private String cuit;
	private String razonSocial;
	private CondicionIva condicionIva;
	private String domicilio;
	private String ingresosBrutos;
	private LocalDate inicioActividades;
}
