package com.postco.operation.domain.repository.impl;
import com.postco.operation.domain.entity.MaterialProgress;
import com.postco.operation.domain.entity.QMaterials;
import com.postco.operation.domain.entity.QWorkInstruction;
import com.postco.operation.domain.entity.QWorkInstructionItem;
import com.postco.operation.domain.repository.WorkInstructionRepositoryCustom;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WorkInstructionCustomImpl implements WorkInstructionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ControlClientDTO.StatisticsInfo> getStatisticsInfo() {
        QWorkInstruction wi = QWorkInstruction.workInstruction;
        QWorkInstructionItem wii = QWorkInstructionItem.workInstructionItem;
        QMaterials m = QMaterials.materials;

        List<Tuple> results = queryFactory
                .select(wi.process, m.nextProc, m.progress, wii.count())
                .from(wi)
                .join(wi.items, wii)
                .join(wii.material, m)
                .groupBy(wi.process, m.nextProc, m.progress)
                .fetch();

        Map<String, ControlClientDTO.StatisticsInfo> statisticsMap = results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> Optional.ofNullable(tuple.get(wi.process)).orElse("Unknown Process"),
                        Collectors.collectingAndThen(Collectors.toList(), tuples -> {
                            ControlClientDTO.StatisticsInfo info = ControlClientDTO.StatisticsInfo.builder()
                                    .process(Optional.ofNullable(tuples.get(0).get(wi.process)).orElse("Unknown Process"))
                                    .currentProgress(new HashMap<>())
                                    .nextProc(new HashMap<>())
                                    .equipmentStatus("RUNNING")
                                    .build();

                            tuples.forEach(tuple -> {
                                String nextProc = Optional.ofNullable(tuple.get(m.nextProc)).orElse("Unknown");
                                MaterialProgress progress = Optional.ofNullable(tuple.get(m.progress)).orElse(MaterialProgress.UNKNOWN);
                                Long count = tuple.get(wii.count());
                                int countValue = (count != null) ? count.intValue() : 0;

                                info.addNextProc(nextProc, countValue);
                                info.addCurrentProgress(progress.name(), countValue);
                            });

                            return info;
                        })
                ));

        return new ArrayList<>(statisticsMap.values());
    }
}