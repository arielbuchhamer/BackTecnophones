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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
	private static final float CONTENT_WIDTH = 511;
	private static final float LINE_HEIGHT = 16;
	private static final float TABLE_ROW_HEIGHT = 22;
	private static final float TOP_BAR_HEIGHT = 6;
	private static final float LOGO_HEIGHT = 30;

	private static final Color TEXT_DARK = new Color(35, 35, 40);
	private static final Color TEXT_MUTED = new Color(120, 120, 128);
	private static final Color LINE_LIGHT = new Color(228, 228, 232);
	private static final Color ROW_TINT = new Color(248, 248, 250);

	private final Path pdfDir;
	private final DecimalFormat moneyFormat;
	private final byte[] logoBytes;
	private final Color colorAcento;
	private final Color colorAcentoSuave;

	public ComprobantePdfService(
			@Value("${facturacion.pdf-dir:comprobantes-pdf}") String pdfDir,
			@Value("${facturacion.pdf.logo-path:classpath:branding/logo.jpg}") Resource logoResource,
			@Value("${facturacion.pdf.color-acento:#4F46E5}") String colorAcentoHex) {
		this.pdfDir = Path.of(pdfDir).toAbsolutePath().normalize();
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
		this.moneyFormat = new DecimalFormat("#,##0.00", symbols);
		this.logoBytes = cargarLogo(logoResource);
		this.colorAcento = parsearColor(colorAcentoHex);
		this.colorAcentoSuave = suavizar(this.colorAcento, 0.92f);
	}

	public String generarPdf(ComprobanteEmitido comprobante) {
		try {
			Files.createDirectories(pdfDir);
			Path path = pdfDir.resolve(nombreArchivo(comprobante)).normalize();

			try (PDDocument document = new PDDocument()) {
				PDPage page = new PDPage(PDRectangle.A4);
				document.addPage(page);
				PDImageXObject logo = cargarLogoEnDocumento(document);

				try (PDPageContentStream content = new PDPageContentStream(document, page)) {
					float pageWidth = page.getMediaBox().getWidth();
					float y = page.getMediaBox().getHeight();

					fill(content, 0, y - TOP_BAR_HEIGHT, pageWidth, TOP_BAR_HEIGHT, colorAcento);
					y -= TOP_BAR_HEIGHT + MARGIN;

					y = dibujarEncabezado(content, comprobante, logo, y);
					y = dibujarCliente(content, comprobante.getSolicitud(), y - 16);
					y = dibujarItems(content, comprobante.getSolicitud(), y - 16);
					y = dibujarTotales(content, comprobante.getSolicitud().getImportes(), y - 12);
					dibujarDatosFiscales(content, comprobante.getDatosFiscales(), y - 18);
				}

				document.save(path.toFile());
			}

			return path.toString();
		} catch (IOException e) {
			throw new ComprobanteException("No se pudo generar el PDF del comprobante", e);
		}
	}

	private float dibujarEncabezado(PDPageContentStream content, ComprobanteEmitido comprobante, PDImageXObject logo, float y) throws IOException {
		ComprobanteSolicitud solicitud = comprobante.getSolicitud();
		DatosEmisor emisor = solicitud.getEmisor();
		float logoBottom = y;

		if (logo != null) {
			float scale = LOGO_HEIGHT / logo.getHeight();
			float logoWidth = logo.getWidth() * scale;
			content.drawImage(logo, MARGIN, y - LOGO_HEIGHT, logoWidth, LOGO_HEIGHT);
			logoBottom = y - LOGO_HEIGHT - 14;
		} else {
			text(content, "TecnoPhones", MARGIN, y - 16, PDType1Font.HELVETICA_BOLD, 18, TEXT_DARK);
			logoBottom = y - 34;
		}

		text(content, valor(emisor.getRazonSocial()), MARGIN, logoBottom, PDType1Font.HELVETICA, 9, TEXT_MUTED);
		text(content, datosEmisorLinea(emisor), MARGIN, logoBottom - 14, PDType1Font.HELVETICA, 9, TEXT_MUTED);

		String letra = letraComprobante(comprobante);
		float badgeSize = 34;
		float badgeX = 300;
		fill(content, badgeX, y - badgeSize + 6, badgeSize, badgeSize, colorAcento);
		textCentrada(content, letra, badgeX, badgeX + badgeSize, y - badgeSize + 16, PDType1Font.HELVETICA_BOLD, 20, Color.WHITE);
		text(content, "COD. " + comprobante.getTipoComprobante().getCodigoArca(), badgeX, y - badgeSize - 6, PDType1Font.HELVETICA, 8, TEXT_MUTED);

		float infoX = 366;
		text(content, tituloComprobante(comprobante), infoX, y - 12, PDType1Font.HELVETICA_BOLD, 14, TEXT_DARK);
		text(content, "Nro: " + numeroCompleto(comprobante), infoX, y - 30, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, "Fecha: " + fecha(solicitud.getFecha()), infoX, y - 44, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, "IVA: " + valor(emisor.getCondicionIva()), infoX, y - 58, PDType1Font.HELVETICA, 10, TEXT_MUTED);

		float bottom = Math.min(logoBottom - 22, y - badgeSize - 20);
		linea(content, MARGIN, bottom, MARGIN + CONTENT_WIDTH, bottom, LINE_LIGHT, 1);
		return bottom;
	}

	private float dibujarCliente(PDPageContentStream content, ComprobanteSolicitud solicitud, float y) throws IOException {
		DatosCliente cliente = solicitud.getCliente();
		text(content, "CLIENTE", MARGIN, y, PDType1Font.HELVETICA_BOLD, 9, colorAcento);

		float rowY = y - 20;
		text(content, nombreCliente(cliente), MARGIN, rowY, PDType1Font.HELVETICA, 10, TEXT_DARK);
		rowY -= 16;
		text(content, valor(cliente.getTipoDocumento()) + " " + valor(cliente.getNumeroDocumento()), MARGIN, rowY, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, "Cond. IVA: " + valor(cliente.getCondicionIva()), 310, rowY, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		rowY -= 16;
		text(content, valor(cliente.getDomicilio()), MARGIN, rowY, PDType1Font.HELVETICA, 10, TEXT_MUTED);

		float bottom = rowY - 14;
		linea(content, MARGIN, bottom, MARGIN + CONTENT_WIDTH, bottom, LINE_LIGHT, 1);
		return bottom;
	}

	private float dibujarItems(PDPageContentStream content, ComprobanteSolicitud solicitud, float y) throws IOException {
		float tableX = MARGIN;
		float descX = tableX + 8;
		float qtyX = tableX + 285;
		float priceX = tableX + 345;
		float ivaX = tableX + 410;
		float totalX = tableX + 453;

		fill(content, tableX, y - TABLE_ROW_HEIGHT, CONTENT_WIDTH, TABLE_ROW_HEIGHT, colorAcentoSuave);
		text(content, "DESCRIPCION", descX, y - 15, PDType1Font.HELVETICA_BOLD, 8, colorAcento);
		text(content, "CANT.", qtyX, y - 15, PDType1Font.HELVETICA_BOLD, 8, colorAcento);
		text(content, "UNIT.", priceX, y - 15, PDType1Font.HELVETICA_BOLD, 8, colorAcento);
		text(content, "IVA", ivaX, y - 15, PDType1Font.HELVETICA_BOLD, 8, colorAcento);
		text(content, "SUBTOTAL", totalX, y - 15, PDType1Font.HELVETICA_BOLD, 8, colorAcento);

		y -= TABLE_ROW_HEIGHT;
		boolean alterna = false;
		for (ComprobanteItem item : solicitud.getItems()) {
			if (alterna) {
				fill(content, tableX, y - TABLE_ROW_HEIGHT, CONTENT_WIDTH, TABLE_ROW_HEIGHT, ROW_TINT);
			}
			text(content, cortar(valor(item.getDescripcion()), 48), descX, y - 15, PDType1Font.HELVETICA, 9, TEXT_DARK);
			text(content, numero(item.getCantidad()), qtyX, y - 15, PDType1Font.HELVETICA, 9, TEXT_DARK);
			text(content, moneda(item.getPrecioUnitario()), priceX, y - 15, PDType1Font.HELVETICA, 9, TEXT_DARK);
			text(content, porcentajeIva(item), ivaX, y - 15, PDType1Font.HELVETICA, 9, TEXT_DARK);
			text(content, moneda(item.getSubtotal()), totalX, y - 15, PDType1Font.HELVETICA, 9, TEXT_DARK);
			linea(content, tableX, y - TABLE_ROW_HEIGHT, tableX + CONTENT_WIDTH, y - TABLE_ROW_HEIGHT, LINE_LIGHT, 0.5f);
			y -= TABLE_ROW_HEIGHT;
			alterna = !alterna;
		}

		return y;
	}

	private float dibujarTotales(PDPageContentStream content, ComprobanteImportes importes, float y) throws IOException {
		float xLabel = 380;
		float xValue = 470;
		text(content, "Neto:", xLabel, y, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, moneda(importes.getNeto()), xValue, y, PDType1Font.HELVETICA, 10, TEXT_DARK);
		text(content, "IVA:", xLabel, y - LINE_HEIGHT, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, moneda(importes.getIva()), xValue, y - LINE_HEIGHT, PDType1Font.HELVETICA, 10, TEXT_DARK);
		text(content, "Tributos:", xLabel, y - LINE_HEIGHT * 2, PDType1Font.HELVETICA, 10, TEXT_MUTED);
		text(content, moneda(importes.getTributos()), xValue, y - LINE_HEIGHT * 2, PDType1Font.HELVETICA, 10, TEXT_DARK);

		float lineY = y - LINE_HEIGHT * 2 - 10;
		linea(content, xLabel, lineY, xLabel + 131, lineY, colorAcento, 1.4f);
		float totalY = lineY - 20;
		text(content, "Total:", xLabel, totalY, PDType1Font.HELVETICA_BOLD, 13, TEXT_DARK);
		text(content, moneda(importes.getTotal()), xValue, totalY, PDType1Font.HELVETICA_BOLD, 13, colorAcento);
		return totalY - 16;
	}

	private void dibujarDatosFiscales(PDPageContentStream content, DatosFiscales datos, float y) throws IOException {
		float height = 54;
		fill(content, MARGIN, y - height, CONTENT_WIDTH, height, colorAcentoSuave);
		fill(content, MARGIN, y - height, 4, height, colorAcento);
		text(content, "Comprobante autorizado por ARCA", MARGIN + 16, y - 20, PDType1Font.HELVETICA_BOLD, 10, colorAcento);
		text(content, "CAE: " + valor(datos == null ? null : datos.getCae()), MARGIN + 16, y - 38, PDType1Font.HELVETICA, 10, TEXT_DARK);
		text(content, "Vto. CAE: " + fecha(datos == null ? null : datos.getCaeVencimiento()), 310, y - 38, PDType1Font.HELVETICA, 10, TEXT_DARK);
	}

	private void text(PDPageContentStream content, String text, float x, float y, PDType1Font font, int size, Color color) throws IOException {
		content.beginText();
		content.setNonStrokingColor(color);
		content.setFont(font, size);
		content.newLineAtOffset(x, y);
		content.showText(sanitizar(text));
		content.endText();
		content.setNonStrokingColor(Color.BLACK);
	}

	private void textCentrada(PDPageContentStream content, String text, float xDesde, float xHasta, float y, PDType1Font font, int size, Color color) throws IOException {
		String sanitizado = sanitizar(text);
		float textWidth = font.getStringWidth(sanitizado) / 1000 * size;
		float x = xDesde + ((xHasta - xDesde) - textWidth) / 2;
		text(content, text, x, y, font, size, color);
	}

	private void linea(PDPageContentStream content, float x1, float y1, float x2, float y2, Color color, float width) throws IOException {
		content.setStrokingColor(color);
		content.setLineWidth(width);
		content.moveTo(x1, y1);
		content.lineTo(x2, y2);
		content.stroke();
	}

	private void fill(PDPageContentStream content, float x, float y, float width, float height, Color color) throws IOException {
		content.setNonStrokingColor(color);
		content.addRect(x, y, width, height);
		content.fill();
		content.setNonStrokingColor(Color.BLACK);
	}

	private byte[] cargarLogo(Resource logoResource) {
		if (logoResource == null || !logoResource.exists()) {
			return null;
		}
		try {
			return logoResource.getInputStream().readAllBytes();
		} catch (IOException e) {
			return null;
		}
	}

	private PDImageXObject cargarLogoEnDocumento(PDDocument document) throws IOException {
		if (logoBytes == null) {
			return null;
		}
		return PDImageXObject.createFromByteArray(document, logoBytes, "logo");
	}

	private Color parsearColor(String hex) {
		try {
			return Color.decode(hex);
		} catch (NumberFormatException e) {
			return new Color(79, 70, 229);
		}
	}

	private Color suavizar(Color base, float haciaBlanco) {
		int r = (int) (base.getRed() + (255 - base.getRed()) * haciaBlanco);
		int g = (int) (base.getGreen() + (255 - base.getGreen()) * haciaBlanco);
		int b = (int) (base.getBlue() + (255 - base.getBlue()) * haciaBlanco);
		return new Color(r, g, b);
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

	private String datosEmisorLinea(DatosEmisor emisor) {
		String linea = "CUIT " + valor(emisor.getCuit());
		if (emisor.getDomicilio() != null && !emisor.getDomicilio().isBlank()) {
			linea += "  -  " + emisor.getDomicilio();
		}
		return linea;
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
