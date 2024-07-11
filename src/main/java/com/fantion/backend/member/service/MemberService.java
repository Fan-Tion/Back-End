package com.fantion.backend.member.service;

import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Request;
import com.fantion.backend.member.dto.SignupDto.Response;
import com.fantion.backend.member.dto.TokenDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MemberService {

  Response signup(Request request, MultipartFile file);

  TokenDto signin(SigninDto signinDto);
}
