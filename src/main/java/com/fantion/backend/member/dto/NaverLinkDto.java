package com.fantion.backend.member.dto;

import com.fantion.backend.member.dto.NaverMemberDto.NaverMemberDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverLinkDto {

  @JsonProperty("access_token")
  private String accessToken;
  @JsonProperty("result")
  private String result;
}
