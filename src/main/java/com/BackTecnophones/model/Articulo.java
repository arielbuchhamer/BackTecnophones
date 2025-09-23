package com.BackTecnophones.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Data
@Document(collection = "articulos")
public class Articulo extends ClaseGenerica {
	private BigDecimal precio;
	private Integer stock;
	private String imageId;
	private String rubroId;
	private String categoriaId;
	private List<Variante> variantes = new ArrayList<>();

	@Data
	public static class Variante {
	    private String sku;             
	    private String color;
	    private Integer stockVariante;
	    
	    public Variante() {}
	    
		public Variante(String sku, String color, Integer stockVariante) {
			super();
			this.sku = sku;
			this.color = color;
			this.stockVariante = stockVariante;
		}
	}
}
