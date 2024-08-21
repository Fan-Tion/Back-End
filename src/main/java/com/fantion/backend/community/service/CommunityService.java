package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.CheckDto;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ChannelAllDto;
import com.fantion.backend.community.dto.ChannelDto;
import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.community.dto.ChannelRemoveDto;
import com.fantion.backend.community.dto.CommentDto;
import com.fantion.backend.community.dto.PostCheckDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.type.PostSearchOption;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Service
public interface CommunityService {

    ResultDTO<ChannelDto.Response> createChannel(ChannelDto.Request request, MultipartFile file);

    ResultDTO<List<ChannelDto.Response>> readChannelRandom();

    ResultDTO<ChannelDto.Response> editChannel(ChannelEditDto.Request request,
                                               MultipartHttpServletRequest file);

    ResultDTO<ChannelDto.Response> removeChannel(ChannelRemoveDto.Request request);

    ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long communityId, Long postId);

    ResultDTO<PostCheckDto> createPost(Long channelId, PostDto.PostCreateRequest request);

    ResultDTO<PostDto.PostResponse> getPost(Long channelId, Long postId);

    ResultDTO<PostCheckDto> updatePost(Long channelId, Long postId,
                                       PostDto.PostUpdateRequest request);

    ResultDTO<PostCheckDto> deletePost(Long channelId, Long postId);

    ResultDTO<Page<PostDto.PostResponse>> getPostList(Long channelId, Integer page);

    ResultDTO<List<ChannelAllDto.Response>> readChannelAll();

    ResultDTO<Page<PostDto.PostResponse>> searchPost(Long channelId, PostSearchOption searchOption,
                                                     String keyword, Integer page);

    ResultDTO<CommentDto.CommentResponse>  createComment(Long channelId, Long postId,
                                                         CommentDto.CommentRequest request);

    ResultDTO<CheckDto> updateComment(Long channelId, Long postId, Long commentId,
                                      CommentDto.CommentRequest request);

    ResultDTO<CheckDto> deleteComment(Long channelId, Long postId, Long commentId);

    ResultDTO<Page<CommentDto.CommentResponse>> getComment(Long channelId, Long postId, Integer page);

    ResultDTO<PostLikeDto.Response> postLikeChk(Long channelId, Long postId);

    ResultDTO<PostLikeDto.Response> postLike(Long channelId, Long postId);

    ResultDTO<ChannelDto.Response> readChannel(Long channelId);
}
