package com.homework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Value
@AllArgsConstructor
public class FileCreateDto {
    @NotBlank(message = "Does not allow empty name!")
    String name;

    @NotNull(message = "Does not allow empty size!")
    @Positive(message = "Does not allow negative or zero size!")
    Integer size;

    List<String> tags;
}
