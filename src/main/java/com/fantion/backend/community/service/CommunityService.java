package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.CheckDto;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.type.PostSearchOption;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CommunityService {

  ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long communityId, Long postId);

  ResultDTO<CheckDto> createPost(Long communityId, PostDto.PostCreateRequest request);

  ResultDTO<PostDto.PostResponse> getPost(Long communityId, Long postId);

  ResultDTO<CheckDto> updatePost(Long communityId, Long postId, PostDto.PostUpdateRequest request);

  ResultDTO<CheckDto> deletePost(Long communityId, Long postId);

  ResultDTO<Page<PostDto.PostResponse>> getPostList(Long communityId, Integer page);

  ResultDTO<Page<PostDto.PostResponse>> searchPost(Long communityId, PostSearchOption searchOption, String keyword, Integer page);
}
