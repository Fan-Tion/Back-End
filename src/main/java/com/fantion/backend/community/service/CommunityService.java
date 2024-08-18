package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.CheckDto;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CommunityService {

  ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long communityId, Long postId);

  ResultDTO<CheckDto> createPost(Long communityId, Long postId, PostDto.PostRequest request);

  ResultDTO<PostDto.PostResponse> getPost(Long communityId, Long postId);

  ResultDTO<CheckDto> updatePost(Long communityId, Long postId, PostDto.PostRequest request);
}
