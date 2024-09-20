package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final TargetMaterialRepository targetMaterialRepository;

    // 생산 기한일
    public List<Fc004aDTO.DueDate> getDueDateInfo(){
        List<Object[]> results = targetMaterialRepository.findMaterialNoAndDueDate();

        return results.stream()
                .map(result -> new Fc004aDTO.DueDate((String) result[0], (String) result[1]))
                .collect(Collectors.toList());
    }

    // 에러재/정상재 비율
    public Map<String, Long> getErrorAndNormalCount(){
        long errorCount = targetMaterialRepository.countByIsError("Y");
        long normalCount = targetMaterialRepository.countByIsError("N");

        Map<String, Long> result = new HashMap<>();
        result.put("error", errorCount);
        result.put("normal", normalCount);

        return result;
    }

    // 품종 비율
//    public List<Object[]> getCoilTypeCount(){
//        return targetMaterialRepository.countByCoilTypeCode();
//    }

    // 고객사 비율
    public List<Object[]> getCustomerCount(){
        return targetMaterialRepository.countByCustomerName();
    }

}
