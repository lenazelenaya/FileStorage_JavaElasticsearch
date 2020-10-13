package com.homework.controller;

import com.homework.dto.ErrorResponseDto;
import com.homework.dto.SuccessResponseDto;
import com.homework.exceptions.*;
import com.homework.service.FileStorageService;
import com.homework.dto.FileCreateDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/file")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(produces = "application/json")
    @ApiOperation(value = "Upload file to dataStorage",
            response = ResponseEntity.class)
    public ResponseEntity<?> upload(@ApiParam(value = "Name and size values of the file", required = true)
                                    @Valid @RequestBody FileCreateDto dto, Errors errors) {
        if (errors.hasErrors()) getExceptionMessage(errors);
        return new ResponseEntity<>(fileStorageService.upload(dto), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Delete file from storage by id",
            notes = "Provide id of the file to delete it",
            response = ResponseEntity.class)
    public ResponseEntity<?> deleteFile(
            @ApiParam(value = "Id of the file to delete", required = true)
            @PathVariable String id) {
        try {

            fileStorageService.deleteById(id);

            return new ResponseEntity<>(new SuccessResponseDto(true), HttpStatus.OK);

        } catch (NotFoundException e) {

            return new ResponseEntity<>(new ErrorResponseDto(false, e.getMessage()), HttpStatus.NOT_FOUND);

        }
    }

    @PostMapping(value = "/{id}/tags",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add list of tags to the file by id",
            notes = "Provide an id and tags as a list (ex.[\"tag1\", \"tag2\"]) to add it to the file")
    public ResponseEntity<?> assignTags(@ApiParam(value = "Id of the file to add tags to id", required = true)
                                             @PathVariable String id,
                                             @ApiParam(value = "List of the tags to add to the file", required = true)
                                             @RequestBody List<String> tags) {
        try {

            fileStorageService.assignTags(id, tags);

            return new ResponseEntity<>(new SuccessResponseDto(true), HttpStatus.OK);

        } catch (NotFoundException e) {

            return new ResponseEntity<>(new ErrorResponseDto(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{id}/tags",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete tags from the file by id",
            notes = "Provide an id and tags as a list (ex.[\"tag1\", \"tag2\"]) to delete it from the file")
    public ResponseEntity<?> deleteTags(@PathVariable String id,
                                             @ApiParam(value = "List of the tags to delete from the file", required = true)
                                             @RequestBody List<String> tags) {
        try {

            fileStorageService.deleteTags(id, tags);

            return new ResponseEntity<>(new SuccessResponseDto(true), HttpStatus.OK);

        } catch (NotFoundException e) {

            return new ResponseEntity<>(new ErrorResponseDto(false, e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all files",
            notes = "You can get all files by query or by tags. Also, you can provide pagination with size and page values")
    public ResponseEntity<?> getAll(@ApiParam(value = "Tags to search files by them")
                                         @RequestParam(value = "tags", required = false) List<String> tags,
                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @ApiParam(value = "Query to find files that contains it in the name")
                                         @RequestParam(value = "q", required = false) String query) {
        return new ResponseEntity<>(fileStorageService.getAll(tags, size, page, query), HttpStatus.OK);
    }

    private void getExceptionMessage(Errors errors) {
        Set<ConstraintViolation<?>> violationsSet = new HashSet<>();
        for (ObjectError e : errors.getAllErrors()) {
            violationsSet.add(e.unwrap(ConstraintViolation.class));
        }
        throw new ConstraintViolationException(violationsSet);
    }
}
