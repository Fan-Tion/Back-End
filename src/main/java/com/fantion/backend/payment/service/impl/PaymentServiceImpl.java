package com.fantion.backend.payment.service.impl;

import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.exception.impl.TossApiException;
import com.fantion.backend.member.entity.BalanceHistory;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.entity.Money;
import com.fantion.backend.member.repository.BalanceHistoryRepository;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.repository.MoneyRepository;
import com.fantion.backend.payment.component.PaymentClient;
import com.fantion.backend.payment.component.PaymentComponent;
import com.fantion.backend.payment.dto.ConfirmDto;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentDto.Request;
import com.fantion.backend.payment.dto.ResponseDto;
import com.fantion.backend.payment.dto.ResponseDto.fail;
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
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberRepository memberRepository;
  private final MoneyRepository moneyRepository;
  private final BalanceHistoryRepository balanceHistoryRepository;
  private final PaymentComponent paymentComponent;
  private final PaymentClient paymentClient;
  private final ObjectMapper objectMapper;

  @Value("${payment.success-url}")
  private String successUrl;

  @Value("${payment.fail-url}")
  private String failUrl;

  @Override
  @Transactional
  public PaymentDto.Response requestPayment(Request request) {

    // MemberService가 추가되면 accessToken을 이용한 유저정보 저장 및 반환 수정
    // 지금은 프론트 코드에서 그냥 던져주는 중
    String orderId = UUID.randomUUID().toString();

    Member member = memberRepository.findByEmail(request.getCustomerEmail())
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
        .paymentDate(LocalDateTime.now())
        .build();
    paymentRepository.save(payment);

    // 결제 정보 Response를 반환
    return PaymentDto.of(payment, successUrl, failUrl);
  }

  @Override
  @Transactional
  public ResponseDto.Success successPayment(String orderId, String paymentKey, Long amount) {

    // DB에 저장되어있는 값과 Param으로 들어온 값이 같은지 검증
    Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT_INFO));
    if (!payment.getAmount().equals(amount)) {
      throw new CustomException(ErrorCode.VALID_PAYMENT_INFO);
    }

    // paymentKey와 성공여부를 true로 저장
    Payment updatePayment = payment.toBuilder()
        .paymentKey(paymentKey)
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

      Optional<Money> byMemberId = moneyRepository.findByMemberId(member.getMemberId());
      if (byMemberId.isEmpty()) { // 첫 예치금 충전 회원
        Money money = Money.builder()
            .member(member)
            .balance(amount)
            .build();
        moneyRepository.save(money);
      } else {
        Money money = byMemberId.get();
        Money updateMoney = money.toBuilder()
            .balance(money.getBalance() + amount)
            .build();
        moneyRepository.save(updateMoney);
      }

      // 예치금 내역에도 저장
      BalanceHistory balanceHistory = BalanceHistory.builder()
          .memberId(member)
          .balance(amount)
          .type(BalanceType.CHARGING)
          .build();
      balanceHistoryRepository.save(balanceHistory);

      return paymentClient.confirmPayment(header, confirmDto).getBody();
    } catch (FeignException fe) {
      // HTTP 상태 코드 가져오기
      HttpStatus httpStatus = HttpStatus.valueOf(fe.status());
      // 에러 응답 본문 가져오기
      String responseBody = fe.contentUTF8();
      // JSON 응답을 파싱하여 원하는 정보 추출
      try {
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String errorCode = jsonNode.path("code").asText();
        String errorMessage = jsonNode.path("message").asText();

        throw new TossApiException(httpStatus, errorCode, errorMessage);
      } catch (IOException ioException) {
        // JSON 파싱 예외 처리
        throw new CustomException(ErrorCode.PARSING_ERROR);
      }
    }
  }

  @Override
  public ResponseDto.fail failPayment(String code, String message, String orderId) {

    return fail.builder()
        .errorCode(code)
        .message(message)
        .orderId(orderId)
        .build();
  }
}
