package com.BackTecnophones.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class ClaseGenerica {
	@Id
	private String id;
	private String descripcion;
}
