package com.postco.operation.domain.repository.impl;
import com.postco.operation.domain.entity.*;
import com.postco.operation.domain.repository.WorkInstructionRepositoryCustom;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WorkInstructionCustomImpl implements WorkInstructionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ControlClientDTO.StatisticsInfo> getStatisticsInfo() {
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QCoilSupply cs = QCoilSupply.coilSupply;


        List<ControlClientDTO.StatisticsInfo> statisticsInfos = queryFactory
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

        return statisticsInfos;
    }
    @Override
    public List<ControlClientDTO.CurrentInfo> getCurrentInfo() {
        QMaterials m = QMaterials.materials;
        QWorkInstructionItem wii = QWorkInstructionItem.workInstructionItem;
        QWorkInstruction wi = QWorkInstruction.workInstruction;

        // 전체 정보를 가져올 리스트 초기화
        List<ControlClientDTO.CurrentInfo> totalInfo = queryFactory
                .select(Projections.bean(ControlClientDTO.CurrentInfo.class,
                        wi.id.as("workInstructionId"))) // workInstructionId를 가져오는 부분 추가
                .from(wi)
                .join(wii).on(wi.id.eq(wii.workInstruction.id))
                .fetch();

        // 각 작업 지시서의 통계 정보에 대해 nextProc 및 currProc 설정
        totalInfo.forEach(currentInfo -> {
            // 다음 공정(nextProc)에 대한 정보 가져오기
            List<Tuple> nextProcResults = queryFactory
                    .select(m.nextProc, m.count())
                    .from(m)
                    .join(wii).on(m.id.eq(wii.material.id))
                    .where(wii.workInstruction.id.eq(currentInfo.getWorkInstructionId())) // 작업 지시서와 연결된 항목 기준
                    .groupBy(m.nextProc)
                    .fetch();

            log.info("이게 nextProc인데: {}", nextProcResults);

            // 현재 진행 상황(progress)에 대한 정보 가져오기
            List<Tuple> currentProgressResults = queryFactory
                    .select(m.currProc, m.count())
                    .from(m)
                    .join(wii).on(m.id.eq(wii.material.id))
                    .where(wii.workInstruction.id.eq(currentInfo.getWorkInstructionId()))
                    .groupBy(m.currProc)
                    .fetch();

            log.info("이게 현재 진행상황 : {}", currentProgressResults);

            // 다음 공정 결과를 CurrentInfo 객체에 저장
            nextProcResults.forEach(result -> {
                String nextProc = result.get(0, String.class);
                Long count = result.get(1, Long.class);  // count는 LONG으로 들어온답니다
                if (count == null){
                    count = 0L;
                }
                currentInfo.addNextProc(nextProc, count.intValue());
            });

            // 현재 공정 결과를 CurrentInfo 객체에 저장
            currentProgressResults.forEach(result -> {
                String progress = result.get(0, String.class);
                Long count = result.get(1, Long.class);
                if (count == null){
                    count = 0L;
                }
                currentInfo.addCurrentProgress(progress, count.intValue());
            });

            log.info("현재 작업 정보: {}", currentInfo);
        });

        log.info("이걸 보냈다 {}", totalInfo);

        return totalInfo;
    }



}