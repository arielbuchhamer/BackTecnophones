package com.BackTecnophones.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	//private static final String BASE_URL_FRONT = "https://tecnophones00.web.app/";
	private static final String BASE_URL_FRONT = "https://tecnophones00.web.app/";
	private static final String BASE_URL_BACK = "https://tecnophonesback.zeabur.app/";
	@Autowired
	VentaService ventaService;
	
	@PostMapping("/mp")
	public String mercado(@RequestBody Venta venta) throws MPException, MPApiException {	
		MercadoPagoConfig.setAccessToken("APP_USR-3788939057992741-092214-9f8b513eb2a9b2b25ba1aea0a9a1dc8e-204093481");
		
		Venta ventaCreada = generarVentaEnBD(venta);
		
		PreferenceBackUrlsRequest backUrls =
			   PreferenceBackUrlsRequest.builder()
			       .success(BASE_URL_FRONT + "exito")
			       .pending(BASE_URL_FRONT + "pendiente")
			       .failure(BASE_URL_FRONT + "fallo")
			       .build();
		
		List<PreferenceItemRequest> items = new ArrayList<>();
		ventaCreada.getDetalles().forEach(ventaDetalle -> {
			PreferenceItemRequest itemRequest =
				       PreferenceItemRequest.builder()
				           .id(ventaDetalle.getArticuloId())
				           .title(ventaDetalle.getArticuloDescripcion())
				           .description(ventaDetalle.getArticuloDescripcion()) // Descripcion mas larga
				           .pictureUrl(BASE_URL_BACK + "articulos/" + ventaDetalle.getArticuloId() + "/imagen")
				           .categoryId("computing")
				           .quantity(ventaDetalle.getCantidad().intValue())
				           .currencyId("ARS")
				           .unitPrice(ventaDetalle.getPrecioUnitario())
				           .build();
				   
		   	items.add(itemRequest);
		});
		
		PreferenceRequest preferenceRequest = PreferenceRequest.builder().externalReference(venta.getPago().getOrderId()).notificationUrl(BASE_URL_BACK + "webhooks/mp").items(items).backUrls(backUrls).autoReturn("approved").build();
		
		PreferenceClient client = new PreferenceClient();
		
		Preference preference = client.create(preferenceRequest);
		
		ventaService.registrarPreferencia(ventaCreada.getId(), preference.getId(), preference.getInitPoint());
		
		return preference.getInitPoint();
	}
	
	private Venta generarVentaEnBD(Venta venta) {
		venta.setPago(new Pago());
		venta.getPago().setOrderId(UUID.randomUUID().toString());
		venta.setEstado(EstadoVenta.PENDIENTE);
		venta.setFechaCreacion(LocalDateTime.now());
		
		return ventaService.save(venta);
	}
	
	@GetMapping("/aprobadas")
	private List<Venta> getVentasAprobadas() {
		return ventaService.findVentasByEstadoOrderByFechaCreacionDesc(EstadoVenta.APROBADO);
	}
	
}
