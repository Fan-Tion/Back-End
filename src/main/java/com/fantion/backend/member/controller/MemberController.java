package com.fantion.backend.member.controller;

import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Response;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;

  @PostMapping(value = "/signup")
  public ResponseEntity<SignupDto.Response> signup(
      @Valid @RequestPart(value = "request") SignupDto.Request request,
      @RequestPart(value = "file", required = false) MultipartFile file) {
    Response result = memberService.signup(request, file);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/check-email")
  public ResponseEntity<CheckDto> checkEmail(
      @RequestParam(value = "email") @Email(message = "이메일 형식이 올바르지 않습니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.") String email) {
    CheckDto result = memberService.checkEmail(email);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<CheckDto> checkNickname(
      @RequestParam(value = "nickname") @NotBlank(message = "닉네임은 공백일 수 없습니다.") String nickname) {
    CheckDto result = memberService.checkNickname(nickname);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/signin")
  public ResponseEntity<TokenDto.Local> signin(@Valid @RequestBody SigninDto signinDto) {
    TokenDto.Local result = memberService.signin(signinDto);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/naver/request")
  public ResponseEntity<String> naverRequest() {
    String result = memberService.naverRequest();
    return ResponseEntity.ok(result);
  }

  @GetMapping("/naver/signin")
  public ResponseEntity<TokenDto.Local> naverSignin(@RequestParam(value = "code") String code) {
    TokenDto.Local result = memberService.neverSignin(code);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/naver/link")
  public ResponseEntity<CheckDto> naverLink(
      @RequestParam(value = "linkEmail") @Email(message = "이메일 형식이 올바르지 않습니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.") String linkEmail) {
    CheckDto result = memberService.naverLink(linkEmail);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/signout")
  public ResponseEntity<CheckDto> signout() {
    CheckDto result = memberService.signout();
    return ResponseEntity.ok(result);
  }

  @PostMapping("/withdrawal")
  public ResponseEntity<CheckDto> withdrawal() {
    CheckDto result = memberService.withdrawal();
    return ResponseEntity.ok(result);
  }
}
