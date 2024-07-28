package com.fantion.backend.member.entity;

import com.fantion.backend.auction.entity.Auction;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rating_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RatingHistory {

  @Id
  private Long auctionId;

  @MapsId()
  @OneToOne
  @JoinColumn(name = "auction_id")
  private Auction auction;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member memberId;
}
