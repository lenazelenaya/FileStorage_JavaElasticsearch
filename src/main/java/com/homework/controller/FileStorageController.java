package com.homework.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homework.exceptions.IncorrectValueException;
import com.homework.exceptions.NotFoundException;
import com.homework.exceptions.NotPresentSizeOrNameException;
import com.homework.service.FileStorageService;
import com.homework.dto.FileCreateDto;
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
    public ResponseEntity<String> upload(@RequestBody String json) throws JsonProcessingException {
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
    public ResponseEntity<String> deleteFile(@PathVariable String id) {

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
    public ResponseEntity<String> assignTags(@PathVariable String id, @RequestBody List<String> tags) {

        JSONObject response = new JSONObject();

        try {

            fileStorageService.assignTags(id, tags);

            response.put("success", true);

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);

        }catch(NotFoundException e){

            response.put("success", false);
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping(value = "/{id}/tags",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteTags(@PathVariable String id, @RequestBody List<String> tags) {

        JSONObject response = new JSONObject();

        try {

            fileStorageService.deleteTags(id, tags);

            response.put("success", true);

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
        }catch (NotFoundException e) {

            response.put("success", false);
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response.toJSONString(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAll(@RequestParam(value = "tags", required = false) List<String> tags,
                                                      @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                      @RequestParam(value = "page", defaultValue = "0") Integer page) {

        JSONObject response = new JSONObject();

        response.put("total", fileStorageService.getCount());
        response.put("page", fileStorageService.getAll(tags, size, page));

        return new ResponseEntity<>(response.toJSONString(), HttpStatus.OK);
    }
}
