package com.homework.dto;

import com.homework.model.File;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AllFilesDto {
    int total;
    List<FileDto> page;
}
