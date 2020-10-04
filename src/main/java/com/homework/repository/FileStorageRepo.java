package com.homework.repository;

import com.homework.model.File;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileStorageRepo extends ElasticsearchRepository<File, String> {
    List<File> findByTags(List<String> tags, Pageable pageable);
}
