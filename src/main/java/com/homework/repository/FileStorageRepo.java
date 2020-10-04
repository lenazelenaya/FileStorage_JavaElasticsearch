package com.homework.repository;

import com.homework.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FileStorageRepo extends ElasticsearchRepository<File, String> {
    Page<File> findAllByNameContains(String query, Pageable pageable);

    Page<File> findAllByNameContainsAndTagsIn(String query, List<String> tags, Pageable pageable);

    Page<File> findAllByTagsIn(List<String> tags, Pageable pageable);
}
