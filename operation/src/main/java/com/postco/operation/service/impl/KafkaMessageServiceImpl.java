package com.postco.operation.service.impl;

import com.postco.core.dto.MaterialDTO;
import com.postco.core.dto.OrderDTO;
import com.postco.core.utils.mapper.MapperUtils;
import com.postco.operation.domain.entity.Materials;
import com.postco.operation.domain.entity.Order;
import com.postco.operation.domain.repository.OrderRepository;
import com.postco.operation.infra.kafka.MaterialsProducer;
import com.postco.operation.infra.kafka.OrderProducer;
import com.postco.operation.service.KafkaMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageServiceImpl implements KafkaMessageService {
    private final OrderRepository orderRepository;
    private final MaterialsProducer materialsProducer;
    private final OrderProducer orderProducer;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void sendAllMaterials() {
        List<Materials> materialsList = entityManager.createQuery(
                        "SELECT m FROM Materials m LEFT JOIN FETCH m.order", Materials.class)
                .getResultList();
        // 특정 규칙 매핑 적용
        PropertyMap<Materials, MaterialDTO.View> propertyMap = new PropertyMap<>() {
            @Override
            protected void configure() {
                map(source.getOrder().getId(), destination.getOrderId());
            }
        };

        // 엔티티 리스트 -> DTO 변환
        List<MaterialDTO.View> viewDto = MapperUtils.mapListWithProperty(materialsList, MaterialDTO.View.class, propertyMap);

        // Kafka 전송
        viewDto.forEach(materialsProducer::sendMaterials);
    }

    @Override
    public void sendOrders() {
        List<Order> orderList = orderRepository.findAll();

        // 엔티티 -> DTO 변환
        List<OrderDTO.View> orderDTO = MapperUtils.mapList(orderList, OrderDTO.View.class);

        // Kafka 전송
        orderDTO.forEach(orderProducer::sendOrders);
    }

    @Override
    public void sendWorkTimeView() {

    }
}
