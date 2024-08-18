package com.fantion.backend.member.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MemberAuthUtil {

  // AccessToken에서 Email 가져오기
  public static String getLoginUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getName() != null) {
      return authentication.getName();
    }
    return null;
  }
}
