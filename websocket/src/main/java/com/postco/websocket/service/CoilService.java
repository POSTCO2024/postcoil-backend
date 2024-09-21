package com.postco.websocket.service;


import com.postco.websocket.web.CoilData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CoilService {
    public CoilData processCoilData(String message) {
        log.info("메세지 요청 수신 완료 : ", message);
        return CoilData.builder()
                .materialId(1L)
                .width(7000.0)
                .thickness(0.12)
                .nextCurr("1CAL")
                .build();
    }
}
