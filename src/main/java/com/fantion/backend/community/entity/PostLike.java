package com.fantion.backend.community.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_like")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike {
    @Id
    @Column(name = "like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;            // 게시글 추천 식별자

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post postId;            // 게시글 식별자

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberId;        // 회원 식별자
}
