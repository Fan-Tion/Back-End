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

@Entity
@Table(name = "balance_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class BalanceHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long balanceHistoryId;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member memberId;

  private Long balance;

  @Enumerated(EnumType.STRING)
  private BalanceType type;
}
