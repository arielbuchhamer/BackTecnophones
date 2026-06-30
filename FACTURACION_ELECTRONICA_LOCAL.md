# Facturacion electronica - decision de estructura local

Este archivo deja registrado el contexto acordado para implementar facturacion electronica en el backend de TecnoPhones. Esta ignorado por Git para que funcione como memoria tecnica local de trabajo y no se suba al repositorio.

## Objetivo

Implementar un modulo desacoplado para emitir comprobantes fiscales electronicos de Argentina usando:

- AFRelay como intermediario REST hacia ARCA/AFIP.
- OpenPDF para generar el PDF del comprobante.
- MongoDB para guardar el comprobante emitido, datos fiscales, respuesta de AFRelay, estado e informacion necesaria para descargar el PDF luego.

El modulo no debe depender directamente de Venta ni de Mercado Pago. Debe trabajar a partir de una clase/modelo de entrada completa, de modo que la fuente pueda ser Postman, formulario, venta local, venta web o cualquier otro flujo futuro.

## Decision principal

No nombrar el modulo solamente como Factura, porque fiscalmente se puede expandir a otros comprobantes:

- Factura A, B, C
- Nota de credito A, B, C
- Nota de debito A, B, C
- Recibos u otros comprobantes alcanzados si luego aplica

Por eso la abstraccion base sera Comprobante.

Nombres recomendados:

- ComprobanteController
- ComprobanteService
- ComprobantePdfService
- ComprobanteFiscalValidator
- NumeracionComprobanteService
- AfRelayClient
- ComprobanteSolicitud
- ComprobanteEmitido
- TipoComprobante
- EstadoComprobante

Se puede ubicar bajo paquete funcional:

```text
com.BackTecnophones.facturacion
```

o, si se prefiere mas explicito:

```text
com.BackTecnophones.comprobantes
```

Recomendacion: usar `facturacion` como paquete y `Comprobante` como nombre de las clases principales.

## Estructura propuesta

```text
src/main/java/com/BackTecnophones
├── controller
│   └── ComprobanteController.java
├── service
│   ├── ComprobanteService.java
│   ├── ComprobantePdfService.java
│   ├── ComprobanteQrService.java
│   ├── ComprobanteFiscalValidator.java
│   └── NumeracionComprobanteService.java
├── client
│   └── AfRelayClient.java
├── config
│   ├── AfRelayProperties.java
│   └── RestClientConfig.java
├── exception
│   ├── ComprobanteException.java
│   ├── AfRelayException.java
│   └── GlobalExceptionHandler.java
├── model
│   └── facturacion
│       ├── ComprobanteSolicitud.java
│       ├── ComprobanteEmitido.java
│       ├── ComprobanteItem.java
│       ├── ComprobanteImportes.java
│       ├── ComprobanteAsociado.java
│       ├── DatosEmisor.java
│       ├── DatosCliente.java
│       ├── DatosFiscales.java
│       ├── AfRelayRespuesta.java
│       ├── EstadoComprobante.java
│       ├── TipoComprobante.java
│       ├── CondicionIva.java
│       ├── TipoDocumento.java
│       └── AlicuotaIva.java
└── repository
    └── ComprobanteEmitidoRepository.java
```

## Modelo de entrada

`ComprobanteSolicitud` representa todo lo necesario para emitir un comprobante. No se lo llama DTO porque el proyecto prefiere trabajar con modelos propios directamente.

Campos sugeridos:

```java
public class ComprobanteSolicitud {
    private String idempotencyKey;

    private DatosEmisor emisor;
    private DatosCliente cliente;

    private TipoComprobante tipoComprobante;
    private Integer puntoVenta;
    private Integer concepto;

    private LocalDate fecha;
    private String moneda;
    private BigDecimal cotizacion;

    private List<ComprobanteItem> items;
    private ComprobanteImportes importes;

    private List<ComprobanteAsociado> comprobantesAsociados;
}
```

Notas:

- `idempotencyKey` es obligatorio para evitar doble emision.
- `comprobantesAsociados` se usa especialmente para notas de credito/debito.
- Para la primera etapa se puede implementar solo Factura B o Factura C, pero dejando la estructura preparada para crecer.

## Modelo persistido

`ComprobanteEmitido` guarda lo que efectivamente quedo emitido o intentado.

```java
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
    private String error;

    private Instant fechaCreacion;
    private Instant fechaAutorizacion;
}
```

Estados sugeridos:

```java
public enum EstadoComprobante {
    RECIBIDO,
    VALIDADO,
    AUTORIZADO,
    PDF_GENERADO,
    ERROR
}
```

## TipoComprobante

Usar enum con codigo ARCA/AFIP:

```java
public enum TipoComprobante {
    FACTURA_A(1),
    NOTA_DEBITO_A(2),
    NOTA_CREDITO_A(3),
    FACTURA_B(6),
    NOTA_DEBITO_B(7),
    NOTA_CREDITO_B(8),
    FACTURA_C(11),
    NOTA_DEBITO_C(12),
    NOTA_CREDITO_C(13);

    private final int codigoArca;
}
```

