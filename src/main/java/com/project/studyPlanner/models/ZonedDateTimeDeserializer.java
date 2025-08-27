package com.project.studyPlanner.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ") // Matches your input format with +0000
            .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
            .toFormatter();

    @Override
    public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        return ZonedDateTime.parse(p.getText(), formatter);
    }
}
