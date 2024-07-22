package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.CategoryType;
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
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "auction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {
  @Id
  @Column(name = "auction_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long auctionId;               // 경매 식별자

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;                // 경매 주최자

  @Column(name = "title")
  private String title;                 // 경매 제목

  @Column(name = "category")
  private CategoryType category;        // 경매 물품 카테고리

  @Column(name = "auction_type")
  private boolean auctionType;          // 경매 타입 (true - 공개, false - 비공개)

  @Column(name = "auction_image")
  private String auctionImage;          // 경매 물품 이미지

  @Column(name = "description")
  private String description;           // 경매 물품 설명

  @Column(name = "current_bid_price")
  private Long currentBidPrice;         // 현재 입찰가

  @Column(name = "current_bidder")
  private String currentBidder;         // 현재 입찰자

  @Column(name = "buy_now_price")
  private Long buyNowPrice;             // 즉시 구매가

  @Column(name = "favorite_cnt")
  private Long favoriteCnt;             // 찜한 회원의 수

  @Column(name = "create_date")
  private LocalDateTime createDate;     // 경매 생성일

  @Column(name = "end_date")
  private LocalDateTime endDate;        // 경매 마감일

  @Column(name = "status")
  private boolean status;               // 경매 상태 (true - 경매중, false - 경매마감)

  @Column(name = "send_chk")
  @ColumnDefault("false")
  private boolean sendChk;              // 인계 확인

  @Column(name = "receive_chk")
  @ColumnDefault("false")
  private boolean receiveChk;           // 인수 확인


  // 상위 입찰 설정
  public void topBid(Long bidPrice,String bidder) {
    this.currentBidPrice = bidPrice;
    this.currentBidder = bidder;
  }

  // 인계 확인
  public void sendChking(boolean sendChk) {
    this.sendChk = sendChk;
  }

  // 인수 확인
  public void receiveChking(boolean receiveChk) {
    this.receiveChk = receiveChk;
  }
}
