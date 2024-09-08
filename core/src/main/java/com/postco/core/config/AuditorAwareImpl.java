package com.postco.core.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        // 6자리만
        String shortUUID = UUID.randomUUID().toString().substring(0, 6);

        String programId = simpleClassName + "-" + shortUUID;

        return Optional.of(programId);
    }
}
