package com.fantion.backend.common.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReturnDto {
  private String message;
  private Map<String, Object> data;
}
