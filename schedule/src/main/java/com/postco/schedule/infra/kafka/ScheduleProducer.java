package com.postco.schedule.infra.kafka;

import com.postco.core.kafka.producer.KafkaProducer;
import com.postco.schedule.presentation.test.SCHConfirmDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleProducer {
    private static final String TOPIC = "schedule-confirm-data";
    private final KafkaProducer genericProducer;

    public void sendConfirmedSchedule(SCHConfirmDTO.View confirmView) {
        genericProducer.sendData(TOPIC, confirmView);
    }
}
