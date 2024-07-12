package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  private Long auctionId;

  @ManyToOne
  Member member;

  @Column(name = "title")
  private String title;

  @Column(name = "category")
  private CategoryType category;

  @Column(name = "auction_type")
  private boolean auctionType;

  @Column(name = "auction_image")
  private String auctionImage;

  @Column(name = "description")
  private String description;

  @Column(name = "current_bid_price")
  private Long currentBidPrice;

  @Column(name = "current_bidder")
  private String currentBidder;

  @Column(name = "buy_now_price")
  private Long buyNowPrice;

  @Column(name = "favorite_cnt")
  private Long favoriteCnt;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "status")
  private boolean status;
}
