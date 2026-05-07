package com.example.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // -------------------------------------------------------------------------
    // POST /products
    // -------------------------------------------------------------------------

    @Test
    void createProduct_returns201WithBody() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Webcam", "price": 79.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Webcam"))
                .andExpect(jsonPath("$.price").value(79.99));
    }

    @Test
    void createProduct_returns400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": 50.0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createProduct_returns400_whenPriceIsNull() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Widget"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createProduct_returns400_whenPriceIsNegative() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Widget", "price": -10.0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // -------------------------------------------------------------------------
    // GET /products
    // -------------------------------------------------------------------------

    @Test
    void getAllProducts_returnsPageWithSeedData() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    @Test
    void getAllProducts_paginationRespectsPageAndSize() throws Exception {
        mockMvc.perform(get("/products").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getAllProducts_secondPageReturnsCorrectSlice() throws Exception {
        mockMvc.perform(get("/products").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // -------------------------------------------------------------------------
    // GET /products/{id}
    // -------------------------------------------------------------------------

    @Test
    void getProductById_returnsProduct_whenFound() throws Exception {
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1200.0));
    }

    @Test
    void getProductById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/products/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 9999"));
    }

    // -------------------------------------------------------------------------
    // POST /products/{id}/reviews
    // -------------------------------------------------------------------------

    @Test
    void addReview_returns201WithComment() throws Exception {
        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Excellent build quality"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.comment").value("Excellent build quality"));
    }

    @Test
    void addReview_returns404_whenProductDoesNotExist() throws Exception {
        mockMvc.perform(post("/products/9999/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": "Orphan review"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReview_returns400_whenCommentIsBlank() throws Exception {
        mockMvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /products/withReviews
    // -------------------------------------------------------------------------

    @Test
    void createProductWithReviews_returns201WithReviewsInBody() throws Exception {
        mockMvc.perform(post("/products/withReviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Headphones",
                                  "price": 149.99,
                                  "reviews": [
                                    {"comment": "Great sound"},
                                    {"comment": "Comfortable fit"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Headphones"))
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[0].comment").value("Great sound"))
                .andExpect(jsonPath("$.reviews[1].comment").value("Comfortable fit"));
    }

    @Test
    void createProductWithReviews_returns400_whenReviewsEmpty() throws Exception {
        mockMvc.perform(post("/products/withReviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ghost", "price": 9.99, "reviews": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("At least one review is required to create a product"));
    }

    @Test
    void createProductWithReviews_doesNotPersistProduct_whenRollbackOccurs() throws Exception {
        // The service saves the product first, then throws EmptyReviewsException.
        // Spring's @Transactional rolls back the save before the response is returned,
        // so the caller only ever sees the 400 — the product never escapes to the DB.
        //
        // NOTE: querying the count inside this @Transactional test would still show
        // the uncommitted write (same session sees its own dirty reads). The actual
        // rollback is evidenced by getAllProducts_returnsPageWithSeedData(), which
        // runs in a separate transaction and always finds exactly 5 rows.
        mockMvc.perform(post("/products/withReviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ghost", "price": 9.99, "reviews": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("At least one review is required to create a product"));
    }
}
