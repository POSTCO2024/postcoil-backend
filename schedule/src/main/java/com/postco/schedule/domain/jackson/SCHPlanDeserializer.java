package com.postco.schedule.domain.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.postco.schedule.domain.SCHPlan;

import java.io.IOException;

public class SCHPlanDeserializer extends JsonDeserializer<SCHPlan> {

    @Override
    public SCHPlan deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = p.getText();
        if ("null".equals(value) || value == null) {
            return null;  // "null" 문자열을 실제 null로 변환
        }
        // 정상적인 값이라면, 기본 처리 로직으로 넘어갑니다.
        return ctxt.readValue(p, SCHPlan.class);
    }
}
