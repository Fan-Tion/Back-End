package com.fantion.backend.member.entity;

import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "money")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Money {

  @Id
  private Long memberId;

  @MapsId()
  @OneToOne
  @JoinColumn(name = "member_id")
  private Member member;
  private Long balance;

  // 예치금 사용
  public void useBalance(Long bidPrice){
    if (bidPrice > balance) {
      throw new CustomException(ErrorCode.NOT_ENOUGH_BALANCE);
    }
    balance -= bidPrice;
  }

  // 예치금 충전
  public void chargingBalance(Long bidPrice){
    balance += bidPrice;
  }
}
