package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.entity.PostLike;
import com.fantion.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike >findByPostIdAndMemberId(Post post, Member member);

}
