package com.BackTecnophones.service;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.BackTecnophones.exception.ComprobanteException;
import com.BackTecnophones.model.facturacion.ComprobanteEmitido;
import com.BackTecnophones.model.facturacion.ComprobanteImportes;
import com.BackTecnophones.model.facturacion.ComprobanteItem;
import com.BackTecnophones.model.facturacion.ComprobanteSolicitud;
import com.BackTecnophones.model.facturacion.DatosCliente;
import com.BackTecnophones.model.facturacion.DatosEmisor;
import com.BackTecnophones.model.facturacion.DatosFiscales;

@Service
public class ComprobantePdfService {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final float MARGIN = 42;
	private static final float LINE_HEIGHT = 16;
	private static final float TABLE_ROW_HEIGHT = 22;

	private final Path pdfDir;
	private final DecimalFormat moneyFormat;

	public ComprobantePdfService(@Value("${facturacion.pdf-dir:comprobantes-pdf}") String pdfDir) {
		this.pdfDir = Path.of(pdfDir).toAbsolutePath().normalize();
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		this.moneyFormat = new DecimalFormat("#,##0.00", symbols);
	}

	public String generarPdf(ComprobanteEmitido comprobante) {
		try {
			Files.createDirectories(pdfDir);
			Path path = pdfDir.resolve(nombreArchivo(comprobante)).normalize();

			try (PDDocument document = new PDDocument()) {
				PDPage page = new PDPage(PDRectangle.A4);
				document.addPage(page);

				try (PDPageContentStream content = new PDPageContentStream(document, page)) {
					float y = page.getMediaBox().getHeight() - MARGIN;
					y = dibujarEncabezado(content, comprobante, y);
					y = dibujarCliente(content, comprobante.getSolicitud(), y - 12);
					y = dibujarItems(content, comprobante.getSolicitud(), y - 14);
					y = dibujarTotales(content, comprobante.getSolicitud().getImportes(), y - 18);
					dibujarDatosFiscales(content, comprobante.getDatosFiscales(), y - 18);
				}

				document.save(path.toFile());
			}

			return path.toString();
		} catch (IOException e) {
			throw new ComprobanteException("No se pudo generar el PDF del comprobante", e);
		}
	}

	private float dibujarEncabezado(PDPageContentStream content, ComprobanteEmitido comprobante, float y) throws IOException {
		ComprobanteSolicitud solicitud = comprobante.getSolicitud();
		DatosEmisor emisor = solicitud.getEmisor();

		rect(content, MARGIN, y - 86, 511, 86, Color.BLACK);
		text(content, "TecnoPhones", MARGIN + 14, y - 22, PDType1Font.HELVETICA_BOLD, 18);
		text(content, valor(emisor.getRazonSocial()), MARGIN + 14, y - 42, PDType1Font.HELVETICA, 10);
		text(content, "CUIT: " + valor(emisor.getCuit()), MARGIN + 14, y - 58, PDType1Font.HELVETICA, 10);
		text(content, "Domicilio: " + valor(emisor.getDomicilio()), MARGIN + 14, y - 74, PDType1Font.HELVETICA, 10);

		String letra = letraComprobante(comprobante);
		text(content, letra, 292, y - 33, PDType1Font.HELVETICA_BOLD, 28);
		text(content, "COD. " + comprobante.getTipoComprobante().getCodigoArca(), 286, y - 54, PDType1Font.HELVETICA, 9);

		text(content, tituloComprobante(comprobante), 380, y - 24, PDType1Font.HELVETICA_BOLD, 14);
		text(content, "Nro: " + numeroCompleto(comprobante), 380, y - 44, PDType1Font.HELVETICA, 10);
		text(content, "Fecha: " + fecha(solicitud.getFecha()), 380, y - 60, PDType1Font.HELVETICA, 10);
		text(content, "IVA: " + valor(emisor.getCondicionIva()), 380, y - 76, PDType1Font.HELVETICA, 10);
		return y - 100;
	}

