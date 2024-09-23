package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.service.CoilSupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilSupplyServiceImpl implements CoilSupplyService {
    private final CoilSupplyRepository coilSupplyRepository;

    @Override
    @Transactional
    public boolean updateCoilSupply(Long workInstructionId, int supplyCount) {
        try {
            // 보급 요청 처리 로직
            CoilSupply coilSupply = coilSupplyRepository.findByWorkInstructionId(workInstructionId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

            coilSupply.updateSupply(supplyCount);
            coilSupplyRepository.save(coilSupply);
            log.info("보급 완료. 작업 지시서 ID: {}, 보급 수량: {}", workInstructionId, supplyCount);
            return true;
        } catch (Exception e) {
            log.error("보급 요청 처리 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateRejectCount(Long workInstructionId) {
        try {
            CoilSupply coilSupply = coilSupplyRepository.findByWorkInstructionId(workInstructionId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

            coilSupply.updateRejects(1);  // 리젝트 카운트 1 증가
            coilSupplyRepository.save(coilSupply);  // 상태 저장
            log.info("Reject 코일 수 업데이트 완료. 총 리젝트 수: {}", coilSupply.getTotalRejects());
            return true;
        } catch (Exception e) {
            log.info("리젝 업데이트 중 오류 발생 : {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateFinishCount(Long workInstructionId) {
        try {
            CoilSupply coilSupply = coilSupplyRepository.findByWorkInstructionId(workInstructionId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid workInstructionId"));

            coilSupply.updateProgressed();
            coilSupplyRepository.save(coilSupply);
            log.info("완료 코일 수 업데이트 성공. 총 완료 코일 : {}", coilSupply.getTotalRejects());
            return true;
        } catch (Exception e) {
            log.info("완료 코일 업데이트 중 오류 발생 : {} ", e.getMessage());
            return false;
        }
    }

}
