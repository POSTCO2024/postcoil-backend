package com.postco.websocket.web;

import com.postco.websocket.service.CoilService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

//@Controller
//@RequiredArgsConstructor
//public class CoilController {
//    private final CoilService coilService;
//    @MessageMapping("/send")
//    @SendTo("/topic/coilData")
//    public CoilData sendCoilData(String message) {
//        return coilService.processCoilData(message);
//    }
//}
