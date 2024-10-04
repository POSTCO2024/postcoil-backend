package com.postco.operation.domain.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.QMaterials;
import com.postco.operation.domain.entity.QWorkInstruction;
import com.postco.operation.domain.entity.QWorkInstructionItem;
import com.postco.operation.domain.repository.MaterialRepositoryCustom;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MaterialCustomImpl implements MaterialRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성

    @Override
    public List<ControlClientDTO.CurrentInfo> getCurrentInfo() {
        QMaterials m = QMaterials.materials;

        // 모든 curr_proc에 대한 정보 가져오기
        List<ControlClientDTO.CurrentInfo> totalInfo = queryFactory
                .select(Projections.bean(ControlClientDTO.CurrentInfo.class,
                        m.currProc.as("currProc")))  // curr_proc 값을 받아온다
                .from(m)
                .groupBy(m.currProc)
                .fetch();

        // 각 curr_proc에 대해 next_proc 및 progress 카운트 설정
        totalInfo.forEach(currentInfo -> {
            // nextProc과 currProcess를 담을 Map을 초기화
            Map<String, Integer> nextProcMap = new HashMap<>();
            Map<MaterialProgress, Integer> currentProgressMap = new HashMap<>();

            // curr_proc에 대한 next_proc 카운트 가져오기
            List<Tuple> nextProcResults = queryFactory
                    .select(m.nextProc, m.count())
                    .from(m)
                    .where(m.currProc.eq(currentInfo.getCurrProc())) // 현재 curr_proc에 해당하는 항목 기준
                    .groupBy(m.nextProc)
                    .fetch();

            // curr_proc에 대한 progress 카운트 가져오기
            List<Tuple> currentProgressResults = queryFactory
                    .select(m.progress, m.count())
                    .from(m)
                    .where(m.currProc.eq(currentInfo.getCurrProc())) // 현재 curr_proc에 해당하는 항목 기준
                    .groupBy(m.progress)
                    .fetch();

            log.info("nextProcResults: {}", nextProcResults);
            log.info("currentProgressResults: {}", currentProgressResults);

            // next_proc 결과를 CurrentInfo 객체에 저장
            nextProcResults.forEach(result -> {
                String nextProc = result.get(0, String.class);
                Long count = result.get(1, Long.class);
                if (count == null) {
                    count = 0L;
                }
                currentInfo.addNextProc(nextProc, count.intValue());
            });

            // progress 결과를 CurrentInfo 객체에 저장
            currentProgressResults.forEach(result -> {
                MaterialProgress progress = result.get(0, MaterialProgress.class);
                Long count = result.get(1, Long.class);
                if (count == null) {
                    count = 0L;
                }
                currentInfo.addCurrentProgress(progress, count.intValue());
            });

            // Map에서 null 키를 찾아서 "null" 문자열로 변환
            if (currentInfo.getNextProc().containsKey(null)) {
                Integer nullValue = currentInfo.getNextProc().remove(null);
                currentInfo.getNextProc().put("null", nullValue);  // "null" 문자열로 변환
            }
        });

        log.info("이걸 보냈다: {}", totalInfo);

        // JSON 직렬화 확인용 로그
        try {
            String serializedData = objectMapper.writeValueAsString(totalInfo);
            log.info("직렬화된 데이터: {}", serializedData);  // 직렬화된 JSON 출력
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 중 오류 발생: ", e);
        }

        return totalInfo;
    }
    @Override
    public List<AnalysisDashboardClientDTO.CurrentInfo> getCurrentInfo(String SchProcess) {
        QMaterials m = QMaterials.materials;
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QWorkInstructionItem wii = QWorkInstructionItem.workInstructionItem;

        // 모든 curr_proc에 대한 정보 가져오기
        List<AnalysisDashboardClientDTO.CurrentInfo> totalInfo = queryFactory
                .select(Projections.bean(AnalysisDashboardClientDTO.CurrentInfo.class,
                        m.currProc.as("currProc")))  // curr_proc 값을 받아온다
                .from(m)
                .where(m.currProc.eq(SchProcess))
                .groupBy(m.currProc)
                .fetch();

        // 각 curr_proc에 대해 next_proc, progress 및 ROLL_UNIT 카운트 설정
        totalInfo.forEach(currentInfo -> {
            // nextProc과 currProcess를 담을 Map을 초기화
            Map<String, Integer> nextProcMap = new HashMap<>();
            Map<MaterialProgress, Integer> currentProgressMap = new HashMap<>();

            // curr_proc에 대한 next_proc 카운트 가져오기
            List<Tuple> nextProcResults = queryFactory
                    .select(m.nextProc, m.count())
                    .from(m)
                    .where(m.currProc.eq(currentInfo.getCurrProc())) // 현재 curr_proc에 해당하는 항목 기준
                    .groupBy(m.nextProc)
                    .fetch();

            // curr_proc에 대한 progress 카운트 가져오기
            List<Tuple> currentProgressResults = queryFactory
                    .select(m.progress, m.count())
                    .from(m)
                    .where(m.currProc.eq(currentInfo.getCurrProc())) // 현재 curr_proc에 해당하는 항목 기준
                    .groupBy(m.progress)
                    .fetch();

            // curr_proc에 대한 ROLL_UNIT별 카운트 가져오기
            List<Tuple> rollUnitResults = queryFactory
                    .select(wi.rollUnit, m.count())
                    .from(m)
                    .join(wii).on(m.id.eq(wii.material.id)) // materials와 work_instruction 테이블 조인
                    .join(wi).on(wii.workInstruction.id.eq(wi.id))
                    .where(m.currProc.eq(currentInfo.getCurrProc())) // 현재 curr_proc에 해당하는 항목 기준
                    .groupBy(wi.rollUnit)  // ROLL_UNIT으로 그룹화
                    .fetch();

            log.info("nextProcResults: {}", nextProcResults);
            log.info("currentProgressResults: {}", currentProgressResults);
            log.info("rollUnitResults: {}", rollUnitResults);

            // next_proc 결과를 CurrentInfo 객체에 저장
            nextProcResults.forEach(result -> {
                String nextProc = result.get(0, String.class);
                Long count = result.get(1, Long.class);
                if (count == null) {
                    count = 0L;
                }
                currentInfo.addNextProc(nextProc, count.intValue());
            });

            // progress 결과를 CurrentInfo 객체에 저장
            currentProgressResults.forEach(result -> {
                MaterialProgress progress = result.get(0, MaterialProgress.class);
                Long count = result.get(1, Long.class);
                if (count == null) {
                    count = 0L;
                }
                currentInfo.addCurrentProgress(progress, count.intValue());
            });

            // ROLL_UNIT 결과를 CurrentInfo 객체에 저장
            rollUnitResults.forEach(result -> {
                String rollUnit = result.get(0, String.class);
                Long count = result.get(1, Long.class);
                if (count == null) {
                    count = 0L;
                }
                currentInfo.addRollUnitCount(rollUnit, count.intValue()); // ROLL_UNIT별 개수를 저장
            });

            // Map에서 null 키를 찾아서 "null" 문자열로 변환
            if (currentInfo.getNextProc().containsKey(null)) {
                Integer nullValue = currentInfo.getNextProc().remove(null);
                currentInfo.getNextProc().put("null", nullValue);  // "null" 문자열로 변환
            }
        });

        log.info("이걸 보냈다: {}", totalInfo);

        // JSON 직렬화 확인용 로그
        try {
            String serializedData = objectMapper.writeValueAsString(totalInfo);
            log.info("직렬화된 데이터: {}", serializedData);  // 직렬화된 JSON 출력
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 중 오류 발생: ", e);
        }

        return totalInfo;
    }

}
