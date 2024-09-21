package com.postco.websocket.web;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoilData {
    private Long materialId;
    private Double width;
    private Double thickness;
    private String nextCurr;
}
