package com.fantion.backend.community.entity;

import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.ChannelStatus;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "channel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long channelId;                 // 채널 식별자

  @ManyToOne
  @JoinColumn(name = "organizer")
  private Member organizer;               // 주최자

  private String title;                   // 채널 제목
  private String description;             // 채널 설명
  private String image;                   // 채널 이미지

  @Enumerated(EnumType.STRING)
  private ChannelStatus status;           // 채널 상태

  private LocalDateTime createDate;       // 채널 생성일

  // 채널 수정
  public Channel editChannel(ChannelEditDto.Request request, String imageUrl) {
    title = request.getTitle();
    description = request.getDescription();
    image = imageUrl;
    return this;
  }
}
