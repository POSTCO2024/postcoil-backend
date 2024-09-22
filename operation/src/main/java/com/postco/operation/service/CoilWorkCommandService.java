package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.CoilSupply;
import com.postco.operation.domain.repository.CoilSupplyRepository;
import com.postco.operation.domain.repository.WorkInstructionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoilWorkCommandService {
    private final WorkInstructionRepository workInstructionRepository;
    private final CoilSupplyRepository coilSupplyRepository;



    // 작업 지시서에 대한 작업 실행.
    // 보급 요구 버튼을 눌렀을 때,다음 작업이 시작
    // 1) coil supply(보급 요구 관련 엔티티) 업데이트
    // 2) 재료 상태 업데이트
    // 3) 작업 지시서 및 아이템 업데이트


    // step 1.
    // 큐에 보급 요구된 개수만큼 작업지시서 코일 순서대로 넣어야함
    // -> 작업지시서와 작업지시 아이템을 가져와야 함.
    // 일정 시간 후 보급 요구가 완료되는 메서드를 호출 후, 보급 요구 count 증가 메서드 호출




    // 일정 시간 후 보급 완료시키는 메서드 (설비 앞 도착 시)
    @Transactional
    public void requestCoilSupply(Long coilSupplyId, int suppliedCount, long delayInSeconds) {
        log.info("Requesting coil supply for ID: {}, count: {}, delay: {} seconds", coilSupplyId, suppliedCount, delayInSeconds);

        CoilSupply coilSupply = coilSupplyRepository.findById(coilSupplyId)
                .orElseThrow(() -> new IllegalArgumentException("CoilSupply not found"));

        completeSupplyAfterDelay(coilSupply, suppliedCount, delayInSeconds);
    }

    private void completeSupplyAfterDelay(CoilSupply coilSupply, int suppliedCount, long delayInSeconds) {
        CompletableFuture.runAsync(() -> {
            try {
                long delayInMilliseconds = delayInSeconds * 1000;
                Thread.sleep(delayInMilliseconds);

                updateSupply(coilSupply, suppliedCount);

                log.info("Completed supply for CoilSupply ID: {}, updated count: {}", coilSupply.getId(), suppliedCount);
            } catch (InterruptedException e) {
                log.error("Supply completion interrupted for CoilSupply ID: {}", coilSupply.getId(), e);
                Thread.currentThread().interrupt();
            }
        });
    }

    @Transactional
    public void updateSupply(CoilSupply coilSupply, int suppliedCount) {
        coilSupply.updateSupply(suppliedCount);
        coilSupplyRepository.save(coilSupply);
        log.info("Updated supply for CoilSupply ID: {}, new supplied count: {}", coilSupply.getId(), coilSupply.getSuppliedCoils());
    }


    // step 2.
    // 보급 요구가 완료되면 1번부터 바로 작업 시작
    // 재료 상태



}
