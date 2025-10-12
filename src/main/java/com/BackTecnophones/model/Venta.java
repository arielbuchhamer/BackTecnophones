package com.BackTecnophones.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "ventas")
public class Venta {
	@Id
	private String id;
	private Pago pago;
    private EstadoVenta estado;
	private String nombreCliente;
	private String apellidoCliente;
	private String dniCliente;
	private String emailCliente;
	private String telefonoCliente;
	private Direccion direccionEnvio;
	private List<VentaDetalle> detalles = new ArrayList<>();
    private BigDecimal total;
	private ZonedDateTime fechaCreacion;
	
	@Data
	public static class Pago {
		private String orderId;         // Referencia usada en Mercado Pago
		private String preferenceId;  	// id de la Preference en MP
		private String checkoutUrl;		// url del pago
		@Indexed(unique = true) // No puede haber dos ventas con el mismo paymentId
		private String paymentId;		// id de pago
		private String paymentStatusDetail;
		private ZonedDateTime fechaPago;
		
		public Pago() {}
		
		public Pago(String orderId, String preferenceId, String checkoutUrl, String paymentId,
				String paymentStatusDetail, ZonedDateTime fechaPago) {
			super();
			this.orderId = orderId;
			this.preferenceId = preferenceId;
			this.checkoutUrl = checkoutUrl;
			this.paymentId = paymentId;
			this.paymentStatusDetail = paymentStatusDetail;
			this.fechaPago = fechaPago;
		}
	}
	
	@Data
	public static class Direccion {
	    private String calle;
	    private String numero;
	    private String piso;
	    private String departamento;
	    private String ciudad;
	    private String provincia;
	    private String codigoPostal;
	    
	    public Direccion() {}

		public Direccion(String calle, String numero, String piso, String departamento, String ciudad, String provincia,
				String codigoPostal) {
			super();
			this.calle = calle;
			this.numero = numero;
			this.piso = piso;
			this.departamento = departamento;
			this.ciudad = ciudad;
			this.provincia = provincia;
			this.codigoPostal = codigoPostal;
		}
	}
	
	@Data
	public static class VentaDetalle {
		private String articuloId;
		private String sku;
		private String articuloDescripcion;
		private BigDecimal cantidad;
		private BigDecimal precioUnitario;
		private BigDecimal subtotal;
		
		public VentaDetalle() {}
		
		public VentaDetalle(String articuloId, String sku, String articuloDescripcion, BigDecimal cantidad, BigDecimal precioUnitario,
				BigDecimal subtotal) {
			super();
			this.articuloId = articuloId;
			this.sku = sku;
			this.articuloDescripcion = articuloDescripcion;
			this.cantidad = cantidad;
			this.precioUnitario = precioUnitario;
			this.subtotal = subtotal;
		}
	}
	
}