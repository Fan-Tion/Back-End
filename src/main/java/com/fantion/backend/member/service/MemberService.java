package com.fantion.backend.member.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.TokenDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@Service
public interface MemberService {

  ResultDTO<SignupDto.Response> signup(SignupDto.Request request, MultipartFile file);

  ResultDTO<CheckDto> checkEmail(String email);

  ResultDTO<CheckDto> checkNickname(String nickname);

  ResultDTO<TokenDto.Local> signin(SigninDto signinDto);

  RedirectView naverRequest();

  ResultDTO<TokenDto.Local> neverSignin(String code);

  ResultDTO<CheckDto> naverLink(String linkEmail);

  ResultDTO<CheckDto> naverUnlink();

  ResultDTO<CheckDto> signout();

  ResultDTO<CheckDto> withdrawal();
}
