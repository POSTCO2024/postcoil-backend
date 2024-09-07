package com.postco.schedule.service;

import com.postco.schedule.domain.ScheduleMaterials;
import com.postco.schedule.domain.repository.ScheduleMaterialsRepository;
import com.postco.schedule.presentation.dto.ScheduleMaterialsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialsService {

    private final ScheduleMaterialsRepository scheduleMaterialsRepository;

//    private JPAQueryFactory queryFactory;

    // TODO: Redis붙이고, ScheduleMaterials -> DTO로 변경하기
    public void insertMaterialsWithWorkTime(List<ScheduleMaterials> scheduleMaterials) {

        for (ScheduleMaterials material : scheduleMaterials) {
            // TODO: DTO-> entity 매핑

            // 작업 시간 계산
            Long workTime = calculateWorkTime(material.getGoalLength(), material.getGoalThickness(),
                    material.getGoalWidth(), material.getTotalWeight());
            material.setWorkTime(workTime);

            // 데이터베이스에 삽입
            scheduleMaterialsRepository.save(material);
        }
    }

    // 작업 시간 계산 메서드
    // TODO: TH 설비로 계산하기
    private Long calculateWorkTime(double goalLength, double goalThickness, double goalWidth, double totalWeight) {
        return  (long) ((goalLength * goalThickness * goalWidth) / totalWeight);
    }
}
