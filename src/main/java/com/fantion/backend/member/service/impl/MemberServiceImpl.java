package com.fantion.backend.member.service.impl;

import com.fantion.backend.exception.impl.DuplicateEmailException;
import com.fantion.backend.exception.impl.DuplicateLinkException;
import com.fantion.backend.exception.impl.DuplicateNicknameException;
import com.fantion.backend.exception.impl.ImageSaveException;
import com.fantion.backend.exception.impl.InvalidEmailException;
import com.fantion.backend.exception.impl.InvalidNicknameException;
import com.fantion.backend.exception.impl.InvalidPasswordException;
import com.fantion.backend.exception.impl.LinkedEmailException;
import com.fantion.backend.exception.impl.NotFoundMemberException;
import com.fantion.backend.exception.impl.OtherSnsLinkException;
import com.fantion.backend.exception.impl.SnsNotLinkedException;
import com.fantion.backend.exception.impl.SuspendedMemberException;
import com.fantion.backend.exception.impl.UnsupportedImageTypeException;
import com.fantion.backend.member.configuration.NaverConfiguration;
import com.fantion.backend.member.configuration.NaverLoginClient;
import com.fantion.backend.member.configuration.NaverProfileClient;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.NaverMemberDto;
import com.fantion.backend.member.dto.NaverMemberDto.NaverMemberDetail;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.SignupDto.Request;
import com.fantion.backend.member.dto.SignupDto.Response;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.dto.TokenDto.Local;
import com.fantion.backend.member.dto.TokenDto.Naver;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.jwt.JwtTokenProvider;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.service.MemberService;
import com.fantion.backend.type.MemberStatus;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final NaverLoginClient naverLoginClient;
  private final NaverProfileClient naverProfileClient;
  private final NaverConfiguration naverConfiguration;
  private final EmailValidator emailValidator = EmailValidator.getInstance();
  private final Random random = new Random();

  @Override
  public Response signup(Request request, MultipartFile file) {

    // 이메일 체크
    if (!emailValidator.isValid(request.getEmail())) {
      throw new InvalidEmailException();
    }

    // 중복가입 체크
    memberRepository.findByEmail(request.getEmail()).ifPresent(member -> {
      // 탈퇴 상태가 아니면 중복가입 exception
      if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
        throw new DuplicateEmailException();
      }

      // 탈퇴했던 회원이 재가입할 때 기존 데이터를 삭제하고 재가입 시킬지
      // 기존 데이터를 업데이트 하는 식으로 가입 시킬지 회의 후 결정
    });

    // 연동 된 email인지 체크
    memberRepository.findByLinkedEmail(request.getEmail()).ifPresent(member -> {
      // 탈퇴 상태가 아니면 중복 가입 exception
      if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
        throw new LinkedEmailException();
      }
    });

    // 닉네임이 연동된 회원인지 체크
    memberRepository.findByNickname(request.getNickname()).ifPresent(member -> {
      // 회원 상태가 활성 상태이면 중복 닉네임 exception
      if (member.getStatus().equals(MemberStatus.ACTIVE)) {
        throw new DuplicateNicknameException();
      }
    });

    Member member;
    // 멤버정보 DB에 저장
    if (file == null || file.isEmpty()) { // 이미지 파일이 없을 때
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

    return Response.builder()
        .email(member.getEmail())
        .success(true)
        .build();
  }


  @Override
  public CheckDto checkEmail(String email) {

    // 이메일 체크
    if (!emailValidator.isValid(email)) {
      throw new InvalidEmailException();
    }

    memberRepository.findByEmail(email).ifPresent(member -> {
      throw new DuplicateEmailException();
    });

    return CheckDto.builder()
        .success(true)
        .build();
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

    return CheckDto.builder()
        .success(true)
        .build();
  }

  @Override
  public Local signin(SigninDto signinDto) {

    Member member = memberRepository.findByEmail(signinDto.getEmail())
        .orElseThrow(NotFoundMemberException::new);

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
    Local tokens = jwtTokenProvider.createTokens(member.getEmail(), member.getMemberId(),
        member.getNickname());

    // Redis에 RefreshToken 저장
    String refreshToken = tokens.getRefreshToken();
    redisTemplate.opsForValue()
        .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
            TimeUnit.MILLISECONDS);

    return tokens;
  }

  @Override
  public String naverRequest() {
    ResponseEntity<String> response = naverLoginClient.naverRequest("code",
        naverConfiguration.getClientId(), naverConfiguration.getState(),
        naverConfiguration.getRedirectUri());
    return response.getBody();
  }

  @Override
  public Local neverSignin(String code) {
    // 네이버 토큰 가져오기
    ResponseEntity<Naver> naverTokens = naverLoginClient.getToken("authorization_code",
        naverConfiguration.getClientId(),
        naverConfiguration.getClientSecret(), code, naverConfiguration.getState());

    // 가져온 토큰으로 프로필 정보 가져오기
    String accessToken = "Bearer " + naverTokens.getBody().getAccessToken();
    ResponseEntity<NaverMemberDto> profile = naverProfileClient.getProfile(accessToken);
    NaverMemberDetail profileDto = profile.getBody().getNaverMemberDetail();

    // 연동 이메일 찾아오기
    Optional<Member> byEmail = memberRepository.findByLinkedEmail(profileDto.getEmail());
    if (!byEmail.isPresent()) { // 비회원
      String nickname = profileDto.getNickname();
      Optional<Member> byNickname = memberRepository.findByNickname(nickname);
      while (byNickname.isPresent()) {
        int randomNumber = random.nextInt(100);
        String newNickname = nickname + randomNumber;
        byNickname = memberRepository.findByNickname(newNickname);
        nickname = newNickname; // 중복이 없을 때까지 새로운 닉네임으로 갱신
      }

      // 신규 회원 가입 진행
      String password = UUID.randomUUID().toString();
      Member member = Member.builder()
          .email(profileDto.getEmail())
          .password(password)
          .nickname(nickname)
          .auth(true)
          .isKakao(false)
          .isNaver(true)
          .address("주소를 설정해주세요.")
          .phoneNumber(profileDto.getMobile())
          .totalRating(0)
          .ratingCnt(0)
          .rating(0)
          .status(MemberStatus.ACTIVE)
          .profileImage(profileDto.getProfileImage())
          .linkedEmail(profileDto.getEmail())
          .createDate(LocalDateTime.now())
          .build();
      memberRepository.save(member);

      Local tokens = jwtTokenProvider.createTokens(member.getEmail(), member.getMemberId(),
          member.getNickname());

      // Redis에 RefreshToken 저장
      String refreshToken = tokens.getRefreshToken();
      redisTemplate.opsForValue()
          .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
              TimeUnit.MILLISECONDS);

      return tokens;
    }

    // 회원인 경우 처리
    Member member = byEmail.get();
    if (member.getStatus().equals(MemberStatus.SUSPENDED)) { // 정지된 회원
      throw new SuspendedMemberException();
    } else if (member.getStatus().equals(MemberStatus.WITHDRAWN)) { // 탈퇴한 회원
      // 탈퇴했던 회원이 재가입할 때 기존 데이터를 삭제하고 재가입 시킬지
      // 기존 데이터를 업데이트 하는 식으로 가입 시킬지 회의 후 결정
    } else if (!member.getIsNaver()) { // 연동안한 회원
      throw new SnsNotLinkedException();
    } else if (member.getIsKakao()) { // 다른 소셜계정 연동 회원
      throw new OtherSnsLinkException();
    }

    // 연동한 회원인 경우
    Local tokens = jwtTokenProvider.createTokens(member.getEmail(),
        member.getMemberId(),
        member.getNickname());

    // Redis에 RefreshToken 저장
    String refreshToken = tokens.getRefreshToken();
    redisTemplate.opsForValue()
        .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
            TimeUnit.MILLISECONDS);

    return tokens;
  }


  @Override
  public CheckDto naverLink(String linkEmail) {

    // 현재 토큰에 저장된 email 가져오기
    String currentEmail = getCurrentEmail();
    Member member = memberRepository.findByEmail(currentEmail)
        .orElseThrow(NotFoundMemberException::new);

    // 소셜 계정이 이미 가입한 email인지 확인
    memberRepository.findByEmail(linkEmail).ifPresent(snsMember -> {
      if (!snsMember.getStatus().equals(MemberStatus.WITHDRAWN)) { // 회원 상태가 탈퇴가 아닐 때
        throw new DuplicateEmailException();
      }
    });

    // 다른 email에 연동이 된 소셜 계정인지 확인
    memberRepository.findByLinkedEmail(linkEmail).ifPresent(snsMember -> {
      if (!snsMember.getStatus().equals(MemberStatus.WITHDRAWN)) { // 회원 상태가 탈퇴가 아닐 때
        throw new LinkedEmailException();
      }
    });

    // 카카오나 이미 연동한 회원일 경우 exception
    if (member.getIsKakao()) {
      throw new OtherSnsLinkException();
    }

    if (member.getIsNaver()) {
      throw new DuplicateLinkException();
    }

    Member updateMember = member.toBuilder()
        .auth(true)
        .isNaver(true)
        .linkedEmail(linkEmail)
        .build();
    memberRepository.save(updateMember);

    return CheckDto.builder()
        .success(true)
        .build();
  }

  public static String getCurrentEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication.getName() != null) {
      return authentication.getName();
    }
    return null;
  }
}
