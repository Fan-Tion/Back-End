package com.fantion.backend.member.controller;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.MemberDto;
import com.fantion.backend.member.dto.RatingRequestDto;
import com.fantion.backend.member.dto.ResetPasswordDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;

  @PostMapping(value = "/signup")
  public ResponseEntity<?> signup(
      @Valid @RequestPart(value = "request") SignupDto.Request request,
      @RequestPart(value = "file", required = false) MultipartFile file) {
    ResultDTO<SignupDto.Response> result = memberService.signup(request, file);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/check-email")
  public ResponseEntity<?> checkEmail(
      @RequestParam(value = "email") @Email(message = "이메일 형식이 올바르지 않습니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.") String email) {
    ResultDTO<CheckDto> result = memberService.checkEmail(email);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<?> checkNickname(
      @RequestParam(value = "nickname") @NotBlank(message = "닉네임은 공백일 수 없습니다.") String nickname) {
    ResultDTO<CheckDto> result = memberService.checkNickname(nickname);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/signin")
  public ResponseEntity<?> Signin(@Valid @RequestBody SigninDto signinDto) {
    ResultDTO<TokenDto.Local> result = memberService.signin(signinDto);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/naver/request")
  public RedirectView naverRequest() {
    RedirectView result = memberService.naverRequest();
    return result;
  }

  @GetMapping("/naver/signin")
  public ResponseEntity<?> naverSignin(@RequestParam(value = "code") String code) {
    ResultDTO<TokenDto.Local> result = memberService.neverSignin(code);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/naver/link")
  public ResponseEntity<?> naverLink(
      @RequestParam(value = "linkEmail") @Email(message = "이메일 형식이 올바르지 않습니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.") String linkEmail) {
    ResultDTO<CheckDto> result = memberService.naverLink(linkEmail);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/naver/unlink")
  public ResponseEntity<?> naverUnlink() {
    ResultDTO<CheckDto> result = memberService.naverUnlink();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/signout")
  public ResponseEntity<?> signout() {
    ResultDTO<CheckDto> result = memberService.signout();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/withdrawal")
  public ResponseEntity<?> withdrawal() {
    ResultDTO<CheckDto> result = memberService.withdrawal();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/my-info")
  public ResponseEntity<?> myInfo() {
    ResultDTO<MemberDto.Response> result = memberService.myInfo();
    return ResponseEntity.ok(result);
  }

  @PutMapping("/edit/info")
  public ResponseEntity<?> myInfoEdit(@Valid @RequestBody MemberDto.Request request) {
    ResultDTO<CheckDto> result = memberService.myInfoEdit(request);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/edit/profile-image")
  public ResponseEntity<?> profileImageEdit(@RequestPart(value = "file" ) MultipartFile file) {
    ResultDTO<CheckDto> result = memberService.profileImageEdit(file);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/reset-password-request")
  public ResponseEntity<?> resetPasswordRequest(
      @Valid @RequestBody ResetPasswordDto.MailRequest Request) {
    ResultDTO<CheckDto> result = memberService.resetPasswordEmail(Request);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/reset-password")
  public ResponseEntity<?> resetPassword(
      @Valid @RequestBody ResetPasswordDto.ChangeRequest request) {
    ResultDTO<CheckDto> result = memberService.resetPassword(request);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/rating")
  public ResponseEntity<?> rating(@RequestBody RatingRequestDto request) {
    ResultDTO<CheckDto> result = memberService.rating(request);
    return ResponseEntity.ok(result);
  }
}
