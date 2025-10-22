package com.BackTecnophones.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "articulos")
@CompoundIndex(name = "idx_rubro_id_desc", def = "{'rubroId': 1, '_id': -1}")
public class Articulo extends ClaseGenerica {
	private BigDecimal precio;
	private Integer stock;
	private String imageId;
	private String rubroId;
	private String categoriaId;
	private List<Variante> variantes = new ArrayList<>();
	@ReadOnlyProperty
    @Field("rubroDescripcion")
	private String rubroDescripcion;
	@ReadOnlyProperty
    @Field("categoriaDescripcion")
	private String categoriaDescripcion;

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
