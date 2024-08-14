package com.fantion.backend.community.controller;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.community.service.CommunityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {

  private final CommunityService communityService;

  @PostMapping("/{communityId}/image")
  public ResponseEntity<ResultDTO<ImageDto>> uploadImage(
      @RequestPart("file") List<MultipartFile> files, @PathVariable Long communityId,
      @RequestParam Long postId) {

    ResultDTO<ImageDto> result = communityService.uploadImage(files, communityId, postId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{communityId}/post")
  public ResponseEntity<ResultDTO<PostDto.PostResponse>> createPost(
      @RequestBody PostDto.PostRequest request, @PathVariable Long communityId) {
    ResultDTO<PostDto.PostResponse> result = communityService.createPost(request, communityId);
    return ResponseEntity.ok(result);
  }
}
