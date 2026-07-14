package com.BackTecnophones.model.facturacion;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "comprobantes_emitidos")
public class ComprobanteEmitido {
	@Id
	private String id;

	@Indexed(unique = true)
	private String idempotencyKey;

	private EstadoComprobante estado;
	private ComprobanteSolicitud solicitud;
	private DatosFiscales datosFiscales;
	private Integer puntoVenta;
	private TipoComprobante tipoComprobante;
	private Long numeroComprobante;
	private String afRelayRequestJson;
	private String afRelayResponseJson;
	private String pdfPath;
	@Transient
	private String pdfUrl;
	private String error;
	private Instant fechaCreacion;
	private Instant fechaAutorizacion;
}
