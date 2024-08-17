package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Post;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<Post> findByPostIdAndStatus(Long postId, PostStatus postStatus);

  List<Post> findAllByMemberId(Member member);
}
