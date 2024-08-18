package com.fantion.backend.community.service;

import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ChannelDto;
import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.community.dto.ChannelRemoveDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface CommunityService {

    ResultDTO<ChannelDto.Response> createChannel(ChannelDto.Request request, MultipartFile file);

    ResultDTO<List<ChannelDto.Response>>readChannelRandom();

    ResultDTO<ChannelDto.Response> editChannel(ChannelEditDto.Request request);

    ResultDTO<ChannelDto.Response> removeChannel(ChannelRemoveDto.Request request);
}
