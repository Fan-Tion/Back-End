package com.fantion.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@Schema(description = "리턴값 통일을 위한 DTO")
public class ResultDTO<T> {
    @Schema(description = "결과 메시지")
    private final String message;
    @Schema(description = "리턴되는 데이터 값")
    private final T data;
}
