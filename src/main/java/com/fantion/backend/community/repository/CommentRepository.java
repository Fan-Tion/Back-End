package com.fantion.backend.community.repository;

import com.fantion.backend.community.entity.Comment;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.type.CommentStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  Optional<Comment> findByCommentIdAndStatus(Long commentId, CommentStatus commentStatus);

  Page<Comment> findByPostAndStatus(Post post, CommentStatus commentStatus, Pageable pageable);
}
