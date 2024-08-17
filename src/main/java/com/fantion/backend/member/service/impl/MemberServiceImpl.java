package com.fantion.backend.member.service.impl;

import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.common.component.MailComponents;
import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.repository.PostRepository;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.configuration.NaverConfiguration;
import com.fantion.backend.member.configuration.NaverLoginClient;
import com.fantion.backend.member.configuration.NaverProfileClient;
import com.fantion.backend.member.dto.CheckDto;
import com.fantion.backend.member.dto.MemberDto;
import com.fantion.backend.member.dto.MyBalanceDto;
import com.fantion.backend.member.dto.NaverLinkDto;
import com.fantion.backend.member.dto.NaverMemberDto;
import com.fantion.backend.member.dto.ProfileImageResponseDto;
import com.fantion.backend.member.dto.RatingRequestDto;
import com.fantion.backend.member.dto.ResetPasswordDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import com.fantion.backend.member.entity.RatingHistory;
import com.fantion.backend.member.jwt.JwtTokenProvider;
import com.fantion.backend.member.repository.BalanceHistoryRepository;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.repository.MoneyRepository;
import com.fantion.backend.member.repository.RatingHistoryRepository;
import com.fantion.backend.member.service.MemberService;
import com.fantion.backend.type.MemberStatus;
import com.fantion.backend.type.PostStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class MemberServiceImpl implements MemberService {

  private static final Long REFRESH_TOKEN_EXPIRES_IN = 86400000L;
  private static final Long NICKNAME_EXPIRES_IN = 300000L;
  private static final Long MAIL_EXPIRES_IN = 300000L;
  private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]{1,12}$");

  private final MemberRepository memberRepository;
  private final AuctionRepository auctionRepository;
  private final MoneyRepository moneyRepository;
  private final RatingHistoryRepository ratingHistoryRepository;
  private final BalanceHistoryRepository balanceHistoryRepository;
  private final PostRepository postRepository;

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private final NaverLoginClient naverLoginClient;
  private final NaverProfileClient naverProfileClient;
  private final NaverConfiguration naverConfiguration;
  private final EmailValidator emailValidator = EmailValidator.getInstance();
  private final Random random = new Random();
  private final HttpServletRequest httpServletRequest;
  private final S3Uploader s3Uploader;
  private final PasswordEncoder passwordEncoder;
  private final MailComponents mailComponents;


  @Override
  @Transactional
  public ResultDTO<SignupDto.SignupResponse> signup(SignupDto.SignupRequest request,
      MultipartFile file) {

    // 이메일 체크
    if (!emailValidator.isValid(request.getEmail())) {
      throw new CustomException(ErrorCode.EMAIL_INVALID);
    }

    // 중복가입 체크
    memberRepository.findByEmail(request.getEmail()).ifPresent(member -> {
      throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
    });

    // 연동 된 email인지 체크
    memberRepository.findByLinkedEmail(request.getEmail()).ifPresent(member -> {
      // 탈퇴 상태가 아니면 중복 가입 exception
      if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
        throw new CustomException(ErrorCode.LINKED_EMAIL_ERROR);
      }
    });

    // 닉네임이 연동된 회원인지 체크
    memberRepository.findByNickname(request.getNickname()).ifPresent(member -> {
      // 회원 상태가 활성 상태이면 중복 닉네임 exception
      if (member.getStatus().equals(MemberStatus.ACTIVE)) {
        throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
      }
    });

    Member member;
    // 멤버정보 DB에 저장
    if (file == null || file.isEmpty()) { // 이미지 파일이 없을 때
      member = SignupDto.signupInput(request, null);
      memberRepository.save(member);
    } else { // 이미지 파일이 있을 때
      // 파일 이름에서 확장자 추출
      String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

      // 지원하는 이미지 파일 확장자 목록
      List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

      String imageUrl;
      // 확장자가 이미지 파일인지 확인
      if (fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase())) {
        try {
          imageUrl = s3Uploader.upload(file, "profile-images");
        } catch (Exception e) {
          throw new CustomException(ErrorCode.FAILED_IMAGE_SAVE);
        }
      } else {
        // 이미지 파일이 아닌 경우에 대한 처리
        throw new CustomException(ErrorCode.UN_SUPPORTED_IMAGE_TYPE);
      }

      member = SignupDto.signupInput(request, imageUrl);
      memberRepository.save(member);
    }

    // 회원가입시 회원 예치금 생성
    Money money = Money.builder()
        .member(member)
        .balance(0L)
        .build();
    moneyRepository.save(money);

    SignupDto.SignupResponse response = SignupDto.SignupResponse.builder()
        .email(member.getEmail())
        .success(true)
        .build();

    return ResultDTO.of("회원가입에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<CheckDto> checkEmail(String email) {

    // 이메일 체크
    if (!emailValidator.isValid(email)) {
      throw new CustomException(ErrorCode.EMAIL_INVALID);
    }

    memberRepository.findByEmail(email).ifPresent(member -> {
      throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
    });

    memberRepository.findByLinkedEmail(email).ifPresent(member -> {
      if (!member.getStatus().equals(MemberStatus.WITHDRAWN)) {
        throw new CustomException(ErrorCode.LINKED_EMAIL_ERROR);
      }
    });

    return ResultDTO.of("사용가능한 이메일 입니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<CheckDto> checkNickname(String nickname) {

    // 닉네임 생성 규칙에 맞는지 확인
    if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
      throw new CustomException(ErrorCode.NICKNAME_INVALID);
    }

    String redisKey = "Nickname: " + nickname;
    String email = redisTemplate.opsForValue().get(redisKey);
    Optional<Member> byNickname = memberRepository.findByNickname(nickname);

    // Redis나 DB에 저장되있는 닉네임일 경우 Exception
    if (email != null || byNickname.isPresent()) {
      throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
    }

    // Redis에 임시저장
    redisTemplate.opsForValue()
        .set("Nickname: " + nickname, nickname, NICKNAME_EXPIRES_IN, TimeUnit.MILLISECONDS);

    return ResultDTO.of("사용가능한 닉네임 입니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<TokenDto.Local> signin(SigninDto signinDto) {

    Member member = memberRepository.findByEmail(signinDto.getEmail())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 회원 상태에 따른 exception
    if (member.getStatus().equals(MemberStatus.SUSPENDED)) {
      throw new CustomException(ErrorCode.SUSPENDED_MEMBER);
    } else if (member.getStatus().equals(MemberStatus.WITHDRAWN)) {
      throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
    }

    // 비밀번호 확인
    if (!passwordEncoder.matches(signinDto.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.PASSWORD_INVALID);
    }

    // 토큰 생성
    TokenDto.Local tokens = jwtTokenProvider.createTokens(member.getEmail(), member.getNickname());

    // Redis에 RefreshToken 저장
    String refreshToken = tokens.getRefreshToken();
    redisTemplate.opsForValue()
        .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
            TimeUnit.MILLISECONDS);

    return ResultDTO.of("로그인에 성공했습니다.", tokens);
  }

  @Override
  @Transactional
  public RedirectView naverRequest() {
    String redirectUrl = "https://nid.naver.com/oauth2.0/authorize";
    String responseType = "code";
    String clientId = naverConfiguration.getClientId();
    String state = naverConfiguration.getState();
    String redirectUri = naverConfiguration.getRedirectUri();

    // 클라이언트에서 사용하기 위해 URL 생성
    String authUrl = String.format("%s?response_type=%s&client_id=%s&state=%s&redirect_uri=%s",
        redirectUrl, responseType, clientId, state, redirectUri);

    RedirectView redirectView = new RedirectView();
    redirectView.setUrl(authUrl);
    return redirectView;
  }

  @Override
  public ResultDTO<TokenDto.Local> neverSignin(String code) {
    // 네이버 토큰 가져오기
    ResponseEntity<TokenDto.Naver> naverTokens = naverLoginClient.getToken("authorization_code",
        naverConfiguration.getClientId(),
        naverConfiguration.getClientSecret(), code, naverConfiguration.getState());

    // 가져온 토큰으로 프로필 정보 가져오기
    String accessToken = "Bearer " + naverTokens.getBody().getAccessToken();
    ResponseEntity<NaverMemberDto> profile = naverProfileClient.getProfile(accessToken);
    NaverMemberDto.NaverMemberDetail profileDto = profile.getBody().getNaverMemberDetail();

    // 연동 이메일 찾아오기
    Optional<Member> byEmail = memberRepository.findByLinkedEmail(profileDto.getEmail());

    if (byEmail.isEmpty()) { // 비회원
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
      String encPassword = BCrypt.hashpw(password, BCrypt.gensalt());
      Member member = Member.builder()
          .email(profileDto.getEmail())
          .password(encPassword)
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

      // 회원가입시 회원 예치금 생성
      Money money = Money.builder()
          .member(member)
          .balance(0L)
          .build();
      moneyRepository.save(money);

      TokenDto.Local tokens = jwtTokenProvider.createTokens(member.getEmail(),
          member.getNickname());

      // Redis에 RefreshToken 저장
      String refreshToken = tokens.getRefreshToken();
      redisTemplate.opsForValue()
          .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
              TimeUnit.MILLISECONDS);

      // Redis에 네이버 AccessToken 저장
      String naverAccessToken = naverTokens.getBody().getAccessToken();
      Long naverExpiresIn = Long.valueOf(naverTokens.getBody().getExpiresIn());
      redisTemplate.opsForValue()
          .set("naverAcessTokenEmail: " + member.getEmail(), naverAccessToken, naverExpiresIn,
              TimeUnit.SECONDS);

      return ResultDTO.of("네이버 로그인에 성공했습니다.", tokens);
    }

    // 회원인 경우 처리
    Member member = byEmail.get();
    if (member.getStatus().equals(MemberStatus.SUSPENDED)) { // 정지된 회원
      throw new CustomException(ErrorCode.SUSPENDED_MEMBER);
    } else if (member.getIsKakao()) { // 다른 소셜계정 연동 회원
      throw new CustomException(ErrorCode.OTHER_SNS_LINKED_ERROR);
    }

    // 연동한 회원인 경우
    TokenDto.Local tokens = jwtTokenProvider.createTokens(member.getEmail(), member.getNickname());

    // Redis에 RefreshToken 저장
    String refreshToken = tokens.getRefreshToken();
    redisTemplate.opsForValue()
        .set("RefreshToken: " + member.getEmail(), refreshToken, REFRESH_TOKEN_EXPIRES_IN,
            TimeUnit.MILLISECONDS);

    // Redis에 네이버 AccessToken 저장
    String naverAccessToken = naverTokens.getBody().getAccessToken();
    Long naverExpiresIn = Long.valueOf(naverTokens.getBody().getExpiresIn());
    redisTemplate.opsForValue()
        .set("naverAcessTokenEmail: " + member.getEmail(), naverAccessToken, naverExpiresIn,
            TimeUnit.SECONDS);

    return ResultDTO.of("네이버 로그인에 성공했습니다.", tokens);
  }


  @Override
  public ResultDTO<CheckDto> naverLink(String linkEmail) {

    // 네이버 계정인지 확인
    String string = linkEmail.split("@")[1];
    if (!string.equals("naver.com")) {
      throw new CustomException(ErrorCode.EMAIL_INVALID);
    }

    // 현재 토큰에 저장된 email 가져오기
    String currentEmail = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(currentEmail)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 소셜 계정이 이미 가입한 email인지 확인
    memberRepository.findByEmail(linkEmail).ifPresent(snsMember -> {
      if (!snsMember.getStatus().equals(MemberStatus.WITHDRAWN)) { // 회원 상태가 탈퇴가 아닐 때
        throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
      }
    });

    // 다른 email에 연동이 된 소셜 계정인지 확인
    memberRepository.findByLinkedEmail(linkEmail).ifPresent(snsMember -> {
      if (!snsMember.getStatus().equals(MemberStatus.WITHDRAWN)) { // 회원 상태가 탈퇴가 아닐 때
        throw new CustomException(ErrorCode.LINKED_EMAIL_ERROR);
      }
    });

    // 카카오나 이미 연동한 회원일 경우 exception
    if (member.getIsKakao()) {
      throw new CustomException(ErrorCode.OTHER_SNS_LINKED_ERROR);
    }

    if (member.getIsNaver()) {
      throw new CustomException(ErrorCode.DUPLICATE_LINKED_ERROR);
    }

    Member updateMember = member.toBuilder()
        .auth(true)
        .isNaver(true)
        .linkedEmail(linkEmail)
        .build();
    memberRepository.save(updateMember);

    return ResultDTO.of("네이버 연동에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<CheckDto> naverUnlink() {

    String currentEmail = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(currentEmail)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (member.getLinkedEmail() == null || member.getLinkedEmail().isEmpty()) {
      throw new CustomException(ErrorCode.NOT_FOUND_LINKED_EMAIL);
    }

    Member updateMember = member.toBuilder()
        .auth(false)
        .isNaver(false)
        .linkedEmail(null)
        .build();
    memberRepository.save(updateMember);

    String naverAccessToken = redisTemplate.opsForValue()
        .get("naverAcessTokenEmail: " + member.getEmail());
    ResponseEntity<NaverLinkDto> delete = naverLoginClient.unLink(
        naverConfiguration.getClientId(),
        naverConfiguration.getClientSecret(), naverAccessToken, "delete");

    if (!delete.getBody().getResult().equals("success")) {
      throw new CustomException(ErrorCode.UN_LINKED_ERROR);
    }

    return ResultDTO.of("네이버 연동해제에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  @Transactional
  public ResultDTO<CheckDto> signout() {

    String email = MemberAuthUtil.getLoginUserId();
    String accessToken = jwtTokenProvider.resolveToken(httpServletRequest);

    // Redis에 해당 유저의 email로 저장된 refreshToken이 있는지 확인 후 있으면 삭제
    if (redisTemplate.opsForValue()
        .get("RefreshToken: " + email) != null) {
      redisTemplate.delete("RefreshToken: " + email);
    }

    // 해당 accessToken 유효시간을 가지고 와서 Redis에 BlackList로 추가
    long expiration = jwtTokenProvider.getExpiration(accessToken);
    long now = (new Date()).getTime();
    long accessTokenExpiresIn = expiration - now;
    redisTemplate.opsForValue()
        .set(accessToken, "logout", accessTokenExpiresIn, TimeUnit.MILLISECONDS);

    return ResultDTO.of("로그아웃에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  @Transactional
  public ResultDTO<CheckDto> withdrawal() {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (!member.getStatus().equals(MemberStatus.ACTIVE)) {
      throw new CustomException(ErrorCode.NOT_FOUND_MEMBER);
    }

    // 연동 SNS가 있는지 확인
    if (member.getIsNaver()) { // 네이버 연동 해제
      String naverAccessToken = redisTemplate.opsForValue()
          .get("naverAcessTokenEmail: " + member.getEmail());
      ResponseEntity<NaverLinkDto> delete = naverLoginClient.unLink(
          naverConfiguration.getClientId(),
          naverConfiguration.getClientSecret(), naverAccessToken, "delete");

      if (!delete.getBody().getResult().equals("success")) {
        throw new CustomException(ErrorCode.UN_LINKED_ERROR);
      }
    }

    Member updateMember = member.toBuilder()
        .nickname("탈퇴한 회원" + member.getNickname())
        .status(MemberStatus.WITHDRAWN)
        .withdrawalDate(LocalDateTime.now())
        .build();
    memberRepository.save(updateMember);

    // 등록한 경매 삭제
    List<Auction> auctionList = auctionRepository.findAllByMemberId(member);
    if (!auctionList.isEmpty()) {
      for (Auction auction : auctionList) {
        auctionRepository.delete(auction);
      }
    }

    // 작성한 게시글 삭제
    List<Post> postList = postRepository.findAllByMemberId(member);
    if (!postList.isEmpty()) {
      for (Post post : postList) {
        if (!post.getStatus().equals(PostStatus.DELETE)) {
          postRepository.save(post.toBuilder()
              .status(PostStatus.DELETE)
              .build());
        }
      }
    }

    // 예치금 삭제
    Money money = moneyRepository.findByMemberId(member.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MONEY));
    moneyRepository.delete(money);

    return ResultDTO.of("회원탈퇴에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<MemberDto.MemberResponse> myInfo() {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    Money money = moneyRepository.findByMemberId(member.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MONEY));

    MemberDto.MemberResponse memberDto = MemberDto.MemberResponse.builder()
        .email(member.getEmail())
        .nickname(member.getNickname())
        .address(member.getAddress())
        .auth(member.getAuth())
        .rating(member.getRating())
        .profileImage(member.getProfileImage())
        .phoneNumber(member.getPhoneNumber())
        .balance(money.getBalance())
        .createDate(member.getCreateDate())
        .build();

    if (member.getIsNaver()) {
      memberDto.setAuthType("NAVER");
      memberDto.setLinkedEmail(member.getLinkedEmail());
    } else if (member.getIsKakao()) {
      memberDto.setAuthType("KAKAO");
    }

    return ResultDTO.of("회원정보를 불러오는데 성공했습니다", memberDto);
  }

  @Override
  @Transactional
  public ResultDTO<CheckDto> myInfoEdit(MemberDto.MemberUpdateRequest request) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 중복 닉네임 체크
    if (!member.getNickname().equals(request.getNickname())) {
      memberRepository.findByNickname(request.getNickname()).ifPresent(nickname -> {
        throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
      });
    }

    Member updateMember = member.toBuilder()
        .nickname(request.getNickname())
        .address(request.getAddress())
        .build();
    memberRepository.save(updateMember);

    // 변경되는 닉네임으로 경매 현재 입찰자 수정
    if (!member.getNickname().equals(request.getNickname())) {
      List<Auction> auctionList = auctionRepository.findAllByCurrentBidder(member.getNickname());
      for (Auction auction : auctionList) {
        Auction updateAuction = auction.toBuilder()
            .currentBidder(request.getNickname())
            .build();
        auctionRepository.save(updateAuction);
      }
    }

    return ResultDTO.of("회원정보 수정에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<ProfileImageResponseDto> profileImageEdit(MultipartFile file) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 기존 프로필 이미지 삭제
    if (member.getProfileImage() != null) {
      try {
        URL exProfileImageUrl = new URL(member.getProfileImage());
        String exProfileImage = exProfileImageUrl.getPath().substring(1);
        if (exProfileImage != null) {
          s3Uploader.deleteFile(exProfileImage);
        }
      } catch (MalformedURLException e) {
        throw new CustomException(ErrorCode.IMAGE_NOT_HAVE_PATH);
      }
    }

    Member updateMember;
    if (file == null || file.isEmpty()) {
      updateMember = member.toBuilder()
          .profileImage(null)
          .build();
    } else {
      String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
      List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
      String imageUrl;

      if (fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase())) {
        try {
          imageUrl = s3Uploader.upload(file, "profile-images");
        } catch (Exception e) {
          throw new CustomException(ErrorCode.FAILED_IMAGE_SAVE);
        }
      } else {
        throw new CustomException(ErrorCode.UN_SUPPORTED_IMAGE_TYPE);
      }
      updateMember = member.toBuilder()
          .profileImage(imageUrl)
          .build();
    }

    memberRepository.save(updateMember);

    return ResultDTO.of("프로필 이미지 변경에 성공했습니다.", ProfileImageResponseDto.builder()
        .success(true)
        .newProfileImageUrl(updateMember.getProfileImage())
        .build());
  }

  @Override
  public ResultDTO<CheckDto> resetPasswordEmail(ResetPasswordDto.MailRequest request) {

    memberRepository.findByEmailAndPhoneNumber(request.getEmail(), request.getPhoneNumber())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    String email = request.getEmail();
    String baseUrl = "https://fan-tion.vercel.app";
    String title = "Fan-Tion 계정 비빌번호 변경 이메일";
    String uuid = UUID.randomUUID().toString();
    String message = "<h3>Fan-Tion 계정 비빌번호 변경 링크입니다. 아래의 링크를 클릭하셔서 비밀번호 변경을 완료해주세요.</h3>" +
        "<div><a href='" + baseUrl + "/reset-password-page?uuid=" + uuid
        + "'> 비밀번호 변경 링크 </a></div>";
    mailComponents.sendMail(email, title, message);

    // redis에 uuid를 임시 저장
    redisTemplate.opsForValue()
        .set("PasswordAuth: " + uuid, email, MAIL_EXPIRES_IN, TimeUnit.MILLISECONDS);

    return ResultDTO.of("비밀번호 변경 이메일 발송에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<CheckDto> resetPassword(ResetPasswordDto.ChangeRequest request) {

    String email = redisTemplate.opsForValue().get("PasswordAuth: " + request.getUuid());
    if (email == null || email.isEmpty()) {
      throw new CustomException(ErrorCode.PASSWORD_RESET_TIMEOUT);
    }

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 기존 비밀번호랑 비교
    if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.PASSWORD_DUPLICATE);
    }

    String encPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt());

    Member updateMember = member.toBuilder()
        .password(encPassword)
        .build();
    memberRepository.save(updateMember);

    return ResultDTO.of("비밀번호 변경에 성공했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  @Transactional
  public ResultDTO<CheckDto> rating(RatingRequestDto request) {

    String email = MemberAuthUtil.getLoginUserId();
    Member buyer = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 그 경매의 실제 구매자인지 체크
    Auction auction = auctionRepository.findById(request.getAuctionId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

    if (!auction.getCurrentBidder().equals(buyer.getNickname())) {
      throw new CustomException(ErrorCode.NOT_AUCTION_BUYER);
    }

    // 이미 그 경매건에 대해 평점을 줬는지 체크
    ratingHistoryRepository.findByAuctionIdAndMemberId(auction.getAuctionId(), buyer)
        .ifPresent(item -> {
          throw new CustomException(ErrorCode.ALREADY_RATED);
        });

    // 인수확인이 된 경매인지 체크
    if (!auction.isReceiveChk()) {
      throw new CustomException(ErrorCode.NOT_CONFIRMED_RECEIVE);
    }

    RatingHistory ratingHistory = RatingHistory.builder()
        .auction(auction)
        .memberId(buyer)
        .build();
    ratingHistoryRepository.save(ratingHistory);

    Member seller = memberRepository.findById(auction.getMember().getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    int ratingCnt = seller.getRatingCnt() + 1;
    int totalRating = seller.getTotalRating() + request.getRating();

    Member updateSeller = seller.toBuilder()
        .ratingCnt(seller.getRatingCnt() + 1)
        .totalRating(totalRating)
        .rating(totalRating / ratingCnt)
        .build();
    memberRepository.save(updateSeller);

    return ResultDTO.of("해당 경매에 대해 판매자에게 평점을 부여했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<Page<MyBalanceDto>> myBalance(String searchOption, Integer pageNumber) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    LocalDateTime startDate = LocalDateTime.now();

    if (searchOption != null && !searchOption.isEmpty()) {
      switch (searchOption) {
        case "1months":
          startDate = startDate.minus(1, ChronoUnit.MONTHS);
          break;
        case "3months":
          startDate = startDate.minus(3, ChronoUnit.MONTHS);
          break;
        case "1year":
          startDate = startDate.minus(1, ChronoUnit.YEARS);
          break;
        default:
          throw new CustomException(ErrorCode.INVALID_SEARCH_SCOPE);
      }
    }

    Pageable pageable = PageRequest.of(pageNumber, 10);
    Page<MyBalanceDto> balanceHistoryPage = covertToResponseList(
        balanceHistoryRepository.findByMemberAndCreateDateAfter(member, startDate, pageable));

    return ResultDTO.of("예치금 내역 불러오기를 성공했습니다.", balanceHistoryPage);
  }

  private Page<MyBalanceDto> covertToResponseList(Page<BalanceHistory> balanceHistories) {
    List<MyBalanceDto> responseList = balanceHistories.stream().map(this::convertToMyBalanceDto)
        .collect(Collectors.toList());

    return new PageImpl<>(responseList, balanceHistories.getPageable(),
        balanceHistories.getTotalElements());
  }

  private MyBalanceDto convertToMyBalanceDto(BalanceHistory balanceHistory) {
    return MyBalanceDto.builder()
        .balance(balanceHistory.getBalance())
        .Type(balanceHistory.getType())
        .createTime(balanceHistory.getCreateDate())
        .build();
  }


  @Scheduled(cron = "0 0 0 * * ?")
  public void deleteWithdrawalMembers() {
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    List<Member> withdrawalMembers = memberRepository.findAllByWithdrawalDateBefore(thirtyDaysAgo);

    try {
      if (withdrawalMembers != null && !withdrawalMembers.isEmpty()) {
        memberRepository.deleteAll(withdrawalMembers);
      } else {
        // 나중에 로그 처리
      }
    } catch (Exception e) {
      // 나중에 로그 처리
    }
  }
}