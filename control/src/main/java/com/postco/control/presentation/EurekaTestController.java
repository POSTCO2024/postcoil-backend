package com.postco.control.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/control/")
public class EurekaTestController {
    @GetMapping("/test")
    public String test(){

        return "Control Eureka Test";
    }
}
