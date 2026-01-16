package com.back.product.controller;

import com.back.security.config.BaseSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(BaseSecurityConfig.class)
public class BrandControllerTests {
    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/products/brands - 브랜드 목록 조회")
    @WithMockUser
    void getBrands() throws Exception {
        mockMvc.perform(
                get("/api/v1/products/brands")
                        .contentType("application/json")
        ).andDo(print())
        .andExpect(status().isOk());
    }
}
