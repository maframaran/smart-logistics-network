package com.logistics.shipment.domain.model;

public record Address(
        String street,
        String city,
        String state,
        String postalCode,
        String country,
        double latitude,
        double longitude
) {
    public Address {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("street must not be blank");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("city must not be blank");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("country must not be blank");
    }
}
