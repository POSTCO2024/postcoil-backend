package com.postco.control.service;

import com.postco.control.domain.TargetMaterial;
import com.postco.control.domain.repository.TargetMaterialRepository;
import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.core.dto.TargetMaterialDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final TargetMaterialRepository targetMaterialRepository;


    // 생산 기한일
    public List<Fc004aDTO.DueDate> getDueDateInfo(){
        List<TargetMaterialDTO.View> materials = Arrays.asList(
                new TargetMaterialDTO.View(3L, 2L, "CM240196", "2", 449.0, 27.0, 690.0, 0.31, "1PCM1CAL", "2023-09-18", "1EGL101", "Customer A", null, "1CAL", "Y", "ErrorTypeA", "N"),
                new TargetMaterialDTO.View(6L, 2L, "CI958029", "2", 479.0, 24.0, 1521.0, 0.95, "EGL", "2023-09-18", "2PCM2CAL1CGL201", "Customer B", null, "2CAL", "Y", "ErrorTypeB", "N"),
                new TargetMaterialDTO.View(7L, 2L, "CO755025", "2", 246.0, 28.0, 540.0, 0.19, "1PCM", "2023-09-18", "1CAL1EGL101", "Customer C", null, "1CAL", "N", null, "Y")

        );

        System.out.println("Dataset: " + materials);

        return targetMaterialRepository.findMaterialNoAndDueDate();
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
    public List<Object[]> getCoilTypeCount(){
        return targetMaterialRepository.countByCoilTypeCode();
    }

    // 고객사 비율
    public List<Object[]> getCustomerCount(){
        return targetMaterialRepository.countByCustomerName();
    }

}
