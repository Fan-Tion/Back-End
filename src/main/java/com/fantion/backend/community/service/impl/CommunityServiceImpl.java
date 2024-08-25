package com.fantion.backend.community.service.impl;

import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.*;
import com.fantion.backend.community.dto.ChannelDto.Response;
import com.fantion.backend.community.dto.ChannelEditDto.Request;
import com.fantion.backend.community.dto.PostDto.PostCreateRequest;
import com.fantion.backend.community.dto.PostDto.PostResponse;
import com.fantion.backend.community.dto.PostDto.PostUpdateRequest;
import com.fantion.backend.community.entity.*;
import com.fantion.backend.community.repository.*;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.ChannelStatus;
import com.fantion.backend.common.dto.CheckDto;
import com.fantion.backend.auction.dto.AuctionFavoriteDto;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fantion.backend.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

  private final ChannelRepository channelRepository;
  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final MemberRepository memberRepository;
  private final CommentRepository commentRepository;
  private final PostReportRepository postReportRepository;
  private final S3Uploader s3Uploader;

    @Override
    public ResultDTO<List<ChannelDto.Response>> readChannelRandom() {
        // 채널 랜덤 조회
        List<Channel> channelRandomList = channelRepository.findChannelRandom();
        List<ChannelDto.Response> responseList = new ArrayList<>();

        for (int i = 0; i < channelRandomList.size(); i++) {
            Channel channel = channelRandomList.get(i);
            // 해당 채널의 최근 게시글 조회
            List<Post> latestPostList = postRepository.findTop10ByChannelOrderByCreateDateDesc(
                    channelRandomList.get(i));

            List<PostDto.PostResponse> latestPostDtoList = latestPostList.stream()
                    .map(PostDto::toResponse).toList();
            ChannelDto.Response response = ChannelDto.response(channel);
            response.setPostList(latestPostDtoList);
            responseList.add(response);
        }

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

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId,ChannelStatus.APPROVAL)
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
  public ResultDTO<PostCheckDto> createPost(Long channelId, PostCreateRequest request) {

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId,ChannelStatus.APPROVAL)
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
  public ResultDTO<PostResponse> getPost(Long channelId, Long postId) {

        channelRepository.findByChannelIdAndStatus(channelId,ChannelStatus.APPROVAL)
        .orElseThrow(() -> new CustomException(NOT_FOUND_CHANNEL));

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
    channelRepository.findByChannelIdAndStatus(channelId,ChannelStatus.APPROVAL)
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
  public ResultDTO<Page<PostDto.PostResponse>> getPostList(Long communityId, Integer page) {
    Channel channel = channelRepository.findByChannelIdAndStatus(communityId,ChannelStatus.APPROVAL)
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

    Channel channel = channelRepository.findByChannelIdAndStatus(channelId,ChannelStatus.APPROVAL)
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
    public ResultDTO<CommentDto.CommentResponse> createComment(Long channelId, Long postId,
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

        CommentDto.CommentResponse response = CommentDto.toResponse(comment);

        return ResultDTO.of("댓글을 성공적으로 작성했습니다.", response);
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

        Page<Comment> comments = commentRepository.findByPostAndStatus(post, CommentStatus.ACTIVE,
                pageable);

        Page<CommentDto.CommentResponse> response = comments.map(
                comment -> CommentDto.toResponse(comment));

        return ResultDTO.of("댓글 목록을 불어오는데 성공했습니다.", response);
    }
    @Override
    public ResultDTO<PostLikeDto.Response> postLikeChk(Long channelId, Long postId) {
        // 추천 여부 확인할 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(NOT_FOUND_POST));

        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

        // 추천 조회
        Optional<PostLike> postLike = postLikeRepository.findByPostIdAndMemberId(post, member);

        PostLikeDto.Response response = PostLikeDto.Response.builder()
                .postLikeChk(true)
                .title(post.getTitle())
                .build();

        // 추천이 되어있지 않는 경우
        if (postLike.isEmpty()) {
            response.setPostLikeChk(false);
        }

        return ResultDTO.of("성공적으로 추천 여부가 조회되었습니다.", response);
    }

    @Override
    public ResultDTO<PostLikeDto.Response> postLike(Long channelId, Long postId) {
        // 추천하거나 취소할 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new CustomException(NOT_FOUND_POST));

        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

        // 추천 조회
        Optional<PostLike> postLike = postLikeRepository.findByPostIdAndMemberId(post, member);

        PostLikeDto.Response response = PostLikeDto.Response.builder()
                .postLikeChk(true)
                .title(post.getTitle())
                .build();

        // 추천이 되어있지 않는 경우
        if (postLike.isEmpty()) {
            PostLike like = PostLike.builder()
                    .postId(post)
                    .memberId(member)
                    .build();
            postLikeRepository.save(like);
            post.like();
        } else {
            // 추천이 되어있는 경우 취소
            postLikeRepository.delete(postLike.get());
            response.setPostLikeChk(false);
            post.unLike();
        }

        return ResultDTO.of("성공적으로 추천 또는 추천 취소가 되었습니다.", response);
    }

    @Override
    public ResultDTO<ChannelDto.Response> readChannel(Long channelId) {
        // 채널 조회
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(()-> new CustomException(NOT_FOUND_CHANNEL));

        ChannelDto.Response response = ChannelDto.response(channel);
        return ResultDTO.of("성공적으로 채널 정보 조회되었습니다.", response);
    }

    @Override
    public ResultDTO<PostReportDto.Response> postReport(PostReportDto.Request request) {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member member = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

        // 신고할 게시글 조회
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_POST));

        // 이미 신고한 게시글인 경우
        postReportRepository.findByPostIdAndMemberId(post,member).ifPresent(report -> {
            throw new CustomException(ALREADY_REPORT_POST);
        });

        PostReport postReport = PostReport.builder()
                .postId(post)
                .memberId(member)
                .description(request.getDescription())
                .build();
        postReportRepository.save(postReport);

        PostReportDto.Response response = PostReportDto.Response.builder()
                .title(post.getTitle())
                .build();

        return ResultDTO.of("성공적으로 게시글 신고되었습니다.", response);
    }

    @Override
  public ResultDTO<List<ChannelAllDto.Response>> readChannelAll() {
    Map<Character, List<Channel>> groupedData = getGroupedData();
    List<ChannelAllDto.Response> response = groupedData.entrySet().stream()
            .map(entry -> new ChannelAllDto.Response(entry.getKey(),
                    entry.getValue().stream().sorted(Comparator.comparing(Channel::getTitle))
                            .map(ChannelDto::response)
                            .collect(Collectors.toList())))
            .collect(Collectors.toList());

    return ResultDTO.of("성공적으로 전체 채널 조회되었습니다.", response);
  }

  private Map<Character, List<Channel>> getGroupedData() {
    List<Channel> channelList = channelRepository.findAll();
    return channelList.stream().collect(Collectors.groupingBy(this::getFirstConsonant));
  }

  // 초성을 가져오는 메소드
  private Character getFirstConsonant(Channel channel) {
    char firstChar = channel.getTitle().charAt(0); // 예: 엔티티에서 이름 필드의 첫 글자 사용
    return getKoreanInitialSound(firstChar);
  }

  // 한글 초성 추출 로직
  private char getKoreanInitialSound(char c) {
    if (c >= '가' && c <= '힣') {
      int unicodeValue = c - 0xAC00;
      int initialSoundIndex = unicodeValue / (21 * 28);
      return (char) (initialSoundIndex + 0x1100); // 초성 유니코드
    }
    return c;
  }

}
