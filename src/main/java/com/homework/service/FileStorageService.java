package com.homework.service;

import com.homework.dto.AllFilesDto;
import com.homework.dto.IDDto;
import com.homework.exceptions.NotFoundException;
import com.homework.dto.FileCreateDto;
import com.homework.dto.FileDto;
import com.homework.extensions.*;
import com.homework.model.File;
import com.homework.repository.FileStorageRepo;
import org.apache.tika.Tika;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final FileStorageRepo repository;

    FileStorageService(FileStorageRepo repository) {
        this.repository = repository;
    }

    public IDDto upload(FileCreateDto dto) {
        List<String> tags = new ArrayList<>();
        if(dto.getTags() != null)
            tags.addAll(dto.getTags());

        Optional<String> extension = getExtensionByStringHandling(dto.getName());
        if (extension.isPresent()) {
            FileType type = getFileType(extension.get());
            if(type != null) {
                tags.add(type.getType());
            }
        }

        // This variant could be use if we need MIME type to add to tags

        // addTagForMIME(tags, dto.getName());

        var newFile = File.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .size(dto.getSize())
                .tags(tags)
                .build();
        return new IDDto(repository.save(newFile).getId());
    }

    public void deleteById(String id) throws NotFoundException {
        if(repository.existsById(id))
            repository.deleteById(id);
        else throw new NotFoundException("There is no file with such id");
    }

    public void deleteTags(String id, List<String> tags) throws NotFoundException {
        if(!repository.existsById(id)) throw new NotFoundException("File with such id is not exist");
        var file = repository.findById(id).get();
        if(file.getTags().containsAll(tags)) {
            file.getTags().removeAll(tags);
            repository.save(file);
        }else throw new NotFoundException("Tag not found on file");
    }

    public void assignTags(String id, List<String> tags) throws NotFoundException {
        if(!repository.existsById(id)) throw new NotFoundException("File with such id is not exist");
        var file = repository.findById(id).get();
        var currentTags = file.getTags();
        if(currentTags != null) {
            currentTags.addAll(tags);
            file.setTags(currentTags);
        } else file.setTags(tags);
        repository.save(file);
    }

    public AllFilesDto getAll(List<String> tags, Integer size, Integer page, String query) {
        Pageable pageable = PageRequest.of(page, size);
        if (tags == null && query == null) {
            var list = repository.findAll(pageable)
                    .getContent()
                    .stream()
                    .map(FileDto::fromEntity)
                    .collect(Collectors.toList());
            return new AllFilesDto((int) getCount(), list);
        } else if(query == null) {
            var list = findByTags(tags, pageable);
            return new AllFilesDto(list.size(), list);
        }
        else if(tags == null){
            var list = findByQuery(query, pageable);
            return new AllFilesDto(list.size(), list);
        }else{
            var list = findByQueryAndTags(tags, query, pageable);
            return new AllFilesDto(list.size(), list);
        }
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

    public long getCount() {
        return repository.count();
    }

    private FileType getFileType(String extension){
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

        if(mimeType != null){
            String newTag = mimeType.substring(0, mimeType.indexOf('/'));
            tags.add(newTag);
        }
    }
}
