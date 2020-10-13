package com.homework.dto;

import com.homework.model.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class FileDto {
    String id;
    String name;
    Integer size;
    List<String> tags;

    public static FileDto fromEntity(File file){
        var tags = file.getTags() == null ? null : Arrays.asList(file.getTags().split(" "));
        return FileDto.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .tags(tags)
                .build();
    }
}
