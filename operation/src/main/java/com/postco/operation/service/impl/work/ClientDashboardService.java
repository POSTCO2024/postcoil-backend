package com.postco.operation.service.impl.work;

import com.postco.operation.domain.repository.impl.CoilSupplyCustomImpl;
import com.postco.operation.domain.repository.impl.MaterialCustomImpl;
import com.postco.operation.domain.repository.impl.WorkInstructionCustomImpl;
import com.postco.operation.infra.kafka.WebsocketProducer;
import com.postco.operation.presentation.dto.AnalysisDashboardClientDTO;
import com.postco.operation.presentation.dto.websocket.ControlClientDTO;
import com.postco.operation.presentation.dto.websocket.WebSocketMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDashboardService {
    private final CoilSupplyCustomImpl coilSupplyCustom;
    private final WorkInstructionCustomImpl workInstructionCustom;
    private final WebsocketProducer websocketProducer;
    private final MaterialCustomImpl materialCustom;
    private static final List<String> PROCESSES = Arrays.asList("1CAL", "2CAL", "1PCM", "2PCM", "1EGL", "2EGL", "1CGL", "2CGL");

    public void sendDashboardStartData(WebSocketMessageType eventType) {
        ControlClientDTO controlDto = ControlClientDTO.builder()
                .factoryDashboard(coilSupplyCustom.getTotalSupplyByProcess())
                .build();
        websocketProducer.sendToControlWork(String.valueOf(eventType), controlDto, true);
    }

    public void sendDashboardEndData(WebSocketMessageType eventType) {
        ControlClientDTO controlDto = ControlClientDTO.builder()
                .factoryDashboard(coilSupplyCustom.getTotalSupplyByProcess())
                .build();
        websocketProducer.sendToControlWork(String.valueOf(eventType), controlDto, false);
    }

    public Mono<AnalysisDashboardClientDTO> sendFirstStatus(String SchProcess) {
        return Mono.zip(
                Mono.fromCallable(() -> workInstructionCustom.getAnlysisAllStaticsInfo())
                        .subscribeOn(Schedulers.boundedElastic()),
                materialCustom.getCurrentInfo(SchProcess)
        ).map(tuple -> AnalysisDashboardClientDTO.builder()
                .processDashboard(tuple.getT1())
                .totalDashboard(tuple.getT2())
                .build());
    }

    public Mono<AnalysisDashboardClientDTO> sendToIndividualDashboardData(String process) {
        return Mono.zip(
                workInstructionCustom.getAnlysisStaticsInfo(process),
                materialCustom.getCurrentInfo(process)
        ).map(tuple -> {
            AnalysisDashboardClientDTO status = AnalysisDashboardClientDTO.builder()
                    .processDashboard(tuple.getT1())
                    .totalDashboard(tuple.getT2())
                    .build();
            websocketProducer.sendToIndividualDashboardData(process, status);
            return status;
        });
    }

    public Flux<AnalysisDashboardClientDTO> sendAllIndividualDashboardData() {
        return Flux.fromIterable(PROCESSES)
                .flatMap(this::sendToIndividualDashboardData);
    }

}
