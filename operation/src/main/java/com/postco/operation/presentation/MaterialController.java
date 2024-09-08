package com.postco.operation.presentation;

import com.postco.operation.service.impl.MaterialUpdateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {
    private final MaterialUpdateServiceImpl materialServiceImpl;

    @GetMapping("/send")
    public ResponseEntity<String> getMaterials() {
        materialServiceImpl.sendAllMaterials();
        return ResponseEntity.ok("All materials sent successfully");
    }
}
