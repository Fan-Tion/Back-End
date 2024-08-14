package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CommunityService {

  ResultDTO<ImageDto> uploadImage(List<MultipartFile> files, Long postId);

  ResultDTO<PostDto.PostResponse> createPost(PostDto.PostRequest request);
}
