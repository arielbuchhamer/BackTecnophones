package com.BackTecnophones.model;

import org.springframework.data.annotation.Id;

public class ClaseGenerica {
	@Id
	private String id;
	private String descripcion;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getDescripcion() {
		return descripcion;
	}
	
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
