package com.fantion.backend.community.controller;

import com.fantion.backend.common.dto.ResultDTO;

import com.fantion.backend.community.dto.ChannelDto;
import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.community.dto.ChannelRemoveDto;
import com.fantion.backend.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.fantion.backend.community.dto.CheckDto;
import com.fantion.backend.community.dto.ImageDto;
import com.fantion.backend.community.dto.PostDto;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.type.PostSearchOption;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "채널 랜덤 조회", description = "랜덤으로 채널 조회할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 채널이 랜덤으로 조회되었습니다.")
    @GetMapping("/channel/random")
    private ResultDTO<List<ChannelDto.Response>> readChannelRandom(){
        return communityService.readChannelRandom();
    }

    @Operation(summary = "채널 생성", description = "채널을 생성할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 채널이 생성되었습니다.")
    @PostMapping("/channel")
    private ResultDTO<ChannelDto.Response> createChannel(@RequestBody ChannelDto.Request request,
                                                         @RequestPart(value = "file",required = false) MultipartFile file){
        return communityService.createChannel(request,file);
    }

    @Operation(summary = "채널 수정", description = "채널을 수정할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 채널이 수정되었습니다.")
    @PutMapping("/channel")
    private ResultDTO<ChannelDto.Response> editChannel(@RequestBody ChannelEditDto.Request request){
        return communityService.editChannel(request);
    }

    @Operation(summary = "채널 삭제", description = "채널을 삭제할 때 사용하는 API")
    @ApiResponse(responseCode = "200", description = "성공적으로 채널이 삭제되었습니다.")
    @DeleteMapping("/channel")
    private ResultDTO<ChannelDto.Response> deleteChannel(@RequestBody ChannelRemoveDto.Request request){
        return communityService.removeChannel(request);
    }

  @PostMapping("/{channelId}/image")
  public ResponseEntity<ResultDTO<ImageDto>> uploadImage(
      @PathVariable Long channelId, @RequestPart("file") List<MultipartFile> files,
      @RequestParam(required = false) Long postId) {

    ResultDTO<ImageDto> result = communityService.uploadImage(files, channelId, postId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{channelId}/post")
  public ResponseEntity<ResultDTO<CheckDto>> createPost(
      @PathVariable Long channelId, @RequestBody @Valid PostDto.PostCreateRequest request) {
    ResultDTO<CheckDto> result = communityService.createPost(channelId, request);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{channelId}/post/{postId}")
  public ResponseEntity<ResultDTO<PostDto.PostResponse>> getPost(@PathVariable Long channelId,
      @PathVariable Long postId) {
    ResultDTO<PostDto.PostResponse> result = communityService.getPost(channelId, postId);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/{channelId}/post/{postId}")
  public ResponseEntity<ResultDTO<CheckDto>> updatePost(@PathVariable Long channelId,
      @PathVariable Long postId, @RequestBody @Valid PostDto.PostUpdateRequest request) {
    ResultDTO<CheckDto> result = communityService.updatePost(channelId, postId, request);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{channelId}/post/{postId}")
  public ResponseEntity<ResultDTO<CheckDto>> deletePost(@PathVariable Long channelId,
      @PathVariable Long postId) {
    ResultDTO<CheckDto> result = communityService.deletePost(channelId, postId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{channelId}")
  public ResponseEntity<ResultDTO<Page<PostDto.PostResponse>>> getPostList(
      @PathVariable Long channelId,
      @RequestParam(name = "page", defaultValue = "0") Integer page) {
    ResultDTO<Page<PostDto.PostResponse>> result = communityService.getPostList(channelId, page);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{channelId}/search")
  public ResponseEntity<ResultDTO<Page<PostDto.PostResponse>>> searchPost(
      @PathVariable Long channelId, @RequestParam PostSearchOption searchOption,
      @RequestParam String keyword, @RequestParam(defaultValue = "0") Integer page) {
    ResultDTO<Page<PostDto.PostResponse>> result = communityService.searchPost(channelId, searchOption, keyword, page);
    return ResponseEntity.ok(result);
  }

}
