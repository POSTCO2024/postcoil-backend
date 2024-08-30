package com.postco.operation.presentation;

import com.postco.operation.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialsController {
    private final MaterialService materialService;

    @GetMapping("/send")
    public ResponseEntity<String> getMaterials() {
        materialService.sendAllMaterials();
        return ResponseEntity.ok("All materials sent successfully");
    }
}
