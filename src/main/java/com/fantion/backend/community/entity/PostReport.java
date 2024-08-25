package com.fantion.backend.community.entity;

import com.fantion.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReport {

    @Id
    @Column(name = "post_report_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postReportId;      // 게시글 신고 식별자

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post postId;            // 신고 받은 게시글

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberId;        // 신고한 회원

    private String description;     // 신고 내용
}
