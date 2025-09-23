package com.BackTecnophones.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.BackTecnophones.model.EstadoVenta;
import com.BackTecnophones.model.Venta;
import com.BackTecnophones.model.Venta.Pago;
import com.BackTecnophones.service.VentaService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

@RestController
@RequestMapping("/ventas")
public class VentaController {
	private static final String BASE_URL = "http://localhost:9000/";
	@Autowired
	VentaService ventaService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Venta agregarVenta(@RequestBody Venta venta) {
		venta.getDetalles().forEach(v -> {
			System.out.println("Articulo: " + v.getArticuloId());
			System.out.println("Sku: " + v.getSku());
			System.out.println("Cantidad: " + v.getCantidad());
			System.out.println("Precio Unitario: " + v.getPrecioUnitario());
			System.out.println("SubTotal: " + v.getSubtotal());
			System.out.println("-----");
		});
		System.out.println("---------------------------");
		System.out.println("Total: " + venta.getTotal());
		
		return venta;
	}
	
	@PostMapping("/mp")
	public String mercado(@RequestBody Venta venta) throws MPException, MPApiException {	
		MercadoPagoConfig.setAccessToken("APP_USR-1278481914142217-091618-c0a522bcdf862d3592681dc93e5cf0f1-2697250004"); //Cambia para produccion
		
		Venta ventaCreada = generarVentaEnBD(venta);
		
		PreferenceBackUrlsRequest backUrls =
			   PreferenceBackUrlsRequest.builder()
			       .success(BASE_URL + "exito")
			       .pending(BASE_URL + "pendiente")
			       .failure(BASE_URL + "fallo")
			       .build();
		
		List<PreferenceItemRequest> items = new ArrayList<>();
		ventaCreada.getDetalles().forEach(ventaDetalle -> {
			PreferenceItemRequest itemRequest =
				       PreferenceItemRequest.builder()
				           .id(ventaDetalle.getArticuloId())
				           .title(ventaDetalle.getArticuloDescripcion())
//				           .description("") // Descripcion mas larga
				           .pictureUrl(BASE_URL + "articulos/" + ventaDetalle.getArticuloId() + "/imagen")
				           .categoryId("computing")
				           .quantity(ventaDetalle.getCantidad().intValue())
				           .currencyId("ARS")
				           .unitPrice(ventaDetalle.getPrecioUnitario())
				           .build();
				   
		   	items.add(itemRequest);
		});
		
		PreferenceRequest preferenceRequest = PreferenceRequest.builder().externalReference(venta.getPago().getOrderId()).notificationUrl(BASE_URL + "webhooks/mp").items(items).backUrls(backUrls).build();
		
		PreferenceClient client = new PreferenceClient();
		
		Preference preference = client.create(preferenceRequest);
		
		ventaService.registrarPreferencia(ventaCreada.getId(), preference.getId(), preference.getSandboxInitPoint());
//		ventaService.registrarPreferencia(ventaCreada.getId(), preference.getId(), preference.getInitPoint()); // Para produccion
		
		return preference.getInitPoint(); //Esto es para producción
		//return preference.getSandboxInitPoint(); //Esto es para desarrollo
	}
	
	private Venta generarVentaEnBD(Venta venta) {
		venta.setPago(new Pago());
		venta.getPago().setOrderId(UUID.randomUUID().toString());
		venta.setEstado(EstadoVenta.PENDIENTE);
		venta.setFechaCreacion(LocalDate.now());
		
		return ventaService.save(venta);
	}
	
}
