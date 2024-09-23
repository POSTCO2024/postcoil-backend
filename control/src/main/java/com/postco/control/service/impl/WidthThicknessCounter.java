package com.postco.control.service.impl;

import com.postco.control.presentation.dto.response.Fc004aDTO;
import com.postco.core.dto.MaterialDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WidthThicknessCounter {
    public Fc004aDTO.WidthThicknessCount calculate(List<MaterialDTO.View> materials) {
        // 폭과 두께에 따른 분포 계산
        Map<Integer, Long> widthDistribution = new HashMap<>();
        Map<Double, Long> thicknessDistribution = new HashMap<>();

        for (MaterialDTO.View material : materials) {
            // 폭(width)을 100mm 단위로 나누어 카운팅
            int widthRange = (int) (material.getWidth() / 100) * 100;
            widthDistribution.put(widthRange, widthDistribution.getOrDefault(widthRange, 0L) + 1);

            // 두께(thickness)를 0.5mm 단위로 나누어 카운팅
            double thicknessRange = Math.floor(material.getThickness() / 0.5) * 0.5;
            thicknessDistribution.put(thicknessRange, thicknessDistribution.getOrDefault(thicknessRange, 0L) + 1);
        }

        return new Fc004aDTO.WidthThicknessCount(widthDistribution, thicknessDistribution);
    }
}
