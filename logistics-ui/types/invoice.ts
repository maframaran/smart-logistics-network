export type InvoiceStatus = "PENDING" | "PAID" | "OVERDUE" | "CANCELLED";

export type SlaType = "STANDARD" | "PRIORITY" | "EXPRESS";

export interface InvoiceLineItem {
  description: string;
  amount: number;
}

export interface CarrierPayment {
  carrierId: string;
  amount: number;
  status: string;
}

export interface Invoice {
  invoiceId: string;
  shipmentId: string;
  shipperId: string;
  carrierId: string;
  slaType: SlaType;
  totalAmount: number;
  slaPenaltyAmount?: number;
  status: InvoiceStatus;
  issuedAt: string;
  dueDate: string;
  paidAt?: string;
  lineItems?: InvoiceLineItem[];
  carrierPayment?: CarrierPayment;
}

export interface InvoiceListItem {
  invoiceId: string;
  shipmentId: string;
  slaType: SlaType;
  totalAmount: number;
  slaPenaltyAmount?: number;
  status: InvoiceStatus;
  issuedAt: string;
  dueDate: string;
}
