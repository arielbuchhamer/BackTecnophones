package com.BackTecnophones.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.BackTecnophones.service.VentaService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

@RestController
@RequestMapping("/webhooks/mp")
public class MercadoPagoWebhookController {
	@Autowired
	VentaService ventaService;
	
	 @PostMapping
	 public ResponseEntity<String> receive(@RequestParam Map<String, String> params, @RequestHeader(value="X-Request-Id", required=false) String requestId, @RequestBody(required=false) String body) {
		 String type = params.get("type");
		 String dataId = params.get("data.id");
		 
		 if (!"payment".equalsIgnoreCase(type) || dataId == null) 
	            return ResponseEntity.ok("ignored");
		 try {
	            Long paymentId = Long.valueOf(dataId);
	
	            // Traer el pago de MP
	            PaymentClient paymentClient = new PaymentClient();
	            Payment payment = paymentClient.get(paymentId);
	
	            // Verificar estado
	            if ("approved".equalsIgnoreCase(payment.getStatus())) {
	
	                // Obtener external_reference (orderId)
	                String orderId = payment.getExternalReference();
	
	                // Verifico si el pago no fue procesado ya
	                if (ventaService.pagoYaProcesado(paymentId)) 
	                    return ResponseEntity.ok("already processed");
	
	                // 5) Transacción atómica:
	                //    - Descontar stock por cada item
	                //    - Generar Venta (persistir)
	                //    - Marcar paymentId como procesado
	                ventaService.confirmarVentaDesdePago(orderId, paymentId, payment);
	
	                return ResponseEntity.ok("ok");
	            } else {
	                return ResponseEntity.ok("status updated");
	            }
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.OK).body("error");
	        }
	 }
}
