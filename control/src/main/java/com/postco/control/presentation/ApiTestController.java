package com.postco.control.presentation;

import com.postco.control.presentation.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class ApiTestController {
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return new User(id, "신찬규 바보", "chandex@example.com");
    }
}

