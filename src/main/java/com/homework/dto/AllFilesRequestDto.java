package com.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllFilesRequestDto {
    List<String> tags;

    @Min(1)
    @NonNull
    int size = 10;

    int page = 0;

    String q;
}
