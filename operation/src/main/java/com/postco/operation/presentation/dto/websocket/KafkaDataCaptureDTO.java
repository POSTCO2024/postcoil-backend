package com.postco.operation.presentation.dto.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KafkaDataCaptureDTO<T> {
    private T payload;
}