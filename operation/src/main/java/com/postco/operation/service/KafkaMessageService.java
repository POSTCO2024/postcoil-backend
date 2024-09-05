package com.postco.operation.service;

import com.postco.core.dto.MaterialDTO;

public interface KafkaMessageService {
    // 재료 데이터 전송
    void sendMaterials(MaterialDTO.View materials);
}
