package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.entity.PostReport;
import com.fantion.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    Optional<PostReport> findByPostIdAndMemberId(Post post, Member member);
}
