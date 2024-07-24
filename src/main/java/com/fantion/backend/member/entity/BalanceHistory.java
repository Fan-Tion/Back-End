package com.fantion.backend.member.entity;

import com.fantion.backend.type.BalanceType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "balance_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BalanceHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long balanceHistoryId;      // 예치금 내역 식별자

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member memberId;            // 회원 식별자

  private Long balance;               // 예치금

  @Enumerated(EnumType.STRING)
  private BalanceType type;           // 예치금 타입 (충전,사용,출금,취소)

  private LocalDateTime createDate;   // 생성일
}
