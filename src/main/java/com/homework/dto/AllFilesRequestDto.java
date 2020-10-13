package com.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllFilesRequestDto {
    List<String> tags;

    int size = 10;

    int page = 0;

    String q;
}
