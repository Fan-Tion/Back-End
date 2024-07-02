package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
  private Long favoriteAuctionId;

  @ManyToOne
  Auction auction;

  @ManyToOne
  Member member;
}