	private float dibujarCliente(PDPageContentStream content, ComprobanteSolicitud solicitud, float y) throws IOException {
		DatosCliente cliente = solicitud.getCliente();
		rect(content, MARGIN, y - 74, 511, 74, Color.BLACK);
		text(content, "Cliente", MARGIN + 10, y - 18, PDType1Font.HELVETICA_BOLD, 11);
		text(content, "Nombre/Razon social: " + nombreCliente(cliente), MARGIN + 10, y - 36, PDType1Font.HELVETICA, 10);
		text(content, "Documento: " + valor(cliente.getTipoDocumento()) + " " + valor(cliente.getNumeroDocumento()), MARGIN + 10, y - 52, PDType1Font.HELVETICA, 10);
		text(content, "Condicion IVA: " + valor(cliente.getCondicionIva()), 310, y - 52, PDType1Font.HELVETICA, 10);
		text(content, "Domicilio: " + valor(cliente.getDomicilio()), MARGIN + 10, y - 68, PDType1Font.HELVETICA, 10);
		return y - 88;
	}

	private float dibujarItems(PDPageContentStream content, ComprobanteSolicitud solicitud, float y) throws IOException {
		float tableX = MARGIN;
		float tableWidth = 511;
		float descX = tableX + 8;
		float qtyX = tableX + 290;
		float priceX = tableX + 354;
		float ivaX = tableX + 420;
		float totalX = tableX + 468;

		fill(content, tableX, y - TABLE_ROW_HEIGHT, tableWidth, TABLE_ROW_HEIGHT, new Color(235, 235, 235));
		rect(content, tableX, y - TABLE_ROW_HEIGHT, tableWidth, TABLE_ROW_HEIGHT, Color.BLACK);
		text(content, "Descripcion", descX, y - 15, PDType1Font.HELVETICA_BOLD, 9);
		text(content, "Cant.", qtyX, y - 15, PDType1Font.HELVETICA_BOLD, 9);
		text(content, "Unit.", priceX, y - 15, PDType1Font.HELVETICA_BOLD, 9);
		text(content, "IVA", ivaX, y - 15, PDType1Font.HELVETICA_BOLD, 9);
		text(content, "Subtotal", totalX, y - 15, PDType1Font.HELVETICA_BOLD, 9);

		y -= TABLE_ROW_HEIGHT;
		for (ComprobanteItem item : solicitud.getItems()) {
			rect(content, tableX, y - TABLE_ROW_HEIGHT, tableWidth, TABLE_ROW_HEIGHT, Color.LIGHT_GRAY);
			text(content, cortar(valor(item.getDescripcion()), 48), descX, y - 15, PDType1Font.HELVETICA, 9);
			text(content, numero(item.getCantidad()), qtyX, y - 15, PDType1Font.HELVETICA, 9);
			text(content, moneda(item.getPrecioUnitario()), priceX, y - 15, PDType1Font.HELVETICA, 9);
			text(content, porcentajeIva(item), ivaX, y - 15, PDType1Font.HELVETICA, 9);
			text(content, moneda(item.getSubtotal()), totalX, y - 15, PDType1Font.HELVETICA, 9);
			y -= TABLE_ROW_HEIGHT;
		}

		return y;
	}

	private float dibujarTotales(PDPageContentStream content, ComprobanteImportes importes, float y) throws IOException {
		float xLabel = 380;
		float xValue = 470;
		text(content, "Neto:", xLabel, y, PDType1Font.HELVETICA, 10);
		text(content, moneda(importes.getNeto()), xValue, y, PDType1Font.HELVETICA, 10);
		text(content, "IVA:", xLabel, y - LINE_HEIGHT, PDType1Font.HELVETICA, 10);
		text(content, moneda(importes.getIva()), xValue, y - LINE_HEIGHT, PDType1Font.HELVETICA, 10);
		text(content, "Tributos:", xLabel, y - LINE_HEIGHT * 2, PDType1Font.HELVETICA, 10);
		text(content, moneda(importes.getTributos()), xValue, y - LINE_HEIGHT * 2, PDType1Font.HELVETICA, 10);
		text(content, "Total:", xLabel, y - LINE_HEIGHT * 3, PDType1Font.HELVETICA_BOLD, 12);
		text(content, moneda(importes.getTotal()), xValue, y - LINE_HEIGHT * 3, PDType1Font.HELVETICA_BOLD, 12);
		return y - LINE_HEIGHT * 4;
	}

