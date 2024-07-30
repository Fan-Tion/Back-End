package com.fantion.backend.member.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.MemberDto;
import com.fantion.backend.member.dto.MyBalanceDto;
import com.fantion.backend.member.dto.RatingRequestDto;
import com.fantion.backend.member.dto.ResetPasswordDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.SignupResponse;
import com.fantion.backend.member.dto.TokenDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@Service
public interface MemberService {

  ResultDTO<SignupResponse> signup(SignupDto.SignupRequest request, MultipartFile file);

  ResultDTO<CheckDto> checkEmail(String email);

  ResultDTO<CheckDto> checkNickname(String nickname);

  ResultDTO<TokenDto.Local> signin(SigninDto signinDto);

  RedirectView naverRequest();

  ResultDTO<TokenDto.Local> neverSignin(String code);

  ResultDTO<CheckDto> naverLink(String linkEmail);

  ResultDTO<CheckDto> naverUnlink();

  ResultDTO<CheckDto> signout();

  ResultDTO<CheckDto> withdrawal();

  ResultDTO<MemberDto.MemberResponse> myInfo();

  ResultDTO<CheckDto> myInfoEdit(MemberDto.MemberUpdateRequest request);

  ResultDTO<CheckDto> profileImageEdit(MultipartFile file);

  ResultDTO<CheckDto> resetPasswordEmail(ResetPasswordDto.MailRequest request);

  ResultDTO<CheckDto> resetPassword(ResetPasswordDto.ChangeRequest request);

  ResultDTO<CheckDto> rating(RatingRequestDto request);

  ResultDTO<Page<MyBalanceDto>> myBalance(String searchOption, Integer pageNumber);
}
