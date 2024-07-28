package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Setter;

@Entity
@Table(name = "bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {
  @Id
  @Column(name = "bid_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long bidId;                 // 입찰 식별자

  @ManyToOne
  @JoinColumn(name = "auction_id")
  private Auction auctionId;          // 경매 식별자

  @Column(name = "bid_price")
  private Long bidPrice;              // 입찰가

  @ManyToOne
  @JoinColumn(name = "bidder")
  private Member bidder;              // 입찰자

  @Column(name = "create_date")
  private LocalDateTime createDate;   // 입찰한 시간


  // 기존 입찰의 입찰가와 입찰시간 갱신
  public void updateBid(Long bidPrice,LocalDateTime createDate) {
    this.bidPrice = bidPrice;
    this.createDate = createDate;

  }


}
