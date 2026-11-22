package com.example.demo.service;

import com.example.demo.dto.ProductDto;
import com.example.demo.dto.ProductListResponse;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String CACHE_KEY = "products:all";

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository,
                          StringRedisTemplate redisTemplate,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Recupera tutti i prodotti:
     * 1. Controlla prima se sono presenti nella Cache Redis.
     * 2. Se presenti (Cache HIT), li restituisce istantaneamente in RAM.
     * 3. Se assenti (Cache MISS), interroga PostgreSQL, salva il risultato in Redis con scadenza 60s e lo restituisce.
     */
    public ProductListResponse getAllProducts() {
        long startTime = System.currentTimeMillis();

        try {
            // 1. Tenta la lettura da Redis
            String cachedJson = redisTemplate.opsForValue().get(CACHE_KEY);

            if (cachedJson != null && !cachedJson.isBlank()) {
                List<ProductDto> cachedList = objectMapper.readValue(cachedJson, new TypeReference<List<ProductDto>>() {});
                long elapsed = System.currentTimeMillis() - startTime;
                log.info(">>> [REDIS CACHE HIT] Prodotti recuperati da Redis in {} ms", elapsed);
                return new ProductListResponse(cachedList, "REDIS_CACHE", elapsed);
            }
        } catch (Exception e) {
            log.warn("Errore durante la lettura da Redis (fallback su Postgres): {}", e.getMessage());
        }

        // 2. Cache MISS: lettura dal Database PostgreSQL
        List<Product> entities = productRepository.findAll();
        List<ProductDto> dtos = entities.stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getDescription()))
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - startTime;
        log.info(">>> [POSTGRESQL QUERY] Prodotti recuperati dal DB in {} ms", elapsed);

        // 3. Salva su Redis con TTL (Time-To-Live) di 60 secondi
        try {
            String jsonToCache = objectMapper.writeValueAsString(dtos);
            redisTemplate.opsForValue().set(CACHE_KEY, jsonToCache, Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("Impossibile salvare in cache su Redis: {}", e.getMessage());
        }

        return new ProductListResponse(dtos, "POSTGRESQL", elapsed);
    }

    /**
     * Crea un nuovo prodotto nel database PostgreSQL e invalida la cache Redis.
     */
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product(dto.getName(), dto.getPrice(), dto.getDescription());
        Product saved = productRepository.save(product);

        // INVALIDAZIONE DELLA CACHE: cancelliamo la vecchia lista memorizzata in Redis
        // in modo che la prossima lettura ricarichi la lista aggiornata da PostgreSQL.
        try {
            redisTemplate.delete(CACHE_KEY);
            log.info(">>> [CACHE EVICTION] Cache Redis '{}' invalidata dopo inserimento", CACHE_KEY);
        } catch (Exception e) {
            log.warn("Errore invalidazione cache Redis: {}", e.getMessage());
        }

        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription());
    }

    /**
     * Permette di svuotare manualmente la cache Redis per testare la differenza di performance
     */
    public void clearCache() {
        redisTemplate.delete(CACHE_KEY);
        log.info(">>> [CACHE CLEARED] Cache Redis svuotata manualmente");
    }
}
