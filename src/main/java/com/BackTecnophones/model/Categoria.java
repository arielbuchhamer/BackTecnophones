package com.BackTecnophones.model;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "categorias")
public class Categoria extends ClaseGenerica {
	private String rubroId;
	@ReadOnlyProperty
    @Field("rubroDescripcion")
    private String rubroDescripcion;
}
