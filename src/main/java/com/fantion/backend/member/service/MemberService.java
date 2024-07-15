package com.fantion.backend.member.service;

import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Request;
import com.fantion.backend.member.dto.TokenDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MemberService {

  SignupDto.Response signup(Request request, MultipartFile file);

  CheckDto checkEmail(String email);

  CheckDto checkNickname(String nickname);

  TokenDto.Local signin(SigninDto signinDto);

  String naverRequest();

  TokenDto.Local neverSignin(String code);

  CheckDto naverLink(String linkEmail);

  CheckDto signout();
}
