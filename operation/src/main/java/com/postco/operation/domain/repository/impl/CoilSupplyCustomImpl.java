package com.postco.operation.domain.repository.impl;

import com.postco.operation.domain.repository.CoilSupplyRepositoryCustom;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CoilSupplyCustomImpl implements CoilSupplyRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ControlClientDTO.TotalSupply> getTotalSupplyByProcess() {

        String sql = "SELECT * FROM work_schedule_summary";

        Query query = entityManager.createNativeQuery(sql);

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(this::mapToTotalSupply)
                .collect(Collectors.toList());
    }

    private ControlClientDTO.TotalSupply mapToTotalSupply(Object[] result) {
        return ControlClientDTO.TotalSupply.builder()
                .process((String) result[0])
                .totalGoalCoils(((Number) result[2]).intValue())
                .totalCompleteCoils(((Number) result[3]).intValue())
                .totalScheduledCoils(((Number) result[4]).intValue())
                .build();
    }
}
