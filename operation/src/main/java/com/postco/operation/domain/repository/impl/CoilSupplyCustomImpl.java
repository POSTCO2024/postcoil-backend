package com.postco.operation.domain.repository.impl;

import com.postco.operation.domain.entity.QCoilSupply;
import com.postco.operation.domain.entity.QWorkInstruction;
import com.postco.operation.domain.repository.CoilSupplyRepositoryCustom;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CoilSupplyCustomImpl implements CoilSupplyRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ControlClientDTO.TotalSupply> getTotalSupplyByProcess() {
        QCoilSupply cs = QCoilSupply.coilSupply;
        QWorkInstruction wi = QWorkInstruction.workInstruction;

        return queryFactory
                .select(Projections.constructor(ControlClientDTO.TotalSupply.class,
                        wi.process,
                        cs.totalCoils.sum().as("totalGoalCoils"),
                        cs.totalProgressed.sum().as("totalCompleteCoils"),
                        wi.startTime.min()
                ))
                .from(cs)
                .join(cs.workInstruction, wi)
                .groupBy(wi.process)
                .fetch();
    }
}
