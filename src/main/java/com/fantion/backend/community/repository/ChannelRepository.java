package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.type.ChannelStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

  @Query(nativeQuery = true, value = "SELECT *  FROM channel ORDER BY RAND() LIMIT 9")
  List<Channel> findChannelRandom();

  Optional<Channel> findByChannelIdAndStatus(Long channelId, ChannelStatus channelStatus);
}