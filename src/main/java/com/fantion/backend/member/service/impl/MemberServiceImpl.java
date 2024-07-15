package com.fantion.backend.member.service.impl;

import com.fantion.backend.exception.impl.DuplicateEmailException;
import com.fantion.backend.exception.impl.DuplicateNicknameException;
import com.fantion.backend.exception.impl.ImageSaveException;
import com.fantion.backend.exception.impl.InvalidEmailException;
import com.fantion.backend.exception.impl.InvalidNicknameException;
import com.fantion.backend.exception.impl.InvalidPasswordException;
import com.fantion.backend.exception.impl.NotFoundMemberException;
import com.fantion.backend.exception.impl.SuspendedMemberException;
import com.fantion.backend.exception.impl.UnsupportedImageTypeException;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Request;
import com.fantion.backend.member.dto.SignupDto.Response;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.jwt.JwtTokenProvider;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.service.MemberService;
import com.fantion.backend.type.MemberStatus;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

  private static final Long REFRESH_TOKEN_EXPIRES_IN = 86400000L;
  private static final Long NICKNAME_EXPIRES_IN = 300000L;
  private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]{1,12}$");

  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate redisTemplate;

  @Override
  public Response signup(Request request, MultipartFile file) {

    // 이메일 체크
    if (!isValidEmail(request.getEmail())) {
      throw new InvalidEmailException();
    }

    // 중복가입 체크
    Optional<Member> byEmail = memberRepository.findByEmail(request.getEmail());
    if (byEmail.isPresent()) {
      Member member = byEmail.get();

      // 탈퇴상태가 아니면 중복가입 exception
      if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
        throw new DuplicateEmailException();
      }

    // 닉네임 중복 체크
    Optional<Member> byNickname = memberRepository.findByNickname(request.getNickname());
    if (byNickname.isPresent()) {
      throw new DuplicateNicknameException();
    }

    Member member = new Member();
    // 멤버정보 DB에 저장
    // 이미지 파일이 없을 때
    if (file.isEmpty() || file == null) {
      member = SignupDto.signupInput(request, null);
      memberRepository.save(member);
    } else { // 이미지 파일이 있을 때
      // 이미지 파일을 저장하고 경로를 가져오기
      String uuid = UUID.randomUUID().toString();
      String projectPath = System.getProperty("user.home") + "\\Desktop\\images\\";
      String fileName = uuid + "_" + file.getOriginalFilename();

      // 파일 이름에서 확장자 추출
      String fileExtension = StringUtils.getFilenameExtension(fileName);

      // 지원하는 이미지 파일 확장자 목록
      List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

      // 확장자가 이미지 파일인지 확인
      if (fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase())) {
        File saveFile = new File(projectPath, fileName);
        try {
          file.transferTo(saveFile);
        } catch (Exception e) {
          throw new ImageSaveException();
        }
      } else {
        // 이미지 파일이 아닌 경우에 대한 처리
        throw new UnsupportedImageTypeException();
      }

      member = SignupDto.signupInput(request, projectPath);
      memberRepository.save(member);
    }

    SignupDto.Response response = SignupDto.Response.builder()
        .email(member.getEmail())
        .success(true)
        .build();

    return response;
  }

  @Override
  public TokenDto signin(SigninDto signinDto) {

    Member member = memberRepository.findByEmail(signinDto.getEmail())
        .orElseThrow(() -> new NotFoundMemberException());

    // 회원 상태에 따른 exception
    if (member.getStatus().equals(MemberStatus.SUSPENDED)) {
      throw new SuspendedMemberException();
    } else if (member.getStatus().equals(MemberStatus.WITHDRAWN)) {
      throw new NotFoundMemberException();
    }

    // 비밀번호 확인
    if (!member.getPassword().equals(signinDto.getPassword())) {
      throw new InvalidPasswordException();
    }

    // 토큰 생성
    TokenDto tokens = jwtTokenProvider.createTokens(member.getEmail(), member.getMemberId(),
        member.getNickname());

    // Redis에 RefreshToken 저장
    String refreshToken = tokens.getRefreshToken();
    redisTemplate.opsForValue()
        .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
            TimeUnit.MILLISECONDS);

    return tokens;
  }

  @Override
  public CheckDto checkEmail(String email) {

    // 이메일 체크
    if (!isValidEmail(email)) {
      throw new InvalidEmailException();
    }

    Optional<Member> byEmail = memberRepository.findByEmail(email);
    if (byEmail.isPresent()) {
      throw new DuplicateEmailException();
    }

    return CheckDto.builder().success(true).build();
  }

  @Override
  public CheckDto checkNickname(String nickname) {

    // 닉네임 생성 규칙에 맞는지 확인
    if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
      throw new InvalidNicknameException();
    }

    String redisKey = "Nickname: " + nickname;
    String email = (String) redisTemplate.opsForValue().get(redisKey);
    Optional<Member> byNickname = memberRepository.findByNickname(nickname);

    // Redis나 DB에 저장되있는 닉네임일 경우 Exception
    if (email != null || byNickname.isPresent()) {
      throw new DuplicateNicknameException();
    }

    // Redis에 임시저장
    redisTemplate.opsForValue()
        .set("Nickname: " + nickname, nickname, NICKNAME_EXPIRES_IN, TimeUnit.MILLISECONDS);

    return CheckDto.builder().success(true).build();
  }

  private boolean isValidEmail(String email) {
    return EmailValidator.getInstance().isValid(email);
  }
}
