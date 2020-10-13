package com.homework.repository;

import com.homework.model.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface FileStorageRepo extends ElasticsearchRepository<File, String> {
    Page<File> findAllByNameContains(String query, Pageable pageable);

    @Query("{ \"bool\" : { " +
                "\"must\" : [  " +
                        "{ \"wildcard\" : { \"name\" : \"*?0*\" }  }, " +
                        "{ \"match\" : { \"tags\" : { \"query\" : \"?1\", \"operator\": \"and\" } } }" +
                    "] " +
                "} " +
            "}")
    Page<File> findAllByNameContainsAndTagsIn(String name, Collection<String> tags, Pageable pageable);

    @Query("{ \"match\" : { \"tags\" : { \"query\" : \"?0\" , \"operator\": \"and\" } } }")
    Page<File> findAllByTagsIn(Collection<String> tags, Pageable pageable);

    int countByTagsIn(Collection<String> tags);

    int countByNameContaining(String name);
}
