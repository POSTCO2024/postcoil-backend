package com.postco.control.presentation;

import com.postco.control.presentation.domain.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class ApiTestController {
    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return new User(id, "John Doe", "john@example.com");
    }
}

