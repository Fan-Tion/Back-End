package com.fantion.backend.community.service.impl;

import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.community.dto.PostDto.PostRequest;
import com.fantion.backend.community.entity.Community;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.repository.CommunityRepository;
import com.fantion.backend.community.repository.PostRepository;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.CommunityStatus;
import com.fantion.backend.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

  private final CommunityRepository communityRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final S3Uploader s3Uploader;

  @Override
  public ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long communityId, Long postId) {

    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (community.getStatus().equals(CommunityStatus.CLOSE)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (postId == null) {
      Post post = Post.builder()
          .community(community)
          .member(member)
          .status(PostStatus.DRAFTS)
          .build();
      postId = postRepository.save(post).getPostId();
    } else {
      Post post = postRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
      if (post.getStatus().equals(PostStatus.DELETE)) {
        throw new CustomException(ErrorCode.NOT_FOUND_POST);
      }
    }

    List<String> imageUrl = new ArrayList<>();
    for (MultipartFile file : files) {
      String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
      List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
      // 확장자가 이미지 파일인지 확인
      if (fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase())) {
        try {
          imageUrl.add(s3Uploader.upload(file, "post-images/" + postId));
        } catch (Exception e) {
          throw new CustomException(ErrorCode.FAILED_IMAGE_SAVE);
        }
      } else {
        // 이미지 파일이 아닌 경우에 대한 처리
        throw new CustomException(ErrorCode.UN_SUPPORTED_IMAGE_TYPE);
      }
    }

    ImageDto response = ImageDto.builder()
        .imageUrl(imageUrl)
        .postId(postId)
        .communityId(communityId)
        .build();

    return ResultDTO.of("게시글 이미지 저장에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<PostDto.PostResponse> createPost(PostRequest request, Long communityId) {

    Community community = communityRepository.findById(communityId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (community.getStatus().equals(CommunityStatus.CLOSE)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    Post post = postRepository.findById(request.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post createdPost = post.toBuilder()
        .community(community)
        .title(request.getTitle())
        .content(request.getContent())
        .likeCnt(0)
        .viewCnt(0)
        .createDate(LocalDateTime.now())
        .deleteDate(null)
        .status(PostStatus.ACTIVE)
        .build();
    postRepository.save(createdPost);

    PostDto.PostResponse response = PostDto.PostResponse.builder()
        .postId(createdPost.getPostId())
        .channelName(createdPost.getCommunity().getTitle())
        .nickname(createdPost.getMember().getNickname())
        .title(createdPost.getTitle())
        .content(createdPost.getContent())
        .likeCnt(createdPost.getLikeCnt())
        .viewCnt(createdPost.getViewCnt())
        .createDate(createdPost.getCreateDate())
        .deleteDate(createdPost.getDeleteDate())
        .status(createdPost.getStatus())
        .build();

    return ResultDTO.of("게시글 작성에 성공했습니다.", response);
  }
}
