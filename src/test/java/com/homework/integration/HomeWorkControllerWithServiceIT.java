package com.homework.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homework.config.ElasticsearchConfig;
import com.homework.controller.FileStorageController;
import com.homework.dto.FileCreateDto;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import com.homework.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ContextConfiguration(classes = ElasticsearchConfig.class)
@WebMvcTest(FileStorageController.class)
@Import(FileStorageService.class)
public class HomeWorkControllerWithServiceIT {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageRepo repository;

    @Test
    void whenUpload_thenReturnValidResponse() throws Exception {
        var testName = "fileName.png";
        int testSize = 123123;
        var testId = UUID.randomUUID().toString();
        var testFile = new FileCreateDto(testName, testSize, null);

        var mockFile = File.builder()
                .id(testId)
                .name(testName)
                .size(testSize)
                .tags("image")
                .build();

        when(repository.save(ArgumentMatchers.any(File.class))).thenReturn(mockFile);

        this.mockMvc
                .perform(post("/file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testFile))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id").value(testId));
    }

    @Test
    void whenDelete_thenReturnValidResponse() throws Exception {
        var testId = UUID.randomUUID().toString();
        this.mockMvc
                .perform(delete("/file/{id}", testId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").isString())
                .andExpect(jsonPath("$.error").value("There is no file with such id"));
    }



}
