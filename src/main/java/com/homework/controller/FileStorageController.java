package com.homework.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homework.exceptions.IncorrectValueException;
import com.homework.exceptions.NotFoundException;
import com.homework.exceptions.NotPresentSizeOrNameException;
import com.homework.service.FileStorageService;
import com.homework.dto.FileCreateDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<String> upload(@ApiParam(value = "Json with name and size values of the file", required = true)
                                         @RequestBody String json) throws JsonProcessingException {
        JSONObject resp = new JSONObject();
        try {
            var dto = new ObjectMapper().readValue(json, FileCreateDto.class);
            resp.put("id", fileStorageService.upload(dto));
            return new ResponseEntity<>(resp.toJSONString(), HttpStatus.OK);
        } catch (IncorrectValueException | NotPresentSizeOrNameException e) {
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return new ResponseEntity<>(resp.toJSONString(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Delete file from storage by id",
            notes = "Provide id of the file to delete it",
            response = ResponseEntity.class)
    public ResponseEntity<String> deleteFile(
            @ApiParam(value = "Id of the file to delete", required = true)
            @PathVariable String id) {

        JSONObject resp = new JSONObject();

        try {

            fileStorageService.deleteById(id);

            resp.put("success", true);
            return new ResponseEntity<>(resp.toJSONString(), HttpStatus.OK);

        } catch (NotFoundException e) {

            resp.put("success", false);
            resp.put("error", e.getMessage());

            return new ResponseEntity<>(resp.toJSONString(), HttpStatus.NOT_FOUND);

        }
    }

    @PostMapping(value = "/{id}/tags",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Add list of tags to the file by id",
            notes = "Provide an id and tags as a list (ex.[\"tag1\", \"tag2\"]) to add it to the file")
    public ResponseEntity<String> assignTags(@ApiParam(value = "Id of the file to add tags to id", required = true)
                                                 @PathVariable String id,
                                             @ApiParam(value = "List of the tags to add to the file", required = true)
                                             @RequestBody List<String> tags) {

        JSONObject response = new JSONObject();

        try {

            fileStorageService.assignTags(id, tags);

            response.put("success", true);

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);

        } catch (NotFoundException e) {

            response.put("success", false);
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{id}/tags",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete tags from the file by id",
            notes = "Provide an id and tags as a list (ex.[\"tag1\", \"tag2\"]) to delete it from the file")
    public ResponseEntity<String> deleteTags(@PathVariable String id,
                                             @ApiParam(value = "List of the tags to delete from the file", required = true)
                                             @RequestBody List<String> tags) {

        JSONObject response = new JSONObject();

        try {

            fileStorageService.deleteTags(id, tags);

            response.put("success", true);

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
        } catch (NotFoundException e) {

            response.put("success", false);
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all files",
            notes = "You can get all files by query or by tags. Also, you can provide pagination with size and page values")
    public ResponseEntity<String> getAll(@ApiParam(value = "Tags to search files by them")
                                             @RequestParam(value = "tags", required = false) List<String> tags,
                                         @ApiParam(value = "Size of the page", defaultValue = "10")
                                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                                         @ApiParam(value = "The 0-based parameter for paging", defaultValue = "0")
                                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @ApiParam(value = "Query to find files that contains it in the name")
                                         @RequestParam(value = "q", required = false) String query) {

        JSONObject response = new JSONObject();

        response.put("total", fileStorageService.getCount());
        response.put("page", fileStorageService.getAll(tags, size, page, query));

        return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
    }
}
