package com.homework.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homework.config.ElasticsearchConfig;
import com.homework.controller.FileStorageController;
import com.homework.dto.*;
import com.homework.exceptions.NotFoundException;
import com.homework.service.FileStorageService;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(FileStorageController.class)
@ContextConfiguration(classes = ElasticsearchConfig.class)
public class HomeWorkControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void whenUploadWithPositiveSize_thenReturnValidResponse() throws Exception {
        String testName = "fileName";
        int testPositiveSize = 123123;
        var dto = new FileCreateDto(testName, testPositiveSize, null);
        var testId = UUID.randomUUID().toString();

        when(fileStorageService.upload(dto)).thenReturn(new IDDto(testId));

        this.mockMvc
                .perform(post("/file")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id").value(testId));
    }

    @Test
    void whenUploadWithNegativeSize_thenReturnErrorResponse() throws Exception {
        String testName = "fileName";
        int testNegativeSize = -123123;

        var dto = new FileCreateDto(testName, testNegativeSize, null);

        this.mockMvc
                .perform(post("/file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error").isString());
    }

    @Test
    void whenDeleteById_thenReturnValidResponse() throws Exception {
        var testId = UUID.randomUUID().toString();

        this.mockMvc
                .perform(delete("/file/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.success").value(true));

    }

    @Test
    void whenGetAll_thenReturnValidResponse() throws Exception{
        var testTotal = 1;
        var testObject = new AllFilesRequestDto(null, 10, 0, null);
        var testID = UUID.randomUUID().toString();
        var testName = "fileName";
        var testSize = 123;
        var testFile = new FileDto(testID, testName, testSize, null);

        when(fileStorageService.getAll(testObject))
                .thenReturn(new AllFilesResponseDto(testTotal, Collections.singletonList(testFile)));

        this.mockMvc
                .perform(get("/file"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.total").value(testTotal))
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.page").isArray())
                .andExpect(jsonPath("$.page[0].name").isString())
                .andExpect(jsonPath("$.page[0].name").value(testName))
                .andExpect(jsonPath("$.page[0].id").value(testID));
    }
}
