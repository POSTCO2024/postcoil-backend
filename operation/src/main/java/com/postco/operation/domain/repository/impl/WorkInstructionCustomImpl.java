package com.postco.operation.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.WorkInstructionRepositoryCustom;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WorkInstructionCustomImpl implements WorkInstructionRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성

    @Override
    public List<ControlClientDTO.StatisticsInfo> getStatisticsInfo() {
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QCoilSupply cs = QCoilSupply.coilSupply;

        List<Tuple> results = queryFactory
                .select(wi.process,
                        cs.totalCoils.sum(),
                        cs.totalCoils.subtract(cs.totalProgressed).subtract(cs.totalRejects).sum(),
                        cs.totalProgressed.sum(),
                        wi.startTime.min())
                .from(wi)
                .join(cs).on(wi.id.eq(cs.workInstruction.id))
                .where(wi.workStatus.eq(WorkStatus.IN_PROGRESS))
                .groupBy(wi.process)
                .fetch();

        // 결과를 매핑
        return results.stream()
                .map(this::mapToStatisticsInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<List<AnalysisDashboardClientDTO.StatisticsInfo>> getAnlysisStaticsInfo(String SchProcess) {
        return Mono.fromCallable(() -> {
            QWorkInstruction wi = QWorkInstruction.workInstruction;
            QCoilSupply cs = QCoilSupply.coilSupply;

            List<Tuple> results = queryFactory
                    .select(wi.process,
                            cs.totalCoils.sum(),
                            cs.totalCoils.subtract(cs.totalProgressed).subtract(cs.totalRejects).sum(),
                            cs.totalProgressed.sum(),
                            wi.startTime.min())
                    .from(wi)
                    .join(cs).on(wi.id.eq(cs.workInstruction.id))
                    .where(wi.workStatus.eq(WorkStatus.IN_PROGRESS))
                    .where(wi.process.eq(SchProcess))
                    .groupBy(wi.process)
                    .fetch();

            return results.stream()
                    .map(this::mapToAnalysisStatisticsInfo)
                    .collect(Collectors.toList());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public List<AnalysisDashboardClientDTO.StatisticsInfo> getAnlysisAllStaticsInfo() {
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QCoilSupply cs = QCoilSupply.coilSupply;

        List<Tuple> results = queryFactory
                .select(wi.process,
                        cs.totalCoils.sum(),
                        cs.totalCoils.subtract(cs.totalProgressed).subtract(cs.totalRejects).sum(),
                        cs.totalProgressed.sum(),
                        wi.startTime.min())
                .from(wi)
                .join(cs).on(wi.id.eq(cs.workInstruction.id))
                .where(wi.workStatus.eq(WorkStatus.IN_PROGRESS))
                .groupBy(wi.process)
                .fetch();

        // 결과를 매핑
        return results.stream()
                .map(this::mapToAnalysisStatisticsInfo)
                .collect(Collectors.toList());
    }

    // 쿼리 결과를 StatisticsInfo로 매핑하는 메서드
    private ControlClientDTO.StatisticsInfo mapToStatisticsInfo(Tuple tuple) {
        return ControlClientDTO.StatisticsInfo.builder()
                .process(tuple.get(0, String.class))
                .workTotalCoils(tuple.get(1, Integer.class))
                .workScheduledCoils(tuple.get(2, Integer.class))
                .workTotalCompleteCoils(tuple.get(3, Integer.class))
                .workStartTime(tuple.get(4, LocalDateTime.class))
                .build();
    }
    private AnalysisDashboardClientDTO.StatisticsInfo mapToAnalysisStatisticsInfo(Tuple tuple) {
        return AnalysisDashboardClientDTO.StatisticsInfo.builder()
                .process(tuple.get(0, String.class))
                .workTotalCoils(tuple.get(1, Integer.class))
                .workScheduledCoils(tuple.get(2, Integer.class))
                .workTotalCompleteCoils(tuple.get(3, Integer.class))
                .workStartTime(tuple.get(4, LocalDateTime.class))
                .build();
    }
}
