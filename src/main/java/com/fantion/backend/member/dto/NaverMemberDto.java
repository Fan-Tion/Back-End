package com.fantion.backend.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaverMemberDto {

  @JsonProperty("resultcode")
  private String resultCode;
  @JsonProperty("message")
  private String message;
  @JsonProperty("response")
  private NaverMemberDetail naverMemberDetail;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class NaverMemberDetail {
    private String email;
    private String name;
    private String nickname;
    private String mobile;
    @JsonProperty("profile_image")
    private String profileImage;
  }
}
