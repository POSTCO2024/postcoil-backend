package com.postco.control.presentation.dto.response;


import com.postco.core.dto.DTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExtractionStandardDTO implements DTO {
    private String factoryCode;
    private String materialStatus;
    private String progress;
    private String currProc;
}
