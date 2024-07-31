package com.fantion.backend.payment.service.impl;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.exception.impl.TossAPIException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import com.fantion.backend.member.repository.BalanceHistoryRepository;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.repository.MoneyRepository;
import com.fantion.backend.payment.component.PaymentClient;
import com.fantion.backend.payment.component.PaymentComponent;
import com.fantion.backend.payment.dto.CancelDto;
import com.fantion.backend.payment.dto.ConfirmDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentResponseDto;
import com.fantion.backend.payment.dto.PaymentResponseDto.PaymentSuccess;
import com.fantion.backend.payment.entity.Payment;
import com.fantion.backend.payment.repository.PaymentRepository;
import com.fantion.backend.payment.service.PaymentService;
import com.fantion.backend.type.BalanceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private static final Long IDEMPOTENCY_KEY_EXPIRES_IN = 1296000000L;

  private final PaymentRepository paymentRepository;
  private final MemberRepository memberRepository;
  private final MoneyRepository moneyRepository;
  private final BalanceHistoryRepository balanceHistoryRepository;
  private final PaymentComponent paymentComponent;
  private final PaymentClient paymentClient;
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${payment.success-url}")
  private String successUrl;

  @Value("${payment.fail-url}")
  private String failUrl;

  @Override
  @Transactional
  public ResultDTO<PaymentDto.PaymentResponse> requestPayment(PaymentDto.PaymentRequest request) {

    String orderId = UUID.randomUUID().toString();

    String email = MemberAuthUtil.getCurrentEmail();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    // 결제 요청 정보를 DB에 저장
    Payment payment = Payment.builder()
        .memberId(member)
        .paymentType(request.getPaymentType())
        .amount(request.getAmount())
        .orderName(request.getOrderName())
        .orderId(orderId)
        .successYn(false)
        .paymentKey(null)
        .cancelYn(false)
        .cancelReason(null)
        .paymentDate(null)
        .cancelDate(null)
        .build();
    paymentRepository.save(payment);

    // 결제 정보 Response를 반환
    return ResultDTO.of("결제를 요청했습니다.", PaymentDto.of(payment, successUrl, failUrl));
  }

  @Override
  @Transactional
  public ResultDTO<PaymentResponseDto.PaymentSuccess> successPayment(String orderId, String paymentKey,
      Long amount) {

    // DB에 저장되어있는 값과 Param으로 들어온 값이 같은지 검증
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT_INFO));
    if (!payment.getAmount().equals(amount)) {
      throw new CustomException(ErrorCode.INVALID_PAYMENT_INFO);
    }

    // paymentKey와 성공여부를 true로 저장
    Payment updatePayment = payment.toBuilder()
        .paymentKey(paymentKey)
        .paymentDate(LocalDateTime.now())
        .successYn(true)
        .build();
    paymentRepository.save(updatePayment);

    // 토스 결제 승인 API를 호출
    ConfirmDto confirmDto = ConfirmDto.builder()
        .paymentKey(paymentKey)
        .orderId(orderId)
        .amount(amount)
        .build();

    String header = paymentComponent.createAuthorizationHeader();

    try {
      // 해당 회원의 예치금 충전
      String email = payment.getMemberId().getEmail();
      Member member = memberRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));
      Money money = moneyRepository.findByMemberId(member.getMemberId())
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MONEY));

      Money updateMoney = money.toBuilder()
          .balance(money.getBalance() + amount)
          .build();
      moneyRepository.save(updateMoney);

      // 예치금 내역에도 저장
      BalanceHistory balanceHistory = BalanceHistory.builder()
          .memberId(member)
          .balance(amount)
          .type(BalanceType.CHARGING)
          .createDate(updatePayment.getPaymentDate())
          .build();
      balanceHistoryRepository.save(balanceHistory);

      return ResultDTO.of("결제를 성공했습니다.", paymentClient.confirmPayment(header, confirmDto).getBody());
    } catch (FeignException fe) {
      return handleFeignException(fe);
    }
  }

  @Override
  public ResultDTO<PaymentResponseDto.PaymentFail> failPayment(String code, String message,
      String orderId) {

    PaymentResponseDto.PaymentFail failDto = PaymentResponseDto.PaymentFail.builder()
        .errorCode(code)
        .message(message)
        .orderId(orderId)
        .build();
    return ResultDTO.of("결제를 실패했습니다.", failDto);
  }

  @Override
  @Transactional
  public ResultDTO<PaymentResponseDto.PaymentSuccess> cancelPayment(CancelDto cancelDto) {

    // 나중에 결제 취소 규정을 정하고 그에 맞는 Valid 추가하기
    // 현재는 결제 취소 금액이 현재 가지고 있는 예치금보다 적은지와
    // 토스 결제 조회 API를 통해 결제 정보가 정말 있는지만 체크

    String email = MemberAuthUtil.getCurrentEmail();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    Payment payment = paymentRepository.findByAmountAndPaymentDate(cancelDto.getBalance(), cancelDto.getCreateTime())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT_INFO));
    String paymentKey = payment.getPaymentKey();

    // payment의 유저정보와 accessToken의 유저 정보가 틀릴경우
    if (payment.getMemberId() != member || !payment.getMemberId().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_PAYMENT_INFO);
    }

    String authorizationHeader = paymentComponent.createAuthorizationHeader();

    Money money = moneyRepository.findByMemberId(member.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MONEY));
    Long balance = money.getBalance();

    try {
      // 토스 결제 조회
      paymentClient.getPayment(authorizationHeader, paymentKey).getBody();

      // 취소금액이 현재 예치금보다 많을 경우
      if (balance < payment.getAmount()) {
        throw new CustomException(ErrorCode.NOT_ENOUGH_BALANCE);
      }

      // 멱등키 찾아오기
      String idempotencyKey = redisTemplate.opsForValue().get("orderId: " + payment.getOrderId());

      if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
        PaymentResponseDto.PaymentSuccess response = paymentClient.cancelPayment(authorizationHeader,
            idempotencyKey, paymentKey,
            cancelDto).getBody();
        return ResultDTO.of("중복 결제 취소 요청입니다.", response);
      }

      idempotencyKey = paymentComponent.createIdempotencyKey();
      // Toss에서 공시한 멱등키 유효기간은 15일
      redisTemplate.opsForValue()
          .set("orderId: " + payment.getOrderId(), idempotencyKey, IDEMPOTENCY_KEY_EXPIRES_IN, TimeUnit.MILLISECONDS);

      PaymentResponseDto.PaymentSuccess response = paymentClient.cancelPayment(authorizationHeader,
          idempotencyKey, paymentKey,
          cancelDto).getBody();

      MoneyAndBalanceHistoryAndPaymentUpdate(payment.getAmount(), balance, money, member, payment,
          cancelDto.getCancelReason());

      return ResultDTO.of("결제 취소를 성공했습니다.", response);
    } catch (FeignException fe) {
      return handleFeignException(fe);
    }
  }

  private void MoneyAndBalanceHistoryAndPaymentUpdate(Long cancelAmount, Long balance, Money money,
      Member member,
      Payment payment, String cancelReason) {

    balance -= cancelAmount;
    Money updateMoney = money.toBuilder()
        .balance(balance)
        .build();
    moneyRepository.save(updateMoney);

    BalanceHistory balanceHistory = BalanceHistory.builder()
        .memberId(member)
        .balance(cancelAmount)
        .type(BalanceType.PAYMENTS_CANCEL)
        .createDate(LocalDateTime.now())
        .build();
    balanceHistoryRepository.save(balanceHistory);

    Payment updatePayment = payment.toBuilder()
        .cancelYn(true)
        .cancelReason(cancelReason)
        .cancelDate(LocalDateTime.now())
        .build();
    paymentRepository.save(updatePayment);
  }

  private ResultDTO<PaymentResponseDto.PaymentSuccess> handleFeignException(FeignException fe) {
    // HTTP 상태 코드 가져오기
    HttpStatus httpStatus = HttpStatus.valueOf(fe.status());
    // 에러 응답 본문 가져오기
    String responseBody = fe.contentUTF8();
    // JSON 응답을 파싱하여 원하는 정보 추출
    try {
      JsonNode jsonNode = objectMapper.readTree(responseBody);
      String errorCode = jsonNode.path("code").asText();
      String errorMessage = jsonNode.path("message").asText();

      throw new TossAPIException(httpStatus, errorCode, errorMessage);
    } catch (IOException ioException) {
      // JSON 파싱 예외 처리
      throw new CustomException(ErrorCode.PARSING_ERROR);
    }
  }
}
