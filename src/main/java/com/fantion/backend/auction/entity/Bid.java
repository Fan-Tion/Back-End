package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.*;

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
  @JoinColumn(name = "member_id")
  private Member bidder;              // 입찰자

  @Column(name = "create_date")
  private LocalDateTime createDate;   // 입찰한 시간

}
