package com.fantion.backend.community.service.impl;

import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.CheckDto;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.*;
import com.fantion.backend.community.dto.ChannelDto.Response;
import com.fantion.backend.community.dto.ChannelEditDto.Request;
import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.community.entity.Comment;
import com.fantion.backend.community.entity.Post;
import com.fantion.backend.community.repository.ChannelRepository;
import com.fantion.backend.community.repository.CommentRepository;
import com.fantion.backend.community.repository.PostRepository;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.ChannelStatus;
import com.fantion.backend.type.CommentStatus;
import com.fantion.backend.type.PostSearchOption;
import com.fantion.backend.type.PostStatus;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import static com.fantion.backend.exception.ErrorCode.IMAGE_IO_ERROR;
import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_CHANNEL;
import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_MEMBER;


@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

  private final ChannelRepository channelRepository;
  private final PostRepository postRepository;
  private final MemberRepository memberRepository;
  private final CommentRepository commentRepository;
  private final S3Uploader s3Uploader;

  @Override
  public ResultDTO<List<Response>> readChannelRandom() {
    List<Channel> channelRandomList = channelRepository.findChannelRandom();
    List<Response> responseList = channelRandomList.stream().map(ChannelDto::response)
        .toList();
    return ResultDTO.of("성공적으로 채널이 랜덤으로 조회되었습니다.", responseList);
  }

  @Transactional
  @Override
  public ResultDTO<Response> createChannel(ChannelDto.Request request,
      MultipartFile file) {
    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member organizer = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    // 채널 생성
    Channel newChannel = Channel.builder()
        .organizer(organizer)
        .title(request.getTitle())
        .description(request.getDescription())
        .status(ChannelStatus.APPROVAL)
        .createDate(LocalDateTime.now())
        .build();

    if (!(file == null || file.isEmpty())) { // 이미지 파일이 있으면
      // 파일 이름에서 확장자 추출
      String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

      // 지원하는 이미지 파일 확장자 목록
      List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");

      String imageUrl;
      // 확장자가 이미지 파일인지 확인
      if (fileExtension != null && allowedExtensions.contains(fileExtension.toLowerCase())) {
        try {
          imageUrl = s3Uploader.upload(file, "channel-images");
        } catch (Exception e) {
          throw new CustomException(ErrorCode.FAILED_IMAGE_SAVE);
        }
      } else {
        // 이미지 파일이 아닌 경우에 대한 처리
        throw new CustomException(ErrorCode.UN_SUPPORTED_IMAGE_TYPE);
      }

      // 이미지 url 세팅
      newChannel.setImage(imageUrl);

    }

    return ResultDTO.of("성공적으로 채널이 생성되었습니다.",
        ChannelDto.response(channelRepository.save(newChannel)));
  }

  @Transactional
  @Override
  public ResultDTO<Response> editChannel(Request request,
      MultipartHttpServletRequest file) {

    // 수정하려는 채널 조회
    Channel channel = channelRepository.findByChannelIdAndStatus(request.getChannelId(),
        ChannelStatus.APPROVAL).orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    String currentImage = channel.getImage();

    Map<String, String[]> parameterMap = file.getParameterMap();
    Map<String, MultipartFile> fileMap = file.getFileMap();

    String imageUrl = "";
    try {
      String type = parameterMap.get("channelImageType")[0].toString();
      if (type.equals("url")) {
        String url = parameterMap.get("channelImageValue")[0].toString();
        imageUrl = url;
      } else if (type.equals("file")) {
        MultipartFile imageFile = fileMap.get("channelImageValue");
        String url = s3Uploader.upload(imageFile, "channel-images");
        imageUrl = url;
      }

      // currentImages 중 auctionImageUrl에 없는 이미지를 삭제
      if (!imageUrl.contains(currentImage)) {
        URL exProfileImageUrl = new URL(currentImage);
        String exProfileImage = exProfileImageUrl.getPath().substring(1);
        s3Uploader.deleteFile(exProfileImage);
      }

    } catch (IOException e) {
      throw new CustomException(IMAGE_IO_ERROR);
    }

    return ResultDTO.of("성공적으로 채널이 수정되었습니다.",
        ChannelDto.response(channel.editChannel(request, imageUrl)));
  }

  @Transactional
  @Override
  public ResultDTO<Response> removeChannel(ChannelRemoveDto.Request request) {
    // 삭제하려는 채널 조회
    Channel channel = channelRepository.findById(request.getChannelId())
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    // 채널 이미지가 있는 경우
    if (channel.getImage() != null) {
      // 채널 이미지 삭제
      try {
        URL exProfileImageUrl = new URL(channel.getImage());
        String exProfileImage = exProfileImageUrl.getPath().substring(1);
        s3Uploader.deleteFile(exProfileImage);
      } catch (MalformedURLException e) {
        throw new CustomException(ErrorCode.IMAGE_NOT_HAVE_PATH);
      }
    }

    // 채널 삭제
    channelRepository.delete(channel);

    Response response = ChannelDto.response(channel);
    return ResultDTO.of("성공적으로 채널이 삭제되었습니다.", response);
  }

  @Override
  public ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long channelId, Long postId) {

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

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
  public ResultDTO<PostCheckDto> createPost(Long channelId, PostDto.PostCreateRequest request) {

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    Post post = new Post();
    if (request.getPostId() != null) {
      post = postRepository.findByPostIdAndStatus(request.getPostId(), PostStatus.DRAFTS)
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));
    } else {
      post = postRepository.save(post.toBuilder()
          .member(member)
          .status(PostStatus.DRAFTS)
          .build());
    }

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

    PostCheckDto response = PostCheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(createdPost.getPostId())
        .build();

    return ResultDTO.of("게시글 작성에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<PostDto.PostResponse> getPost(Long channelId, Long postId) {

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Post updateViewCnt = post.toBuilder()
        .viewCnt(post.getViewCnt() + 1)
        .build();
    postRepository.save(updateViewCnt);

    PostDto.PostResponse response = PostDto.toResponse(updateViewCnt);

    return ResultDTO.of("게시글 조회에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<PostCheckDto> updatePost(Long channelId, Long postId,
      PostDto.PostUpdateRequest request) {

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    if (channel.getStatus().equals(ChannelStatus.CLOSING)) {
      throw new CustomException(NOT_FOUND_CHANNEL);
    }

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post updatePost = post.toBuilder()
        .title(request.getTitle())
        .content(request.getContent())
        .build();
    postRepository.save(updatePost);

    PostCheckDto response = PostCheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(postId)
        .build();

    return ResultDTO.of("게시글 수정에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<PostCheckDto> deletePost(Long channelId, Long postId) {

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    if (!post.getMember().equals(member)) {
      throw new CustomException(ErrorCode.INVALID_POST_MEMBER);
    }

    Post deletePost = post.toBuilder()
        .deleteDate(LocalDateTime.now())
        .status(PostStatus.DELETE)
        .build();
    postRepository.save(deletePost);

    PostCheckDto response = PostCheckDto.builder()
        .success(true)
        .channelId(channelId)
        .postId(postId)
        .build();

    return ResultDTO.of("게시글 삭제에 성공했습니다.", response);
  }

  @Override
  public ResultDTO<Page<PostDto.PostResponse>> getPostList(Long channelId, Integer page) {

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    // 페이지 요청 설정 (한 페이지에 20개, 최신순 정렬)
    Pageable pageable = PageRequest.of(page, 20, Sort.by(Direction.DESC, "createDate"));

    // 해당 커뮤니티의 활성 상태인 게시글만 조회
    Page<Post> posts = postRepository.findByChannelAndStatus(channel, PostStatus.ACTIVE,
        pageable);

    // 조회된 게시글을 PostResponse DTO로 변환
    Page<PostDto.PostResponse> response = posts.map(post -> PostDto.toResponse(post));

    return ResultDTO.of("게시글 목록을 불러오는데 성공했습니다.", response);
  }

  @Override
  public ResultDTO<Page<PostDto.PostResponse>> searchPost(Long channelId, PostSearchOption searchOption,
      String keyword, Integer page) {

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

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

    Page<PostDto.PostResponse> response = posts.map(post -> PostDto.toResponse(post));

    return ResultDTO.of("게시글 검색을 성공했습니다.", response);
  }

  @Override
  public ResultDTO<CheckDto> createComment(Long channelId, Long postId,
      CommentDto.CommentRequest request) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Comment comment = Comment.builder()
        .post(post)
        .member(member)
        .content(request.getContent())
        .status(CommentStatus.ACTIVE)
        .createDate(LocalDateTime.now())
        .build();
    commentRepository.save(comment);

    return ResultDTO.of("댓글을 성공적으로 작성했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<CheckDto> updateComment(Long channelId, Long postId,
      Long commentId, CommentDto.CommentRequest request) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Comment comment = commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

    if (!member.equals(comment.getMember())) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_MEMBER);
    }

    Comment updateComment = comment.toBuilder()
        .content(request.getContent())
        .build();
    commentRepository.save(updateComment);

    return ResultDTO.of("댓글을 성공적으로 수정했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<CheckDto> deleteComment(Long channelId, Long postId, Long commentId) {

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Comment comment = commentRepository.findByCommentIdAndStatus(commentId, CommentStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_COMMENT));

    if (!member.equals(comment.getMember())) {
      throw new CustomException(ErrorCode.INVALID_COMMENT_MEMBER);
    }

    Comment updateComment = comment.toBuilder()
        .status(CommentStatus.DELETE)
        .deleteDate(LocalDateTime.now())
        .build();
    commentRepository.save(updateComment);

    return ResultDTO.of("댓글을 성공적으로 삭제했습니다.", CheckDto.builder().success(true).build());
  }

  @Override
  public ResultDTO<Page<CommentDto.CommentResponse>> getComment(Long channelId, Long postId,
      Integer page) {

    channelRepository.findByChannelIdAndStatus(channelId, ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

    Post post = postRepository.findByPostIdAndStatus(postId, PostStatus.ACTIVE)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

    Pageable pageable = PageRequest.of(page, 15, Sort.by(Direction.DESC, "createDate"));

    Page<Comment> comments = commentRepository.findByPostAndStatus(post, CommentStatus.ACTIVE, pageable);

    Page<CommentDto.CommentResponse> response = comments.map(comment -> CommentDto.toResponse(comment));

    return ResultDTO.of("댓글 목록을 불어오는데 성공했습니다.", response);
  }
}
