package com.fantion.backend.auction.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.Entity;
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
import lombok.Setter;

@Entity
@Table(name = "auction_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long auctionReportId;

  @ManyToOne
  @JoinColumn(name = "auction_id")
  private Auction auctionId;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member memberId;

  private String description;
}
