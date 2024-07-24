package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "favorite_auction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteAuction {
  @Id
  @Column(name = "favorite_auction_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long favoriteAuctionId;   // 찜 경매 식별자

  @ManyToOne
  @JoinColumn(name = "auction_id")
  private Auction auction;          // 경매 식별자

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;            // 회원 식별자
}
