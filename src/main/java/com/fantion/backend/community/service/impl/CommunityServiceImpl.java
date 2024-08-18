package com.fantion.backend.community.service.impl;

import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.CheckDto;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.community.dto.PostDto.PostCreateRequest;
import com.fantion.backend.community.dto.PostDto.PostResponse;
import com.fantion.backend.community.dto.PostDto.PostUpdateRequest;
import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.repository.ChannelRepository;
import com.fantion.backend.community.repository.PostRepository;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.ChannelStatus;
import com.fantion.backend.type.PostSearchOption;
import com.fantion.backend.type.PostStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

  private final ChannelRepository channelRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final S3Uploader s3Uploader;

  @Override
  public ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long channelId, Long postId) {

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (postId == null) {
      Post post = Post.builder()
          .channel(channel)
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
        .channelId(channelId)
        .build();

    return ResultDTO.of("게시글 이미지 저장에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<CheckDto> createPost(Long channelId, PostCreateRequest request) {

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    Post post = new Post();
    if (request.getPostId() != null) {
      post = postRepository.findByPostIdAndStatus(request.getPostId(), PostStatus.DRAFTS)
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    } else {
      post = postRepository.save(post.toBuilder()
          .status(PostStatus.DRAFTS)
          .build());
    }

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post createdPost = post.toBuilder()
        .channel(channel)
        .title(request.getTitle())
        .content(request.getContent())
        .likeCnt(0)
        .viewCnt(0)
        .createDate(LocalDateTime.now())
        .deleteDate(null)
        .status(PostStatus.ACTIVE)
        .build();
    postRepository.save(createdPost);

    CheckDto response = CheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(createdPost.getPostId())
        .build();

    return ResultDTO.of("게시글 작성에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<PostResponse> getPost(Long channelId, Long postId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }
    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Post updateViewCnt = post.toBuilder()
        .viewCnt(post.getViewCnt() + 1)
        .build();
    postRepository.save(updateViewCnt);

    PostResponse response = PostDto.toResponse(updateViewCnt);

    return ResultDTO.of("게시글 조회에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<CheckDto> updatePost(Long channelId, Long postId,
      PostUpdateRequest request) {

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post updatePost = post.toBuilder()
        .title(request.getTitle())
        .content(request.getContent())
        .build();
    postRepository.save(updatePost);

    CheckDto response = CheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(postId)
        .build();

    return ResultDTO.of("게시글 수정에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<CheckDto> deletePost(Long channelId, Long postId) {
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post deletePost = post.toBuilder()
        .deleteDate(LocalDateTime.now())
        .status(PostStatus.DELETE)
        .build();
    postRepository.save(deletePost);

    CheckDto response = CheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(postId)
        .build();

    return ResultDTO.of("게시글 삭제에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<Page<PostResponse>> getPostList(Long communityId, Integer page) {
    Channel channel = channelRepository.findById(communityId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }

    // 페이지 요청 설정 (한 페이지에 20개, 최신순 정렬)
    Pageable pageable = PageRequest.of(page, 20, Sort.by(Direction.DESC, "createDate"));

    // 해당 커뮤니티의 활성 상태인 게시글만 조회
    Page<Post> posts = postRepository.findByChannelAndStatus(channel, PostStatus.ACTIVE,
        pageable);

    // 조회된 게시글을 PostResponse DTO로 변환
    Page<PostResponse> response = posts.map(post -> PostDto.toResponse(post));

    return ResultDTO.of("게시글 목록을 불러오는데 성공했습니다.", response);
  }

  @Override
  public ResultDTO<Page<PostResponse>> searchPost(Long channelId, PostSearchOption searchOption,
      String keyword, Integer page) {

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(ErrorCode.NOT_FOUND_CHANNEL);
    }
    Pageable pageable = PageRequest.of(page, 20, Sort.by(Direction.DESC, "createDate"));

    Page<Post> posts = Page.empty(pageable);

    if (searchOption.equals(PostSearchOption.TITLE)) {
      posts = postRepository.findByChannelAndTitleContainingAndStatus(channel, keyword,
          PostStatus.ACTIVE, pageable);
    } else if (searchOption.equals(PostSearchOption.CONTENT)) {
      // 내용에서 키워드를 포함하는 게시물 검색
      posts = postRepository.findByChannelAndContentContainingAndStatus(channel, keyword,
          PostStatus.ACTIVE, pageable);
    } else if (searchOption.equals(PostSearchOption.TITLE_AND_CONTENT)) {
      // 제목이나 내용에서 키워드를 포함하는 게시물 검색
      posts = postRepository.findByChannelAndTitleContainingOrContentContainingAndStatus(
          channel, keyword, keyword, PostStatus.ACTIVE, pageable);
    } else if (searchOption.equals(PostSearchOption.NICKNAME)) {
      // 닉네임으로 게시물 검색, 닉네임 검색은 닉네임이 정확해야 검색 가능.
      Optional<Member> byNickname = memberRepository.findByNickname(keyword);
      if (byNickname.isPresent()) {
        Member member = byNickname.get();
        posts = postRepository.findByChannelAndMemberAndStatus(channel, member,
            PostStatus.ACTIVE, pageable);
      }
    }

    Page<PostResponse> response = posts.map(post -> PostDto.toResponse(post));

    return ResultDTO.of("게시글 검색을 성공했습니다.", response);
  }
}
