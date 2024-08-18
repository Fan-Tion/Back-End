package com.fantion.backend.community.entity;

import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.ChannelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    public Channel editChannel(ChannelEditDto.Request request) {
        title = request.getTitle();
        description = request.getDescription();
        return this;
    }

}
