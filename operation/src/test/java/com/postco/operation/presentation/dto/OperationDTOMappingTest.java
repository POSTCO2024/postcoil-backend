package com.postco.operation.presentation.dto;

import com.postco.core.dto.DTO;
import com.postco.operation.domain.entity.coil.ColdStandardReduction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class OperationDTOMappingTest {

    @Test
    public void 냉연_표준_감소_테이블_매핑() {
        // DTO 생성
        ColdStandardReductionDTO dto = ColdStandardReductionDTO.builder()
                .id(1L)
                .coilTypeCode("HTS600")
                .process("Cold Rolling")
                .thicknessReduction(0.15)
                .widthReduction(0.2)
                .temperature(300.0)
                .build();

        // DTO -> Entity 변환 (convert 메서드 사용)
        ColdStandardReduction entity = dto.convert(ColdStandardReduction.class);

        // 검증
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getCoilTypeCode(), entity.getCoilTypeCode());
        assertEquals(dto.getProcess(), entity.getProcess());
        assertEquals(dto.getThicknessReduction(), entity.getThicknessReduction());
        assertEquals(dto.getWidthReduction(), entity.getWidthReduction());
        assertEquals(dto.getTemperature(), entity.getTemperature());
    }

    @Test
    public void testMapToDTO() {
        // Map 생성
        Map<Object, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("coilTypeCode", "HTS600");
        map.put("process", "Cold Rolling");
        map.put("thicknessReduction", 0.15);
        map.put("widthReduction", 0.2);
        map.put("temperature", 300.0);

        // Map -> DTO 변환 (fromMap 메서드 사용)
        ColdStandardReductionDTO dto = DTO.fromMap(map, ColdStandardReductionDTO.class);

        // 검증
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("HTS600", dto.getCoilTypeCode());
        assertEquals("Cold Rolling", dto.getProcess());
        assertEquals(0.15, dto.getThicknessReduction());
        assertEquals(0.2, dto.getWidthReduction());
        assertEquals(300.0, dto.getTemperature());
    }
}
