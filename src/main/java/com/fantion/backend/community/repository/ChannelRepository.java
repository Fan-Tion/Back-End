package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel,Long> {
    @Query(nativeQuery=true, value="SELECT *  FROM channel ORDER BY RAND() LIMIT 2")
    List<Channel> findChannelRandom();
}