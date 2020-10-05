package com.homework.service;

import com.homework.exceptions.NotFoundException;
import com.homework.dto.FileCreateDto;
import com.homework.dto.FileDto;
import com.homework.extensions.*;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FileStorageService {

    private final FileStorageRepo repository;

    FileStorageService(FileStorageRepo repository) {
        this.repository = repository;
    }

    public String upload(FileCreateDto dto) {
        List<String> tags = new ArrayList<>();
        if(dto.getTags() != null)
            tags.addAll(dto.getTags());

        FileType type = getFileType(dto.getName());

        if(type != null)
            addTag(tags, type);

        var newFile = File.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .size(dto.getSize())
                .tags(tags)
                .build();
        return repository.save(newFile).getId();
    }

    public void deleteById(String id) throws NotFoundException {
        repository.findById(id).orElseThrow(NotFoundException::new);
        repository.deleteById(id);
    }

    public void deleteTags(String id, List<String> tags) throws NotFoundException {
        var file = repository.findById(id).orElseThrow(NotFoundException::new);
        if(file.getTags().containsAll(tags)) {
            file.getTags().removeAll(tags);
            repository.save(file);
        }else throw new NotFoundException("One of the tags is not present on the file and body");
    }

    public void assignTags(String id, List<String> tags) throws NotFoundException {
        var file = repository.findById(id).orElseThrow(NotFoundException::new);
        file.getTags().addAll(tags);
        repository.save(file);
    }

    public List<FileDto> getAll(List<String> tags, Integer size, Integer page, String query) {
        Pageable pageable = PageRequest.of(page, size);
        if (tags == null && query == null) {
            return repository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(FileDto::fromEntity)
                    .collect(Collectors.toList());
        } else if(query == null)
            return this.findByTags(tags, pageable);
        else if(tags == null){
            return this.findByQuery(query, pageable);
        }else{
            return this.findByQueryAndTags(tags, query, pageable);
        }
    }

    public long getCount() {
        var files = repository.findAll();
        return StreamSupport.stream(files.spliterator(), false).count();
    }

    private List<FileDto> findByTags(List<String> tags, Pageable pageable) {
        Page<File> files = repository.findAllByTagsIn(tags, pageable);
        return files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
    }

    private List<FileDto> findByQuery(String query, Pageable pageable) {
        Page<File> files = repository.findAllByNameContains(query, pageable);
        return files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
    }

    private List<FileDto> findByQueryAndTags(List<String> tags, String query, Pageable pageable) {
        Page<File> files = repository.findAllByNameContainsAndTagsIn(query, tags, pageable);
        return files.toList().stream().map(FileDto::fromEntity).collect(Collectors.toList());
    }

    private void addTag(List<String> tags, FileType type){
        if(type instanceof AudioType)
            tags.add("audio");
        else if(type instanceof DocType)
            tags.add("document");
        else if(type instanceof VideoType)
            tags.add("video");
        else if(type instanceof ImageType)
            tags.add("image");
    }

    private FileType getFileType(String fileName){
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

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
}
