package com.postco.operation.service;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;

public interface KafkaMessageService {
    // 모든 재료 전송 메서드
    void sendAllMaterials();
    // 모든 주문 데이터 전송
    void sendOrders();
    // 작업시간 뷰 데이터 전송
    void sendWorkTimeView();
}
