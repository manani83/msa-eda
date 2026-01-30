package com.example.hexagonal.domain.order;

public class Address {
    private final String zip;
    private final String line1;
    private final String line2;

    public Address(String zip, String line1, String line2) {
        if (zip == null || zip.isBlank()) {
            throw new IllegalArgumentException("zip must not be blank");
        }
        if (line1 == null || line1.isBlank()) {
            throw new IllegalArgumentException("line1 must not be blank");
        }
        this.zip = zip;
        this.line1 = line1;
        this.line2 = line2;
    }

    public String getZip() {
        return zip;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }
}
