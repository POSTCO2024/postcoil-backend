package com.postco.operation.infra.kafka;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class NullKeySerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused) throws IOException {
        jsonGenerator.writeFieldName("null");  // null 값을 "null" 문자열로 변환
    }
}
