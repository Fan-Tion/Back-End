package com.fantion.backend.member.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class MemberAuthUtil {

    // 현재 로그인한 사용자 가져오기
    public static String getLoginUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = (String) authentication.getPrincipal();
        return principal;

    }
}
