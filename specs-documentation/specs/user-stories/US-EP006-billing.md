# User Stories — EP-006 Billing

## Shipper Stories

**US-035** As a Shipper, I want to receive an invoice automatically after my shipment is delivered so that I can process payment without manual follow-up.
_Priority: Must | Feature: F-018_

**US-036** As a Shipper, I want the invoice to show an itemized breakdown (base cost, SLA penalty if any, fees) so that I can audit charges.
_Priority: Must | Feature: F-018, F-019_

**US-037** As a Shipper, I want to understand exactly how much I am penalized for a late delivery and why so that I can evaluate my SLA choices.
_Priority: Should | Feature: F-019_

## Carrier Stories

**US-038** As a Carrier, I want to receive payment automatically after delivery is confirmed so that my cash flow is predictable.
_Priority: Must | Feature: F-020_

**US-039** As a Carrier, I want to see the payment amount before it is transferred, including the platform commission deduction, so that I can verify it is correct.
_Priority: Should | Feature: F-020_

## Platform Administrator Stories

**US-040** As a Platform Administrator, I want to configure the platform commission rate so that the business model can be adjusted without code changes.
_Priority: Must | Feature: F-020_

**US-041** As a Platform Administrator, I want to waive an SLA penalty for a specific shipment (e.g. due to force majeure) so that the platform can handle exceptional cases fairly.
_Priority: Should | Feature: F-019_
