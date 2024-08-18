package com.fantion.backend.community.service.impl;

import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.community.dto.ChannelDto;
import com.fantion.backend.community.dto.ChannelEditDto;
import com.fantion.backend.community.dto.ChannelRemoveDto;
import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.community.repository.ChannelRepository;
import com.fantion.backend.community.service.CommunityService;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.ChannelStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_CHANNEL;
import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_MEMBER;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final S3Uploader s3Uploader;

    @Override
    public ResultDTO<List<ChannelDto.Response>> readChannelRandom() {
        List<Channel> channelRandomList = channelRepository.findChannelRandom();
        List<ChannelDto.Response> responseList = channelRandomList.stream().map(ChannelDto::response).toList();
        return ResultDTO.of("성공적으로 채널이 랜덤으로 조회되었습니다.", responseList);
    }

    @Transactional
    @Override
    public ResultDTO<ChannelDto.Response> createChannel(ChannelDto.Request request, MultipartFile file) {
        // 로그인한 사용자 가져오기
        String loginEmail = MemberAuthUtil.getLoginUserId();

        // 사용자 조회
        Member organizer = memberRepository.findByEmail(loginEmail)
                .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

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

        return ResultDTO.of("성공적으로 채널이 생성되었습니다.", ChannelDto.response(channelRepository.save(newChannel)));
    }
    @Transactional
    @Override
    public ResultDTO<ChannelDto.Response> editChannel(ChannelEditDto.Request request) {
        // 수정하려는 채널 조회
        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_CHANNEL));

        return ResultDTO.of("성공적으로 채널이 수정되었습니다.", ChannelDto.response(channel.editChannel(request)));
    }
    @Transactional
    @Override
    public ResultDTO<ChannelDto.Response> removeChannel(ChannelRemoveDto.Request request) {
        // 삭제하려는 채널 조회
        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(()-> new CustomException(NOT_FOUND_CHANNEL));

        // 채널 이미지가 있는 경우
        if (channel.getImage() != null){
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

        ChannelDto.Response response = ChannelDto.response(channel);
        return ResultDTO.of("성공적으로 채널이 삭제되었습니다.", response);
    }
}
