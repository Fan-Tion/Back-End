package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.community.dto.PostDto.PostRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CommunityService {

  ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long communityId, Long postId);

  ResultDTO<PostDto.PostResponse> createPost(PostRequest request, Long communityId);

  ResultDTO<PostDto.PostResponse> getPost(Long communityId, Long postId);
}
