package com.BackTecnophones.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Accesorio;
import com.BackTecnophones.model.Categoria;
import com.BackTecnophones.service.CategoriaService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

@RestController
@RequestMapping("/pago")
public class MercadoPagoController {
	
	@Autowired
	private CategoriaService categoriaService;
	
	@GetMapping("/mp")
	public String mercado(@RequestBody Accesorio accesorio, @PathVariable int cantidad) throws MPException, MPApiException {
		
		Categoria categoria = categoriaService.findById(accesorio.getCategoriaId())
		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
		
		MercadoPagoConfig.setAccessToken("TEST-7367239675157827-070915-7fe666fecab3aff28a224cfb0bc6f301-321665846");
		
		PreferenceBackUrlsRequest backUrls =
				   PreferenceBackUrlsRequest.builder()
				       .success("https://www.tu-sitio/success")
				       .pending("https://www.tu-sitio/pending")
				       .failure("https://www.tu-sitio/failure")
				       .build();
		
		PreferenceItemRequest itemRequest =
			       PreferenceItemRequest.builder()
			           .id(accesorio.getId())
			           .title(categoria.getDescripcion())
			           .description(accesorio.getDescripcion())
			           .pictureUrl("http://picture.com/PS5")
			           .categoryId(accesorio.getCategoriaId())
			           .quantity(cantidad)
			           .currencyId("ARS")
			           .unitPrice(accesorio.getPrecio())
			           .build();
		
		List<PreferenceItemRequest> items = new ArrayList<>();
			   
	   	items.add(itemRequest);
			   
		PreferenceRequest preferenceRequest = PreferenceRequest.builder().items(items).backUrls(backUrls).build();
		
		PreferenceClient client = new PreferenceClient();
		
		Preference preference = client.create(preferenceRequest);
		
		//return preference.getInitPoint(); Esto es para producción
		return preference.getSandboxInitPoint(); //Esto es para desarrollo
	}
}
