package com.postco.operation.presentation;

import com.postco.operation.presentation.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class ApiTestController {
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return new User(id, "조업", "chandex@example.com");
    }
}

