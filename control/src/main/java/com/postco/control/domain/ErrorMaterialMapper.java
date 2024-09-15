package com.postco.control.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorMaterialMapper {
    // 에러재 조회    

    // 에러패스
    public static Map<String, Long> errorPassIds(List<Long> error_material_ids) {
        Map<String, Long> result = new HashMap<>();
        for (Long id : error_material_ids) {
            result.put("material_id", id);
        }
        return result;
    }
}
