package com.fantion.backend.member.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

  private String email;
  private String nickname;
  private String address;
  private Boolean auth;
  private String authType;
  private Integer rating;
  private String profileImage;
  private String phoneNumber;
  private LocalDateTime createDate;
}
