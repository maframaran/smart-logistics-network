package com.logistics.billing.infrastructure.rest;

import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.in.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Invoices", description = "Invoicing, SLA penalties, and carrier payments")
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final GenerateInvoiceUseCase generateInvoice;
    private final PayInvoiceUseCase payInvoice;
    private final GetInvoiceUseCase getInvoice;

    public InvoiceController(GenerateInvoiceUseCase generateInvoice, PayInvoiceUseCase payInvoice,
                             GetInvoiceUseCase getInvoice) {
        this.generateInvoice = generateInvoice;
        this.payInvoice = payInvoice;
        this.getInvoice = getInvoice;
    }

    @Operation(summary = "Generate an invoice", description = "Computes SLA penalty (BRL 50/150/300/day) and raises InvoiceGenerated.")
    @ApiResponse(responseCode = "201", description = "Invoice generated")
    @PostMapping
    public ResponseEntity<InvoiceResponse> generate(@RequestBody GenerateInvoiceRequest request) {
        InvoiceId id = generateInvoice.generate(new GenerateInvoiceUseCase.Command(
                request.shipmentId(), request.shipperId(), request.carrierId(),
                Money.brl(request.baseAmountBrl()),
                SlaType.valueOf(request.slaType()),
                request.promisedDeliveryDate(),
                request.actualDeliveryDate(),
                request.dueDate()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).body(new InvoiceResponse(id.toString(), "PENDING"));
    }

    @Operation(summary = "Get an invoice by ID")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> get(@PathVariable String id) {
        Invoice invoice = getInvoice.findById(InvoiceId.of(id));
        return ResponseEntity.ok(toDetail(invoice));
    }

    @Operation(summary = "List invoices", description = "Optionally filter by status or shipmentId.")
    @GetMapping
    public ResponseEntity<List<InvoiceDetailResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String shipmentId
    ) {
        if (shipmentId != null) {
            return getInvoice.findByShipmentId(shipmentId)
                    .map(i -> ResponseEntity.ok(List.of(toDetail(i))))
                    .orElse(ResponseEntity.ok(List.of()));
        }
        List<Invoice> invoices = status != null
                ? getInvoice.findByStatus(InvoiceStatus.valueOf(status))
                : getInvoice.findAll();
        return ResponseEntity.ok(invoices.stream().map(this::toDetail).toList());
    }

    @Operation(summary = "Mark an invoice as paid")
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> pay(@PathVariable String id) {
        payInvoice.pay(InvoiceId.of(id));
        return ResponseEntity.noContent().build();
    }

    private InvoiceDetailResponse toDetail(Invoice i) {
        return new InvoiceDetailResponse(
                i.getId().toString(), i.getShipmentId(), i.getShipperId(), i.getCarrierId(),
                i.getBaseAmount().amount(), i.getSlaPenalty().penaltyAmount().amount(),
                i.getTotalAmount().amount(), i.getDueDate(), i.getDueDate(), i.getStatus().name());
    }

    record GenerateInvoiceRequest(String shipmentId, String shipperId, String carrierId,
                                   double baseAmountBrl, String slaType,
                                   LocalDate promisedDeliveryDate, LocalDate actualDeliveryDate, LocalDate dueDate) {}
    record InvoiceResponse(String invoiceId, String status) {}
    record InvoiceDetailResponse(String invoiceId, String shipmentId, String shipperId, String carrierId,
                                  BigDecimal baseAmount, BigDecimal slaPenaltyAmount,
                                  BigDecimal totalAmount, LocalDate issuedAt, LocalDate dueDate, String status) {}
}
