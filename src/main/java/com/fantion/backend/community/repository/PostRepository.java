package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Community;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  Optional<Post> findByPostIdAndStatus(Long postId, PostStatus postStatus);

  List<Post> findAllByMember(Member member);

  Page<Post> findByCommunityAndStatus(Community community, PostStatus status, Pageable pageable);
}
