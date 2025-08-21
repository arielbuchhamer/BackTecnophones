package com.BackTecnophones.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias")
public class Categoria extends ClaseGenerica {
	String rubroId;
	
	public String getRubroId() {
		return rubroId;
	}
	
	public void setRubroId(String rubroId) {
		this.rubroId = rubroId;
	}
}
