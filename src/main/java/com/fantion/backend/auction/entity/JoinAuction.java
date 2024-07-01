package com.fantion.backend.auction.entity;

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
@Table(name = "join_auction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinAuction {
  @Id
  @Column(name = "join_auction_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long joinAuctionId;

  @ManyToOne
  Auction auction;

  @ManyToOne
  Member member;
}
