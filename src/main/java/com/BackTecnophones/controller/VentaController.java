package com.BackTecnophones.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.BackTecnophones.model.Articulo;
import com.BackTecnophones.model.EstadoVenta;
import com.BackTecnophones.model.Venta;
import com.BackTecnophones.model.Venta.Pago;
import com.BackTecnophones.service.ArticuloService;
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
	private static final String BASE_URL_BACK = "https://api.tecnophones.com.ar/";
	@Autowired
	VentaService ventaService;
	@Autowired
	ArticuloService articuloService;
	
	@PostMapping("/mp")
	public String mercado(@RequestBody Venta venta) throws MPException, MPApiException {	
		MercadoPagoConfig.setAccessToken("APP_USR-8836744955592659-112717-6b4a2956c0caca4a33754249a169c9c5-324027017");
		
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
		venta.getDetalles().forEach(detalle -> {
			Optional<Articulo> art = articuloService.findById(detalle.getArticuloId());
			art.ifPresent(a -> {
				if (detalle.getSku() == null && BigDecimal.valueOf(a.getStock()).compareTo(detalle.getCantidad()) < 0)
					throw new RuntimeException("No hay stock suficiente para el artículo: " + a.getDescripcion());
				else if (detalle.getSku() != null)
				{
					var varianteOpt = a.getVariantes().stream()
				            .filter(artic -> Objects.equals(artic.getSku(), detalle.getSku()))
				            .findFirst();
					
					if (varianteOpt.isEmpty()) {
			            throw new RuntimeException("Error con el codigo del producto " + a.getDescripcion());
			        }
					
					Integer stockVar = varianteOpt.get().getStockVariante();
					if (BigDecimal.valueOf(stockVar).compareTo(detalle.getCantidad()) < 0)
						throw new RuntimeException("No hay stock suficiente para el artículo: " + a.getDescripcion());
				}
		            
			});
			
		});
		venta.setPago(new Pago());
		venta.getPago().setOrderId(UUID.randomUUID().toString());
		venta.setEstado(EstadoVenta.PENDIENTE);
		venta.setFechaCreacion(Instant.now());
		
		return ventaService.save(venta);
	}
	
	@GetMapping("/aprobadas")
	private List<Venta> getVentasAprobadas() {
		return ventaService.findVentasByEstadoOrderByFechaCreacionDesc(EstadoVenta.APROBADO);
	}
	
}
