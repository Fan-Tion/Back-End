package com.fantion.backend.member.service;

import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Request;
import com.fantion.backend.member.dto.SignupDto.Response;
import com.fantion.backend.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface MemberService {

  Response signup(Request request, MultipartFile file);
}
