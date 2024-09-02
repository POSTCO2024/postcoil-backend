package com.postco.control.service;

import com.postco.control.presentation.dto.response.MaterialDTO;

import java.util.List;

public interface TargetMaterialService {
    List<MaterialDTO> findMaterial();
}