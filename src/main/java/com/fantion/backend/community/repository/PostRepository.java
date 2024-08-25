package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.PostStatus;
import java.time.LocalDateTime;
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

  Page<Post> findByChannelAndStatus(Channel channel, PostStatus postStatus, Pageable pageable);

  Page<Post> findByChannelAndTitleContainingAndStatus(Channel channel, String keyword,
      PostStatus postStatus, Pageable pageable);

  Page<Post> findByChannelAndContentContainingAndStatus(Channel channel, String keyword,
      PostStatus postStatus, Pageable pageable);

  Page<Post> findByChannelAndTitleContainingOrContentContainingAndStatus(Channel channel,
      String keyword, String keyword1,
      PostStatus postStatus, Pageable pageable);

  Page<Post> findByChannelAndMemberAndStatus(Channel channel, Member member, PostStatus postStatus, Pageable pageable);

  List<Post> findTop10ByChannelOrderByCreateDateDesc(Channel channel);

  List<Post> findAllByDeleteDateBefore(LocalDateTime thirtyDaysAgo);
}
