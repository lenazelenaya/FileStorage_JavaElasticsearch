package com.homework.service;

import com.homework.dto.*;
import com.homework.exceptions.NotFoundException;
import com.homework.extensions.*;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import org.apache.tika.Tika;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Service
public class FileStorageService {

    private final FileStorageRepo repository;

    private final RestHighLevelClient client;

    public FileStorageService(FileStorageRepo repository, RestHighLevelClient client) {
        this.repository = repository;
        this.client = client;
    }

    public IDDto upload(FileCreateDto dto) {
        List<String> tagsList = new ArrayList<>();
        if (dto.getTags() != null)
            tagsList.addAll(dto.getTags());

        Optional<String> extension = getExtensionByStringHandling(dto.getName());
        if (extension.isPresent()) {
            FileType type = getFileType(extension.get());
            if (type != null) {
                tagsList.add(type.getType());
            }
        }

        // This variant could be use if we need MIME type to add to tags

        // addTagForMIME(tags, dto.getName());

        var newFile = File.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .size(dto.getSize())
                .tags(String.join(" ", tagsList))
                .build();
        return new IDDto(repository.save(newFile).getId());
    }

    public void deleteById(String id) throws NotFoundException {
        if (repository.existsById(id))
            repository.deleteById(id);
        else throw new NotFoundException("There is no file with such id");
    }

    public void deleteTags(String id, List<String> tags) throws NotFoundException {
        if (!repository.existsById(id)) throw new NotFoundException("File with such id is not exist");

//        Possible implementation with update API

//        UpdateByQueryRequest request = new UpdateByQueryRequest("files");
//        Map<String, Object> parameters = singletonMap("tags", tags);
//
//
//        String expr = "if(params.tags.stream()" +
//                ".containsAll( Arrays.asList(doc['tags'].split(\" \") ) { " +
//                    "Arrays.asList(doc['tags'].split(\" \").removeAll(params.tags) " +
//                "}";
//
//        request.setScript(new Script(ScriptType.INLINE,
//                "painless",
//                expr,
//                parameters));
//        request.setQuery(new TermQueryBuilder("id", id));
//        BulkByScrollResponse response = client.updateByQuery(request, RequestOptions.DEFAULT);

        var file = repository.findById(id).get();
        var currentTags = file.getTags();

        if (currentTags == null || currentTags.equals("")) throw new NotFoundException("Tag not found on file");

        var currentTagsList = new ArrayList<>(Arrays.asList(currentTags.split(" ")));

        // Check if currentTags contains all tags from list to delete
        if (currentTagsList.containsAll(tags)) {
            currentTagsList.removeAll(tags);
            file.setTags(String.join(" ", currentTagsList));
            repository.save(file);
        } else throw new NotFoundException("Tag not found on file");
    }

    public void assignTags(String id, List<String> tags) throws NotFoundException {
        if (!repository.existsById(id)) throw new NotFoundException("File with such id is not exist");
        var file = repository.findById(id).get();
        
        var currentTags = file.getTags();
        
        if (currentTags != null && !currentTags.equals("")) {
            
            var currentTagsSet = new HashSet<>(Arrays.asList(currentTags.split(" ")));
            
            currentTagsSet.addAll(tags);
            
            file.setTags(String.join(" ", currentTagsSet));
            
        } else file.setTags(String.join(" ", tags));
        repository.save(file);
    }

    public AllFilesResponseDto getAll(AllFilesRequestDto dto) {
        var page = dto.getPage();
        var size = dto.getSize();
        var tags = dto.getTags();
        var query = dto.getQ();

        Pageable pageable = PageRequest.of(page, size);
        if (tags == null && query == null) {
            var list = repository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(FileDto::fromEntity)
                    .collect(Collectors.toList());
            return new AllFilesResponseDto((int) repository.count(), list);
        } else if (query == null) {
            
            return findByTags(tags, pageable);
            
        } else if (tags == null) {

            return findByQuery(query, pageable);

        } else {

            return findByQueryAndTags(tags, query, pageable);

        }
    }

    private AllFilesResponseDto findByTags(List<String> tags, Pageable pageable) {
//        QueryBuilder queryBuilder = new MatchQueryBuilder("tags", String.join(" ", tags)).operator(Operator.AND);
//        repository.search(queryBuilder, pageable);
        
        Page<File> files = repository.findAllByTagsIn(tags, pageable);
        var list = files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
        return new AllFilesResponseDto(repository.countByTagsIn(tags), list);
    }

    private AllFilesResponseDto findByQuery(String query, Pageable pageable) {
        Page<File> files = repository.findAllByNameContains(query, pageable);
        var list = files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
        return new AllFilesResponseDto(repository.countByNameContaining(query), list);
    }

    private AllFilesResponseDto findByQueryAndTags(List<String> tags, String query, Pageable pageable) {
        Page<File> files = repository.findAllByNameContainsAndTagsIn(query, tags, pageable);
        var list = files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
        return new AllFilesResponseDto(list.size(), list);
    }

    private FileType getFileType(String extension) {
        if (Arrays.stream(AudioType.values())
                .anyMatch(type -> type.toString().equals(extension.toUpperCase())))
            return AudioType.valueOf(extension.toUpperCase());

        if (Arrays.stream(DocType.values()).
                anyMatch(type -> type.toString().equals(extension.toUpperCase())))
            return DocType.valueOf(extension.toUpperCase());

        if (Arrays.stream(VideoType.values())
                .anyMatch(type -> type.toString().equals(extension.toUpperCase())))
            return VideoType.valueOf(extension.toUpperCase());

        if (Arrays.stream(ImageType.values())
                .anyMatch(type -> type.toString().equals(extension.toUpperCase())))
            return ImageType.valueOf(extension.toUpperCase());

        // If there is no extension in enums, there would not be any file type
        return null;
    }

    private Optional<String> getExtensionByStringHandling(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1));
    }


    // For MIME types as application/zip
    private void addTagForMIME(List<String> tags, String name) {
        Tika tika = new Tika();
        String mimeType = tika.detect(name);

        if (mimeType != null) {
            String newTag = mimeType.substring(0, mimeType.indexOf('/'));
            tags.add(newTag);
        }
    }
}
