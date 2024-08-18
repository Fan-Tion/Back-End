package com.fantion.backend.community.entity;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.CommunityStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Community {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long communityId;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  private String title;
  private String description;
  private String image;

  @Enumerated(EnumType.STRING)
  private CommunityStatus status;

  private LocalDateTime createDate;
}