## Flujo general

```text
POST /comprobantes
        ↓
ComprobanteController
        ↓
ComprobanteService.generarComprobante(solicitud)
        ↓
ComprobanteFiscalValidator valida campos minimos y reglas por tipo
        ↓
buscar por idempotencyKey
        ↓
si ya existe AUTORIZADO/PDF_GENERADO, devolver el existente
        ↓
NumeracionComprobanteService consulta ultimo autorizado en AFRelay
        ↓
ComprobanteService arma request FECAESolicitar
        ↓
AfRelayClient envia a /wsfe/FECAESolicitar
        ↓
procesar respuesta fiscal: CAE, vencimiento, numero, resultado
        ↓
ComprobanteQrService arma QR fiscal
        ↓
ComprobantePdfService genera PDF con OpenPDF
        ↓
guardar ComprobanteEmitido
        ↓
devolver metadata o PDF
```

## Endpoints recomendados

```http
POST /comprobantes
```

Emite el comprobante y devuelve metadata.

```http
POST /comprobantes/pdf
```

Emite el comprobante y devuelve el PDF directamente como descarga.

```http
GET /comprobantes/{id}
```

Consulta un comprobante emitido.

```http
GET /comprobantes/{id}/pdf
```

Descarga el PDF ya generado.

## AFRelay

AFRelay corre como microservicio externo, normalmente:

```text
http://localhost:8000
```

Endpoints principales a consumir:

```text
POST /wsfe/FECompUltimoAutorizado
POST /wsfe/FECAESolicitar
POST /wsfe/FECompConsultar
GET  /health/readiness
GET  /health/liveness
```

El JSON exacto del request debe verificarse contra Swagger/OpenAPI de AFRelay en:

```text
http://localhost:8000/docs
```

Decision: encapsular todo AFRelay en `AfRelayClient`. Ningun controller o service de negocio deberia conocer detalles HTTP del intermediario.

## Configuracion sugerida

Agregar propiedades:

```properties
afrelay.base-url=http://localhost:8000
facturacion.pdf-dir=./data/comprobantes
facturacion.logo-path=./data/logo.png
```

`AfRelayProperties` deberia leer `afrelay.base-url`.

## Dependencias sugeridas

OpenPDF:

```xml
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>2.0.3</version>
</dependency>
```

QR:

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
```

## Validaciones minimas

- `idempotencyKey` obligatorio.
- `emisor.cuit` obligatorio.
- `cliente` obligatorio.
- `tipoComprobante` obligatorio.
- `puntoVenta` mayor que cero.
- `fecha` obligatoria.
- `items` no vacio.
- cantidades mayores a cero.
- precios unitarios mayores o iguales a cero.
- total mayor a cero.
- importes coherentes: neto + iva + tributos + exento + no gravado = total.
- si el comprobante es nota de credito/debito, exigir `comprobantesAsociados`.
- reglas por condicion de IVA del emisor y receptor.

## Idempotencia y prevencion de doble emision

Punto critico del modulo.

Reglas:

1. Toda solicitud debe traer `idempotencyKey`.
2. Mongo debe tener indice unico por `idempotencyKey`.
3. Si llega la misma clave y ya existe comprobante autorizado, devolver el existente.
4. Si AFRelay o ARCA responde timeout, no repetir ciegamente `FECAESolicitar`.
5. Ante duda, consultar `FECompConsultar` o `FECompUltimoAutorizado`.
6. Guardar request y response completos de AFRelay para auditoria.
7. El PDF debe generarse a partir del comprobante autorizado, no antes.

Ejemplos de claves:

```text
POSTMAN-FACTURA-TEST-0001
VENTA-65fabc123
LOCAL-CAJA1-20260426-000001
```

## Integracion futura con Venta

No acoplar `ComprobanteService` a `VentaService`.

Cuando se integre:

```text
Venta aprobada
    ↓
armar ComprobanteSolicitud desde Venta
    ↓
ComprobanteService.generarComprobante(solicitud)
    ↓
guardar comprobanteId en Venta si hace falta
```

Esto permite que el modulo tambien funcione desde Postman o formulario manual.

## Complejidad estimada

Primera etapa:

- Emitir manualmente con Postman.
- Pedir CAE via AFRelay.
- Guardar comprobante emitido.
- Generar PDF.
- Descargar PDF.

Complejidad: media.

Etapa productiva:

- Manejo robusto de numeracion.
- Reintentos seguros.
- Consulta de comprobantes.
- Notas de credito/debito.
- Auditoria.
- Integracion con ventas reales.
- Manejo de contingencias.

Complejidad: media/alta.

## Decision de implementacion inicial

Implementar primero:

- `ComprobanteController`
- `ComprobanteService`
- `AfRelayClient`
- `ComprobantePdfService`
- modelos base
- repositorio Mongo
- validaciones minimas
- endpoint manual para Postman

Tipo inicial recomendado:

- `FACTURA_B` si el negocio es Responsable Inscripto vendiendo a consumidor final.
- `FACTURA_C` si el negocio es Monotributista.

La condicion fiscal real del negocio define el tipo correcto.
