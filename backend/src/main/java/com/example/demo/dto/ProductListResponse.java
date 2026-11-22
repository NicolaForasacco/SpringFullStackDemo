package com.example.demo.dto;

import java.util.List;

public class ProductListResponse {

    private List<ProductDto> products;
    private String source; // "REDIS_CACHE" oppure "POSTGRESQL"
    private long elapsedMs; // Tempo impiegato in millisecondi

    public ProductListResponse() {}

    public ProductListResponse(List<ProductDto> products, String source, long elapsedMs) {
        this.products = products;
        this.source = source;
        this.elapsedMs = elapsedMs;
    }

    public List<ProductDto> getProducts() { return products; }
    public void setProducts(List<ProductDto> products) { this.products = products; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }
}
