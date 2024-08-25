package com.fantion.backend.member.controller;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.exception.ErrorResponse;
import com.fantion.backend.common.dto.CheckDto;
import com.fantion.backend.member.dto.MemberDto;
import com.fantion.backend.member.dto.MyBalanceDto;
import com.fantion.backend.member.dto.ProfileImageResponseDto;
import com.fantion.backend.member.dto.RatingRequestDto;
import com.fantion.backend.member.dto.ResetPasswordDto;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@Tag(name = "Member", description = "Member Service API")
public class MemberController {

  private final MemberService memberService;

  @Operation(summary = "회원가입", description = "회원가입 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원가입에 성공했습니다."),
      @ApiResponse(responseCode = "400", description =
          "유효하지 않은 이메일 입니다.<br>이미 가입된 계정 입니다.<br>다른 이메일과 소셜계정 연동한 이메일 입니다."
              + "<br>중복된 닉네임 입니다.<br>지원되지 않는 이미지 파일 형식입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "이미지 저장에 실패 했습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ResultDTO<SignupDto.SignupResponse>> signup(
      @RequestPart(value = "request") @Valid SignupDto.SignupRequest request,
      @RequestPart(value = "file", required = false) MultipartFile file) {
    ResultDTO<SignupDto.SignupResponse> result = memberService.signup(request, file);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "이메일 중복 확인", description = "이메일 중복 확인 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용가능한 이메일 입니다."),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 이메일 입니다.<br>이미 가입된 계정 입니다.<br>다른 이메일과 소셜계정 연동한 이메일 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @Parameter(name = "email", description = "이메일 중복을 확인하려는 이메일", example = "test@email.com")
  @GetMapping("/check-email")
  public ResponseEntity<ResultDTO<CheckDto>> checkEmail(
      @RequestParam(value = "email") @Email(message = "이메일 형식이 올바르지 않습니다.")
      @NotBlank(message = "이메일은 공백일 수 없습니다.") @NotNull(message = "이메일은 null 값일 수 없습니다.") String email) {
    ResultDTO<CheckDto> result = memberService.checkEmail(email);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 확인 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "사용가능한 닉네임 입니다."),
      @ApiResponse(responseCode = "400", description = "닉네임이 유효하지 않습니다.<br>중복된 닉네임 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @Parameter(name = "nickname", description = "닉네임 중복을 확인하려는 닉네임", example = "닉네임")
  @GetMapping("/check-nickname")
  public ResponseEntity<ResultDTO<CheckDto>> checkNickname(
      @RequestParam(value = "nickname") @NotBlank(message = "닉네임은 공백일 수 없습니다.")
      @NotNull(message = "닉네임은 null 값일 수 없습니다.") String nickname) {
    ResultDTO<CheckDto> result = memberService.checkNickname(nickname);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "로그인", description = "로그인 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "존재하지 않는 회원입니다.<br>계정정지 조치된 회원입니다.<br>잘못된 비밀번호 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/signin")
  public ResponseEntity<ResultDTO<TokenDto.Local>> Signin(@RequestBody @Valid SigninDto signinDto) {
    ResultDTO<TokenDto.Local> result = memberService.signin(signinDto);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "네이버 로그인 페이지 요청", description = "네이버 로그인 페이지를 요청 할 때 사용하는 API")
  @GetMapping("/naver/request")
  public RedirectView naverRequest() {
    RedirectView result = memberService.naverRequest();
    return result;
  }

  @Operation(summary = "네이버 로그인", description = "네이버 로그인 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "네이버 로그인에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "계정정지 조치된 회원입니다.<br>이미 다른 소셜계정으로 연동 하셨습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @Parameter(name = "code", description = "네이버 로그인 성공시 리다이렉트 되면 주는 String 값")
  @GetMapping("/naver/signin")
  public ResponseEntity<ResultDTO<TokenDto.Local>> naverSignin(
      @RequestParam(value = "code") String code) {
    ResultDTO<TokenDto.Local> result = memberService.neverSignin(code);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "네이버 연동 메일 요청", description = "네이버 연동 메일 요청 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "네이버 연동 이메일 발송에 성공했습니다."),
      @ApiResponse(responseCode = "400", description =
          "이미 가입한 이메일 입니다.<br>다른 이메일과 소셜계정 연동한 이메일 입니다.<br>유효하지 않는 이메일 입니다."
              + "이미 다른 소셜계정으로 연동 하셨습니다.<br>이미 해당 소셜계정과 연동하셨습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/naver/link")
  public ResponseEntity<ResultDTO<CheckDto>> naverLinkRequest(@RequestParam String linkEmail) {
    ResultDTO<CheckDto> result = memberService.naverLinkEmail(linkEmail);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "네이버 연동", description = "네이버 연동 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "네이버 연동에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "SNS 계정 연동 시간이 만료되었습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @Parameter(name = "linkEmail", description = "계정과 연동하려는 네이버 이메일", example = "test@naver.com")
  @PutMapping("/naver/link")
  public ResponseEntity<ResultDTO<CheckDto>> naverLink(
      @RequestParam(value = "linkEmail") @Email(message = "이메일 형식이 올바르지 않습니다.") @NotBlank(message = "이메일은 공백일 수 없습니다.")
      @NotNull(message = "이메일은 null 값일 수 없습니다.") String linkEmail,
      @RequestParam(value = "uuid") @NotBlank(message = "UUID는 공백일 수 없습니다.")
      @NotNull(message = "UUID는 null 값일 수 없습니다.") String uuid) {
    ResultDTO<CheckDto> result = memberService.naverLink(linkEmail, uuid);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "네이버 연동해제", description = "네이버 연동해제 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "네이버 연동해제에 성공했습니다."),
      @ApiResponse(responseCode = "404", description = "연동한 이메일이 없습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "연동해제 중 에러가 발생했습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/naver/unlink")
  public ResponseEntity<ResultDTO<CheckDto>> naverUnlink() {
    ResultDTO<CheckDto> result = memberService.naverUnlink();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "로그아웃", description = "로그아웃 할 때 사용하는 API")
  @ApiResponse(responseCode = "200", description = "로그아웃에 성공했습니다.")
  @PostMapping("/signout")
  public ResponseEntity<ResultDTO<CheckDto>> signout() {
    ResultDTO<CheckDto> result = memberService.signout();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "회원탈퇴", description = "회원탈퇴 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원탈퇴에 성공했습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "연동해제 중 에러가 발생했습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/withdrawal")
  public ResponseEntity<ResultDTO<CheckDto>> withdrawal() {
    ResultDTO<CheckDto> result = memberService.withdrawal();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "내 회원정보 보기", description = "내 회원정보를 조회 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원정보를 불러오는데 성공했습니다"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.<br>예치금이 존재하지 않습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/my-info")
  public ResponseEntity<ResultDTO<MemberDto.MemberResponse>> myInfo() {
    ResultDTO<MemberDto.MemberResponse> result = memberService.myInfo();
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "내 회원정보 수정", description = "내 회원정보를 수정 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원정보 수정에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "중복된 닉네임 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/my-info")
  public ResponseEntity<ResultDTO<CheckDto>> myInfoEdit(
      @RequestBody @Valid MemberDto.MemberUpdateRequest request) {
    ResultDTO<CheckDto> result = memberService.myInfoEdit(request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "내 프로필 이미지 변경", description = "내 프로필 이미지를 변경 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "프로필 이미지 변경에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 파일 경로입니다.<br>지원되지 않는 이미지 파일 형식입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "403", description = "권한이 없어 접근이 불가능한 이미지입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.<br>이미지가 존재하지 않습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "이미지 저장에 실패 했습니다.<br>이미지 내부 서버 오류입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ResultDTO<ProfileImageResponseDto>> profileImageEdit(
      @RequestPart(value = "file") MultipartFile file) {
    ResultDTO<ProfileImageResponseDto> result = memberService.profileImageEdit(file);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "비밀번호 변경 메일 요청", description = "비밀번호 변경 메일 요청 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "비밀번호 변경 이메일 발송에 성공했습니다."),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "메일전송에 실패했습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/reset-password-request")
  public ResponseEntity<ResultDTO<CheckDto>> resetPasswordRequest(
      @RequestBody @Valid ResetPasswordDto.MailRequest Request) {
    ResultDTO<CheckDto> result = memberService.resetPasswordEmail(Request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "비밀번호 변경", description = "비밀번호 변경 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "비밀번호 변경에 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "비밀번호 변경 기간이 만료되었습니다.<br>기존과 동일한 비밀번호로 변경할 수 없습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/reset-password")
  public ResponseEntity<ResultDTO<CheckDto>> resetPassword(
      @Valid @RequestBody ResetPasswordDto.ChangeRequest request) {
    ResultDTO<CheckDto> result = memberService.resetPassword(request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "판매자 평점부여", description = "판매자 평점부여 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "해당 경매에 대해 판매자에게 평점을 부여했습니다."),
      @ApiResponse(responseCode = "400", description = "존재하지 않는 경매입니다.<br>구매자가 아닙니다."
          + "<br>이미 해당 경매건에 평점을 매겼습니다.<br>인수확인이 아직 완료되지 않았습니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PutMapping("/rating")
  public ResponseEntity<ResultDTO<CheckDto>> rating(@RequestBody RatingRequestDto request) {
    ResultDTO<CheckDto> result = memberService.rating(request);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "기간별 예치금 내역 보기", description = "기간별 예치금 내역 보기를 할 때 사용하는 API")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "예치금 내역 불러오기를 성공했습니다."),
      @ApiResponse(responseCode = "400", description = "유효하지 않은 검색범위 입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/my-balance/{searchOption}")
  public ResponseEntity<ResultDTO<Page<MyBalanceDto>>> myBalance(
      @PathVariable(value = "searchOption") String searchOption,
      @RequestParam(value = "page", defaultValue = "0") Integer pageNumber) {
    ResultDTO<Page<MyBalanceDto>> result = memberService.myBalance(searchOption, pageNumber);
    return ResponseEntity.ok(result);
  }
}
