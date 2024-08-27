package com.postco.operation.presentation;

import com.postco.operation.presentation.entity.User;
import com.postco.operation.service.TestProducer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ApiTestController {
    private final TestProducer producer;
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return new User(id, "조업", "chandex@example.com");
    }

    @GetMapping
    public String test() {
        return "Kafka Test";
    }

    @PostMapping
    public String sendMessage(@RequestParam("message") String message) {
        this.producer.sendMessage(message);
        return message;
    }
}