	private void dibujarDatosFiscales(PDPageContentStream content, DatosFiscales datos, float y) throws IOException {
		rect(content, MARGIN, y - 54, 511, 54, Color.BLACK);
		text(content, "Comprobante autorizado por ARCA", MARGIN + 10, y - 17, PDType1Font.HELVETICA_BOLD, 10);
		text(content, "CAE: " + valor(datos == null ? null : datos.getCae()), MARGIN + 10, y - 34, PDType1Font.HELVETICA, 10);
		text(content, "Vto. CAE: " + fecha(datos == null ? null : datos.getCaeVencimiento()), 310, y - 34, PDType1Font.HELVETICA, 10);
	}

	private void text(PDPageContentStream content, String text, float x, float y, PDType1Font font, int size) throws IOException {
		content.beginText();
		content.setFont(font, size);
		content.newLineAtOffset(x, y);
		content.showText(sanitizar(text));
		content.endText();
	}

	private void rect(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
		content.setStrokingColor(color);
		content.addRect(x, y, width, height);
		content.stroke();
	}

	private void fill(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
		content.setNonStrokingColor(color);
		content.addRect(x, y, width, height);
		content.fill();
		content.setNonStrokingColor(Color.BLACK);
	}

	private String nombreArchivo(ComprobanteEmitido comprobante) {
		return "factura-" + numeroCompleto(comprobante).replace("-", "") + "-" + comprobante.getId() + ".pdf";
	}

	private String numeroCompleto(ComprobanteEmitido comprobante) {
		return String.format("%04d-%08d", comprobante.getPuntoVenta(), comprobante.getNumeroComprobante());
	}

	private String tituloComprobante(ComprobanteEmitido comprobante) {
		return comprobante.getTipoComprobante().name().replace("_", " ");
	}

	private String letraComprobante(ComprobanteEmitido comprobante) {
		String name = comprobante.getTipoComprobante().name();
		if (name.endsWith("_A")) {
			return "A";
		}
		if (name.endsWith("_B")) {
			return "B";
		}
		if (name.endsWith("_C")) {
			return "C";
		}
		return "";
	}

	private String nombreCliente(DatosCliente cliente) {
		if (cliente == null) {
			return "";
		}
		if (cliente.getRazonSocial() != null && !cliente.getRazonSocial().isBlank()) {
			return cliente.getRazonSocial();
		}
		return (valor(cliente.getNombre()) + " " + valor(cliente.getApellido())).trim();
	}

	private String porcentajeIva(ComprobanteItem item) {
		if (item.getAlicuotaIva() == null) {
			return "";
		}
		return numero(item.getAlicuotaIva().getPorcentaje()) + "%";
	}

	private String moneda(BigDecimal value) {
		return "$ " + moneyFormat.format(orZero(value));
	}

	private String numero(BigDecimal value) {
		return orZero(value).stripTrailingZeros().toPlainString();
	}

	private BigDecimal orZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
	}

	private String fecha(java.time.LocalDate fecha) {
		return fecha == null ? "" : DATE_FORMATTER.format(fecha);
	}

	private String valor(Object value) {
		return value == null ? "" : value.toString().replace("_", " ");
	}

	private String cortar(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength - 3) + "...";
	}

	private String sanitizar(String value) {
		return value == null ? "" : value.replaceAll("[^\\p{Print}\\p{IsLatin}]", "?");
	}
}
