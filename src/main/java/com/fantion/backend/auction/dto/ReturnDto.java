package com.fantion.backend.auction.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnDto {
  private String message;
  private Map<String, Object> data;
}
