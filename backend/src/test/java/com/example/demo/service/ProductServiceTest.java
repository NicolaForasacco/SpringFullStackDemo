package com.example.demo.service;

import com.example.demo.dto.ProductDto;
import com.example.demo.dto.ProductListResponse;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("Cache HIT: dovrebbe restituire i dati da Redis senza interrogare PostgreSQL")
    void testGetAllProductsCacheHit() throws Exception {
        String cachedJson = "[{\"id\":1,\"name\":\"Mouse\",\"price\":25.0}]";
        List<ProductDto> expectedList = List.of(new ProductDto(1L, "Mouse", 25.0, "Desc"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("products:all")).thenReturn(cachedJson);
        when(objectMapper.readValue(eq(cachedJson), any(TypeReference.class))).thenReturn(expectedList);

        ProductListResponse response = productService.getAllProducts();

        assertNotNull(response);
        assertEquals("REDIS_CACHE", response.getSource());
        assertEquals(1, response.getProducts().size());

        // Verifica che il database PostgreSQL NON sia stato interrogato
        verify(productRepository, never()).findAll();
    }

    @Test
    @DisplayName("Cache MISS: dovrebbe interrogare PostgreSQL e salvare il risultato in Redis")
    void testGetAllProductsCacheMiss() throws Exception {
        Product entity = new Product("Tastiera", 80.0, "Meccanica");
        entity.setId(10L);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("products:all")).thenReturn(null); // Cache vuota!
        when(productRepository.findAll()).thenReturn(List.of(entity));
        when(objectMapper.writeValueAsString(any())).thenReturn("[{\"id\":10,\"name\":\"Tastiera\"}]");

        ProductListResponse response = productService.getAllProducts();

        assertNotNull(response);
        assertEquals("POSTGRESQL", response.getSource());
        assertEquals(1, response.getProducts().size());

        // Verifica che il DB sia stato interrogato e che il risultato sia stato salvato su Redis
        verify(productRepository, times(1)).findAll();
        verify(valueOperations, times(1)).set(eq("products:all"), anyString(), eq(Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("Create Product: dovrebbe salvare su PostgreSQL e invalidare la cache Redis")
    void testCreateProductEvictsCache() {
        ProductDto inputDto = new ProductDto(null, "Monitor", 300.0, "4K");
        Product savedEntity = new Product("Monitor", 300.0, "4K");
        savedEntity.setId(99L);

        when(productRepository.save(any(Product.class))).thenReturn(savedEntity);

        ProductDto created = productService.createProduct(inputDto);

        assertNotNull(created);
        assertEquals(99L, created.getId());

        // Verifica che la cache Redis sia stata invalidata (chiave cancellata)
        verify(redisTemplate, times(1)).delete("products:all");
    }
}
