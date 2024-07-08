package com.fantion.backend.member.controller;

import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Response;
import com.fantion.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/signup")
  public ResponseEntity<SignupDto.Response> signup(
      @RequestPart(value = "request") SignupDto.Request request,
      @RequestPart(value = "file") MultipartFile file) {
    Response result = memberService.signup(request, file);
    return ResponseEntity.ok(result);
  }
}
