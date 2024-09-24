package com.postco.operation.presentation;

import com.postco.operation.service.KafkaMessageService;
import com.postco.operation.service.impl.WebSocketServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/materials")
@CrossOrigin(origins = "http://localhost:6050")
@RequiredArgsConstructor
public class MaterialController {
    private final KafkaMessageService kafkaMessageService;
    private final WebSocketServiceImpl webSocketService;

    @GetMapping("/send")
    public ResponseEntity<String> getMaterials() {
        kafkaMessageService.sendAllMaterials();
        return ResponseEntity.ok("All materials sent successfully");
    }

    @GetMapping("/sendOrder")
    public ResponseEntity<String> getOrders() {
        kafkaMessageService.sendOrders();
        return ResponseEntity.ok("All orders sent successfully");
    }

    @GetMapping("/websocketTest")
    public void getMaterialAndSendToWebSocket() {
        webSocketService.findMaterials();
    }

}
