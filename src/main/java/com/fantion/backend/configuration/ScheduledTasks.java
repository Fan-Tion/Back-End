package com.fantion.backend.configuration;

import com.fantion.backend.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
  private final AuctionService auctionService;

  @Scheduled(cron = "0 0 0 * * ?")
//  @Scheduled(cron = "*/20 * * * * ?")
  public void executeScheduledTask() {
    auctionService.endAuctionSaveOrUpdate();
  }
}
