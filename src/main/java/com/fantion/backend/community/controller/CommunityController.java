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

@RestController
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
}
