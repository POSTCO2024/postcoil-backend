package com.postco.operation.domain.repository.impl;
import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.WorkInstructionRepositoryCustom;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class WorkInstructionCustomImpl implements WorkInstructionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ControlClientDTO.StatisticsInfo> getStatisticsInfo() {
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QCoilSupply cs = QCoilSupply.coilSupply;

        return queryFactory
                .select(Projections.constructor(ControlClientDTO.StatisticsInfo.class,
                        wi.process,
                        cs.totalCoils.sum().as("workTotalCoils"),
                        cs.totalCoils.subtract(cs.totalProgressed).subtract(cs.totalRejects).sum().as("workScheduledCoils"),
                        cs.totalProgressed.sum().as("workTotalCompleteCoils"),
                        wi.startTime.min().as("workStartTime")
                ))
                .from(wi)
                .join(cs).on(wi.id.eq(cs.workInstruction.id))
                .where(wi.workStatus.eq(WorkStatus.IN_PROGRESS))
                .groupBy(wi.process)
                .fetch();
    }
}