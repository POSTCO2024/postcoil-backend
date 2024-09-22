package com.postco.operation.service.impl;

import com.postco.operation.domain.entity.WorkInstructionItem;
import com.postco.operation.domain.repository.WorkItemRepository;
import com.postco.operation.service.WorkItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemServiceImpl implements WorkItemService {
    private final WorkItemRepository workItemRepository;

    @Override
    @Transactional
    public boolean rejectWorkItem(Long itemId) {
        try {
            WorkInstructionItem item = workItemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("작업 아이템을 찾을 수 없습니다."));

            item.updateReject();
            workItemRepository.save(item);

            log.info("작업 아이템 리젝트 처리 완료. ID: {}", itemId);
            return true;
        } catch (Exception e) {
            log.error("작업 아이템 리젝트 처리 중 오류 발생. ID: {}", itemId, e);
            return false;
        }
    }
}
