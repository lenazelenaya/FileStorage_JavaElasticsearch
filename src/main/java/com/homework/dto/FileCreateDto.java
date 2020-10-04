package com.homework.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.homework.serializers.Deserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Data
@Value
@AllArgsConstructor
@JsonDeserialize(using = Deserializer.class)
public class FileCreateDto {
    String name;
    byte[] size;
    List<String> tags;
}
