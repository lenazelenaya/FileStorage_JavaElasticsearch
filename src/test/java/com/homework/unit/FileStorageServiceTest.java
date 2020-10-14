package com.homework.unit;

import com.homework.dto.AllFilesRequestDto;
import com.homework.dto.FileCreateDto;
import com.homework.dto.FileDto;
import com.homework.dto.IDDto;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import com.homework.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileStorageServiceTest {
    private FileStorageRepo repository;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        this.repository = mock(FileStorageRepo.class);
        service = new FileStorageService(repository);
    }

    @Test
    void whenGetAll_thenReturnAll() {
        var request = new AllFilesRequestDto(null, 10, 0, null);
        var testFiles = new ArrayList<File>();
        testFiles.add(new File(UUID.randomUUID().toString(), "first", 123, null));
        testFiles.add(new File(UUID.randomUUID().toString(), "second", 123, null));

        when(repository.findAll(ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(testFiles));
        repository.saveAll(testFiles);

        var result = service.getAll(request);

        assertEquals(result.getPage().size(), testFiles.size());
    }

}
