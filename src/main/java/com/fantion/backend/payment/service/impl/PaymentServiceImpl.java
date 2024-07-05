package com.fantion.backend.payment.service.impl;

import com.fantion.backend.exception.impl.NotFoundMemberException;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.payment.dto.PaymentDto;
import com.fantion.backend.payment.dto.PaymentDto.Response;
import com.fantion.backend.payment.entity.Payment;
import com.fantion.backend.payment.repository.PaymentRepository;
import com.fantion.backend.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberRepository memberRepository;

  @Value("${payment.success-url}")
  private String successUrl;

  @Value("${payment.fail-url}")
  private String failUrl;

  @Override
  @Transactional
  public PaymentDto.Response requestPayment(PaymentDto.Request request) {

    // MemberService가 추가되면 accessToken을 이용한 유저정보 저장 및 반환 수정
    // 지금은 프론트 코드에서 그냥 던져주는 중
    String orderId = UUID.randomUUID().toString();

    Member member = memberRepository.findByEmail(request.getCustomerEmail())
        .orElseThrow(() -> new NotFoundMemberException());

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
    Response response = PaymentDto.of(payment, successUrl, failUrl);

    return response;
  }
}
