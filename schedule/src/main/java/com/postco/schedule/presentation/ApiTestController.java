package com.postco.schedule.presentation;

import com.postco.schedule.presentation.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class ApiTestController {
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return new User(id, "스케쥴", "chandex@example.com");
    }
}

