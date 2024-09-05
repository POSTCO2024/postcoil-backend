package com.postco.operation.service;

import com.postco.operation.presentation.dto.MaterialsDTO;

public interface KafkaMessageService {
    // 재료 데이터 전송
    void sendMaterials(MaterialsDTO.View materials);
}
