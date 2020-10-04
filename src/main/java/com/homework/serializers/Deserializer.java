package com.homework.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.homework.Exceptions.IncorrectValueException;
import com.homework.Exceptions.NotPresentSizeOrNameException;
import com.homework.dto.FileCreateDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Deserializer extends StdDeserializer<FileCreateDto> {

    public Deserializer() {
        this(null);
    }

    public Deserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FileCreateDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (!node.hasNonNull("name") || !node.hasNonNull("size")) {
            throw new NotPresentSizeOrNameException("Does not allow empty name or size");
        }

        if (node.get("size").asLong() < 0) {
            throw new IncorrectValueException("Size can not be negative");
        }

        String name = node.get("name").toString();
        byte[] size = node.get("size").asText().getBytes();
        List<String> tags = getTags(node);

        return new FileCreateDto(name, size, tags);
    }

    private List<String> getTags(JsonNode node) {
        if(node.hasNonNull("tags")){
            List<String> list = new ArrayList<>();
            ArrayNode array = (ArrayNode) node.get("tags");
            for(int i = 0; i < array.size(); i++){
                list.add(array.get(i).asText());
            }
            return list;
        }
        return null;
    }
}
