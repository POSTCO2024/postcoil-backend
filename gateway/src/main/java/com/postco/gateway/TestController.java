package com.postco.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

    @GetMapping("/test")
    public Mono<String> test() {
        return Mono.just("Hello from API Gateway!");
    }
}
