package com.logistics.rag.domain.model;

import java.util.List;

public record WaiverResult(
        String recommendation,   // WAIVE | UPHOLD | ESCALATE
        double confidence,
        String reasoning,
        List<WaiverPrecedent> precedents
) {
    public WaiverResult {
        precedents = List.copyOf(precedents);
    }

    public record WaiverPrecedent(
            String invoiceId,
            String decision,
            String reason,
            long penaltyDays,
            double penaltyAmountBrl
    ) {}
}
