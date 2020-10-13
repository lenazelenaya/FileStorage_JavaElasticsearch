package com.homework.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "files")
public class File {
    @Id
    private String id;
    private String name;
    private Integer size;
    private String tags;
}
