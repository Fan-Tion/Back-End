package com.fantion.backend.payment.entity;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long paymentId;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member memberId;

  @Enumerated(EnumType.STRING)
  private PaymentType paymentType;
  private Long amount;
  private String orderName;
  private String orderId;
  private LocalDateTime paymentDate;
  private Boolean successYn;
  private String paymentKey;
  private Boolean cancelYn;
}
