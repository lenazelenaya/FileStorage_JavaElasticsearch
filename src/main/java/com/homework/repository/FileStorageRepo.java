package com.homework.repository;

import com.homework.model.File;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FileStorageRepo extends ElasticsearchRepository<File, String> {
}
