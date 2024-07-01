package com.fantion.backend.auction.entity;

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
  private Long bidId;

  @ManyToOne
  private Auction auction;

  @Column(name = "bid_price")
  private Long bidPrice;

  @Column(name = "bidder")
  private String bidder;

  @Column(name = "create_date")
  private LocalDateTime createDate;
}
