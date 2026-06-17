package com.logistics.billing.infrastructure.rest;

import com.logistics.billing.domain.model.*;
import com.logistics.billing.domain.ports.in.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> get(@PathVariable String id) {
        Invoice invoice = getInvoice.findById(InvoiceId.of(id));
        return ResponseEntity.ok(toDetail(invoice));
    }

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
                : getInvoice.findByStatus(InvoiceStatus.PENDING);
        return ResponseEntity.ok(invoices.stream().map(this::toDetail).toList());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> pay(@PathVariable String id) {
        payInvoice.pay(InvoiceId.of(id));
        return ResponseEntity.noContent().build();
    }

    private InvoiceDetailResponse toDetail(Invoice i) {
        return new InvoiceDetailResponse(
                i.getId().toString(), i.getShipmentId(), i.getShipperId(), i.getCarrierId(),
                i.getBaseAmount().amount(), i.getSlaPenalty().daysLate(),
                i.getSlaPenalty().penaltyAmount().amount(), i.getTotalAmount().amount(),
                i.getDueDate(), i.getStatus().name());
    }

    record GenerateInvoiceRequest(String shipmentId, String shipperId, String carrierId,
                                   double baseAmountBrl, String slaType,
                                   LocalDate promisedDeliveryDate, LocalDate actualDeliveryDate, LocalDate dueDate) {}
    record InvoiceResponse(String invoiceId, String status) {}
    record InvoiceDetailResponse(String invoiceId, String shipmentId, String shipperId, String carrierId,
                                  BigDecimal baseAmount, long penaltyDaysLate, BigDecimal penaltyAmount,
                                  BigDecimal totalAmount, LocalDate dueDate, String status) {}
}
