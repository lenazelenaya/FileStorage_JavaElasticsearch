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

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
        file.getTags().removeAll(tags);
        repository.save(file);
    }

    public void assignTags(String id, List<String> tags) throws NotFoundException {
        var file = repository.findById(id).orElseThrow(NotFoundException::new);
        file.getTags().addAll(tags);
        repository.save(file);
    }

    public List<FileDto> getAll(List<String> tags, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page, size);
        if (tags == null) {
            return repository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(FileDto::fromEntity)
                    .collect(Collectors.toList());
        } else return this.findByTags(tags, pageable);
    }

    public long getCount() {
        var files = repository.findAll();
        return StreamSupport.stream(files.spliterator(), false).count();
    }

    private List<FileDto> findByTags(List<String> tags, Pageable pageable) {
        String query = String.join(" ", tags);
        Page<File> files = repository.search(queryStringQuery(query), pageable);
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
 // !!!
        System.out.println(fileName);
        System.out.println(Arrays.stream(DocType.values()).anyMatch(type -> type.toString().equals(extension.toUpperCase())));

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

        return null;
    }
}
