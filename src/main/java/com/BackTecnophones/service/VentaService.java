package com.BackTecnophones.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BackTecnophones.model.Articulo;
import com.BackTecnophones.model.EstadoVenta;
import com.BackTecnophones.model.Venta;
import com.BackTecnophones.model.Venta.VentaDetalle;
import com.BackTecnophones.repository.VentaRepository;
import com.mercadopago.resources.payment.Payment;
import com.mongodb.client.result.UpdateResult;

@Service
@Transactional
public class VentaService implements GenericService<Venta>{
	private final VentaRepository ventaRepo;
	private final MongoTemplate mongoTemplate;
	
	public VentaService(VentaRepository ventaRepo, MongoTemplate mongoTemplat)
	{
		this.ventaRepo = ventaRepo;
		this.mongoTemplate = mongoTemplat;
	}

	@Override
	public Optional<Venta> findById(String id) {
		return ventaRepo.findById(id);
	}

	@Override
	public List<Venta> findAll() {
		return ventaRepo.findAll();
	}

	@Override
	public Venta save(Venta entity) {
		return ventaRepo.save(entity);
	}

	@Override
	public void delete(String id) {
		// No implementado
	}
	
	public void registrarPreferencia(String ventaId, String preferenceId, String checkoutUrl) {
	    Venta v = this.findById(ventaId).orElseThrow();
	    v.getPago().setPreferenceId(preferenceId);
	    v.getPago().setCheckoutUrl(checkoutUrl);
	    
	    this.save(v);
	}
	
	public void confirmarVentaDesdePago(String orderId, Long paymentId, Payment payment) {
        Venta venta = ventaRepo.findByPagoOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Venta no encontrada para orderId=" + orderId));

        if (venta.getPago().getPaymentId() != null && this.pagoYaProcesado(paymentId)) 
            return; // Pago ya processado

        List<VentaDetalle> descontados = new ArrayList<>();
        
        try {
        	// Descuento de stock atómico por cada item
            venta.getDetalles().forEach(det -> {
            	disminuirStock(det.getArticuloId(), det.getCantidad().intValue());
            	descontados.add(det);
            });

            venta.setEstado(EstadoVenta.APROBADO);
            venta.getPago().setPaymentId(String.valueOf(paymentId));
            venta.getPago().setPaymentStatusDetail(payment.getStatusDetail());  // ej: "accredited"
            venta.getPago().setFechaPago(LocalDateTime.now());

            this.save(venta);
        	
        } catch (RuntimeException e) {
            // Rollback best-effort solo de lo que sí se descontó
            revertirStockParcial(descontados);
            throw e;
        }
    }
	
	// Se resta stock asi porque es atomico(o hace todo, o no hace nada) y no se expone a condiciones de carrera. Es una sola operacion atomica: Si no hay sotck suficiente no modificada nada
	private void disminuirStock(String articuloId, int cantidad) {
		Query q = new Query(Criteria.where("_id").is(articuloId).and("stock").gte(cantidad));
        Update u = new Update().inc("stock", -cantidad);
        UpdateResult r = mongoTemplate.updateFirst(q, u, Articulo.class);

        // Me fijo si hay stock suficiente
        if (r.getModifiedCount() == 0) 
        	throw new IllegalStateException("Stock insuficiente para articulo=" + articuloId);
	}
	
	private void revertirStockParcial(List<VentaDetalle> descontados) {
		descontados.forEach(det -> {
            Query q = new Query(Criteria.where("_id").is(det.getArticuloId()));
            Update u = new Update().inc("stock", +det.getCantidad().intValue());
            mongoTemplate.updateFirst(q, u, Articulo.class);
		});
    }
	
	public boolean pagoYaProcesado(Long paymentId) {
		return ventaRepo.existsByPagoPaymentId(String.valueOf(paymentId));
	}
	
	public void setEstadoVenta(Venta venta, EstadoVenta estadoVenta) {
		venta.setEstado(estadoVenta);
		
		ventaRepo.save(venta);
	}
	
	public List<Venta> findVentasByEstadoOrderByFechaCreacionDesc(EstadoVenta estado) {
		return ventaRepo.findByEstadoOrderByFechaCreacionDesc(estado);
	}
}
