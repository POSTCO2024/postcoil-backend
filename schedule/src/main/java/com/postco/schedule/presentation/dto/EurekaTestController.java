package com.postco.schedule.presentation.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/eureka")
public class EurekaTestController {
    @GetMapping("/test")
    public String test(){

        return "Schedule Eureka Test";
    }
}
