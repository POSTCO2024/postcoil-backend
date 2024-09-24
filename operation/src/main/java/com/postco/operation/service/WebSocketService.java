package com.postco.operation.service;

import com.postco.operation.domain.entity.Materials;

import java.util.List;

public interface WebSocketService {
    List<Materials> findMaterials();
}
