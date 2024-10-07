package com.postco.control.service.impl;

import com.postco.control.presentation.dto.response.Fc001aDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class ProcessCounter {
    // 각 차공정에 따른 필드 업데이트 로직을 Map에 미리 정의
    private static final Map<String, Consumer<Fc001aDTO.Table>> processCountUpdater = Map.of(
            "1CAL", table -> table.setProc1CAL(table.getProc1CAL() + 1),
            "2CAL", table -> table.setProc2CAL(table.getProc2CAL() + 1),
            "1EGL", table -> table.setProc1EGL(table.getProc1EGL() + 1),
            "2EGL", table -> table.setProc2EGL(table.getProc2EGL() + 1),
            "1CGL", table -> table.setProc1CGL(table.getProc1CGL() + 1),
            "2CGL", table -> table.setProc2CGL(table.getProc2CGL() + 1),
            "101", table -> table.setProc1Packing(table.getProc1Packing() + 1),
            "201", table -> table.setProc2Packing(table.getProc2Packing() + 1)
    );

    // 차공정에 따른 카운팅을 수행하는 메서드
    public static void countNextProc(Fc001aDTO.Table tableEntry, String nextProc) {
        // 해당 차공정에 대한 업데이트 로직이 있는지 확인
        Consumer<Fc001aDTO.Table> updater = processCountUpdater.get(nextProc);

        if (updater != null) {
            // 해당 공정이 존재하면 업데이트 수행
            updater.accept(tableEntry);
        } else {
            // 없는 공정일 경우 경고 로그 출력
            log.warn("알 수 없는 차공정: " + nextProc);
        }
    }
}
