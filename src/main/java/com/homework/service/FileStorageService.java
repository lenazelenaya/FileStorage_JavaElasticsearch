package com.homework.service;

import com.homework.Exceptions.NotFoundException;
import com.homework.dto.FileCreateDto;
import com.homework.dto.FileDto;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FileStorageService {

    private final FileStorageRepo repository;

    FileStorageService(FileStorageRepo repository){
        this.repository = repository;
    }

    public String upload(FileCreateDto dto){
        var newFile = File.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .size(dto.getSize())
                .tags(dto.getTags())
                .build();
        return repository.save(newFile).getId();
    }

    public void deleteById(String id) throws NotFoundException {
        repository.findById(id).orElseThrow(NotFoundException::new);
        repository.deleteById(id);
    }

    public void deleteTags(String id, List<String> tags) throws NotFoundException{
        var file = repository.findById(id).orElseThrow(NotFoundException::new);
        file.getTags().removeAll(tags);
        repository.save(file);
    }

    public void assignTags(String id, List<String> tags) throws NotFoundException {
        var file = repository.findById(id).orElseThrow(NotFoundException::new);
        file.setTags(tags);
        repository.save(file);
    }

    public List<FileDto> getAll(List<String> tags, Integer size, Integer page) {
        Pageable pageable = PageRequest.of(page,size);
        if(tags == null) {
            return repository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(FileDto::fromEntity)
                    .collect(Collectors.toList());
        }else return repository.findByTags(tags, pageable)
                .stream()
                .map(FileDto::fromEntity)
                .collect(Collectors.toList());
    }

    public long getCount() {
        var files = repository.findAll();
        return StreamSupport.stream(files.spliterator(), false).count();
    }
}
