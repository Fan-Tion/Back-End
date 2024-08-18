package com.fantion.backend.auction.service.impl;

import static com.fantion.backend.exception.ErrorCode.*;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.AuctionRequest;
import com.fantion.backend.auction.dto.AuctionDto.AuctionResponse;
import com.fantion.backend.auction.dto.AuctionFavoriteDto.Response;
import com.fantion.backend.auction.dto.AuctionReportDto.AuctionReportRequest;
import com.fantion.backend.auction.dto.AuctionReportDto.AuctionReportResponse;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.AuctionReport;
import com.fantion.backend.auction.entity.Bid;
import com.fantion.backend.auction.entity.FavoriteAuction;
import com.fantion.backend.auction.repository.AuctionReportRepository;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.repository.BidRepository;
import com.fantion.backend.auction.repository.FavoriteAuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.common.component.S3Uploader;
import com.fantion.backend.common.dto.ResultDTO;
import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.auth.MemberAuthUtil;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.CategoryType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.jca.endpoint.GenericMessageEndpointFactory.InternalResourceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final MemberRepository memberRepository;
  private final FavoriteAuctionRepository favoriteAuctionRepository;
  private final BidRepository bidRepository;
  private final AuctionReportRepository auctionReportRepository;

  private final RedisTemplate<String, Object> redisTemplate;

  private final S3Uploader s3Uploader;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String serverUrl = "https://www.fantion.kro.kr/auction/";

  @Value("${s3.auction-file-path}")
  private String imageUrl;

  @Override
  @Transactional
  public ResultDTO<Map<String, Long>> createAuction(@Valid AuctionDto.AuctionRequest request,
      List<MultipartFile> auctionImage) {

    auctionRepository.save(toAuction(request, null));

    /*
     * 저장한 auciton을 가져와서 변경
     * */
    Auction auction = auctionRepository.findTopByMemberOrderByAuctionIdDesc(
            memberRepository.findById(getLoginUserId())
                .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER)))
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    auction.setAuctionImage(setImageUrl(saveImages(auction.getAuctionId(), auctionImage)));

    return ResultDTO.of("경매 생성에 성공했습니다.", Map.of("auctionId", auction.getAuctionId()));
  }

  // 경매 상세보기
  @Override
  public ResultDTO<AuctionResponse> findAuction(Long auctionId) {
    // 상세보기할 경매 조회
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    return ResultDTO.of("성공적으로 상세보기할 경매가 조회되었습니다.", toResponse(auction));
  }

  /**
   * 경매 수정
   */
  @Override
  @Transactional
  public ResultDTO<AuctionResponse> updateAuction(@Valid AuctionDto.AuctionRequest request,
      MultipartHttpServletRequest file, Long auctionId) {

    Map<String, String[]> imageType = file.getParameterMap();
    Map<String, MultipartFile> auctionImage = file.getFileMap();

    List<String> auctionImgList = updateImages(auctionId, imageType, auctionImage);

    Auction auction = updateValue(request, auctionId, auctionImgList);

    return ResultDTO.of("성공적으로 경매를 변경했습니다.", toResponse(auction));
  }

  /**
   * 경매 삭제
   */
  @Override
  @Transactional
  public ResultDTO<Boolean> deleteAuction(Long auctionId) {
    auctionRepository.findById(auctionId).orElseThrow(
        () -> new CustomException(NOT_FOUND_AUCTION));

    auctionRepository.deleteById(auctionId);
    s3Uploader.deleteFolder(auctionId);

    return ResultDTO.of("경매 삭제 성공했습니다.", true);
  }

  /**
   * 경매 리스트
   */
  @Override
  public ResultDTO<Page<AuctionResponse>> getList(int page) {
    Pageable pageable = getPageable(page);

    Page<Auction> auctionPage = auctionRepository.findAllByStatus(true, pageable);
    List<Auction> auctionList = new ArrayList<>(auctionPage.getContent());
    Collections.shuffle(auctionList);
    auctionPage = new PageImpl<>(auctionList, pageable, auctionPage.getTotalElements());

    return ResultDTO.of("경매 전체 페이지를 불러오는데 성공했습니다.", covertToResponseList(auctionPage));
  }

  /**
   * 경매 검색
   */
  @Override
  public ResultDTO<Page<AuctionResponse>> getSearchList(int page, CategoryType category,
      String keyword) {

    Pageable pageable = getPageable(page);
    Page<Auction> auctionPage;

    try {
      if (keyword != null) {
        if (category.equals(CategoryType.ALL)) {
          auctionPage = auctionRepository.findAllByTitleContaining(keyword, pageable);
        } else {
          auctionPage = auctionRepository.findByCategoryAndTitleContaining(category, keyword,
              pageable);
        }
      } else {
        if (category.equals(CategoryType.ALL)) {
          auctionPage = auctionRepository.findAll(pageable);
        } else {
          auctionPage = auctionRepository.findByCategory(category, pageable);
        }
      }
    } catch (Exception e) {
      throw new CustomException(ENUM_INVALID_FORMAT);
    }

    return ResultDTO.of("경매 검색을 완료했습니다.", covertToResponseList(auctionPage));
  }

  /**
   * 이미지 가져오기
   */
  @Override
  public Resource getImage(Path imagePath, HttpHeaders headers) {
    try {
      Resource resource = new UrlResource(imagePath.toUri());
      return resource;
    } catch (MalformedURLException e) {
      throw new CustomException(IMAGE_MALFORMED);
    } catch (InternalResourceException e) {
      throw new CustomException(IMAGE_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 옥션 기간별 거래목록 생성 및 갱신
   */
  @Override
  public void endAuctionSaveOrUpdate() {
    List<Auction> endAuctionCategoryList
        = auctionRepository.findByEndDateAndStatus(LocalDate.now().minusDays(1), false);

    List<String> categoryList = convertAuctionToCategory(endAuctionCategoryList);

    Map<String, Integer> map = getAuctionDateValue();
    if (map == null) {
      map = new HashMap<>();
    }
    for (String categoryName : categoryList) {
      map.put(categoryName, map.getOrDefault(categoryName, 0) + 1);
    }
  }

  /**
   * 전날 거래량 가져오기
   */
  @Override
  public Map<String, Integer> getAuctionDateValue() {
    String json = (String) redisTemplate.opsForValue()
        .get(LocalDate.now().minusDays(0).toString());
    if (json != null) {
      try {
        return objectMapper.readValue(json, Map.class);
      } catch (JsonProcessingException e) {
        throw new CustomException(PARSING_ERROR);
      }
    }
    return null;
  }


  @Override
  public ResultDTO<List<CategoryDto>> getAllAuctionCategory() {
    CategoryType[] categoryArray = CategoryType.values();
    List<CategoryDto> categoryList = new ArrayList<>();

    for (int i = 1; i < categoryArray.length; i++) {
      categoryList.add(new CategoryDto(
          categoryArray[i].name(),
          serverUrl + "search?&categoryOption="
              + categoryArray[i].name() + "&keyword=&page=0"));
    }

    return ResultDTO.of("전체 카테고리를 불러오는데 성공했습니다.", categoryList);
  }

  @Override
  public ResultDTO<List<CategoryDto>> getFavoriteAuctionCategory() {
    Map<String, Integer> map = getAuctionDateValue();
    if (map == null) {
      map = new HashMap<>();
    }

    List<CategoryDto> categoryList = map.entrySet()
        .stream()
        .sorted(Entry.<String, Integer>comparingByValue().reversed())
        .map(entry -> new CategoryDto(
            entry.getKey(), serverUrl + "search?&categoryOption="
            + entry.getKey() + "&keyword=&page=0"))
        .collect(Collectors.toList());

    Random random = new Random();
    CategoryType[] categoryArray = CategoryType.values();
    Set<String> categorySet
        = categoryList.stream().map(CategoryDto::getTitle).collect(Collectors.toSet());

    while (categoryList.size() < categoryArray.length) {
      String categoryTypeStr = categoryArray[random.nextInt(categoryArray.length)].name();

      if (categorySet.add(categoryTypeStr)) {
        categoryList.add(new CategoryDto(
            categoryTypeStr,
            serverUrl + "search?&categoryOption=" + categoryTypeStr
                + "&keyword=&page=0"
        ));
      }
    }

    return ResultDTO.of("인기 카테고리를 불러오는데 성공했습니다.", categoryList);
  }

  @Transactional
  @Override
  public ResultDTO<Response> favoriteChk(Long auctionId) {
    // 찜 여부 확인할 경매 조회
    Auction favoriteChkAuction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member member = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    // 찜 조회
    Optional<FavoriteAuction> favoriteAuction = favoriteAuctionRepository
        .findByAuctionAndMember(favoriteChkAuction, member);

    Response response = Response.builder()
        .favoriteChk(true)
        .title(favoriteChkAuction.getTitle())
        .build();

    // 찜이 되어있지 않는 경우
    if (favoriteAuction.isEmpty()) {
      response.setFavoriteChk(false);
    }

    return ResultDTO.of("성공적으로 찜 여부가 조회되었습니다.", response);
  }

  @Transactional
  @Override
  public ResultDTO<Response> favoriteAuction(Long auctionId) {
    // 찜하거나 찜 취소할 경매 조회
    Auction favoriteChkAuction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member member = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    // 찜 조회
    Optional<FavoriteAuction> favoriteAuction = favoriteAuctionRepository
        .findByAuctionAndMember(favoriteChkAuction, member);

    Response response = Response.builder()
        .favoriteChk(true)
        .title(favoriteChkAuction.getTitle())
        .build();

    // 찜이 되어있지 않는 경우 찜 하기
    if (favoriteAuction.isEmpty()) {
      FavoriteAuction favorite = FavoriteAuction.builder()
          .auction(favoriteChkAuction)
          .member(member)
          .build();
      favoriteAuctionRepository.save(favorite);

    } else {
      // 찜이 되어 있는 경우 찜 취소
      favoriteAuctionRepository.delete(favoriteAuction.get());
      response.setFavoriteChk(false);

    }

    return ResultDTO.of("성공적으로 찜 또는 찜취소가 되었습니다.", response);
  }

  @Override
  public ResultDTO<Page<AuctionResponse>> getSellAuctionList(int page) {
    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member seller = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    Pageable pageable = getPageable(page);

    // 경매 주최자가 본인인 경매 물품 조회
    Page<AuctionResponse> response = covertToResponseList(
        auctionRepository.findByMember(seller, pageable));
    return ResultDTO.of("성공적으로 판매한 경매 목룍이 조회되었습니다.", response);
  }

  @Override
  public ResultDTO<Page<AuctionResponse>> getBuyAuctionList(int page) {
    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member buyer = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    Pageable pageable = getPageable(page);

    // 현재 입찰자가 본인이면서 인수확인이 되어있는 경매 물품 조회
    Page<AuctionResponse> response = covertToResponseList(auctionRepository
        .findByStatusAndReceiveChkAndCurrentBidder(false, true, buyer.getNickname(), pageable));
    return ResultDTO.of("성공적으로 구매한 경매 목룍이 조회되었습니다.", response);
  }

  @Override
  public ResultDTO<Page<AuctionResponse>> getJoinAuctionList(int page) {
    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member bidder = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    Pageable pageable = getPageable(page);

    // 입찰 목록에서 경매목록 추출
    Page<AuctionResponse> response = covertToResponseList(
        bidRepository.findByBidder(bidder, pageable).map(Bid::getAuctionId));
    return ResultDTO.of("성공적으로 입찰한 경매 목룍이 조회되었습니다.", response);
  }

  @Override
  public ResultDTO<Page<AuctionResponse>> getFavoriteAuctionList(int page) {
    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member member = memberRepository.findByEmail(loginEmail)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    Pageable pageable = getPageable(page);

    // 찜한 목록에서 경매목록 추출
    Page<AuctionResponse> response = covertToResponseList(
        favoriteAuctionRepository.findByMember(member, pageable).map(FavoriteAuction::getAuction));
    return ResultDTO.of("성공적으로 찜한 경매 목룍이 조회되었습니다.", response);
  }

  @Override
  public ResultDTO<AuctionReportResponse> reportAuction(Long auctionId,
      AuctionReportRequest request) {
    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    auctionReportRepository.findByAuctionIdAndMemberId(auction, member).ifPresent(report -> {
      throw new CustomException(ALREADY_REPORT_AUCTION);
    });

    AuctionReport auctionReport = AuctionReport.builder()
        .auctionId(auction)
        .memberId(member)
        .description(request.getDescription())
        .build();
    auctionReportRepository.save(auctionReport);

    return ResultDTO.of("성공적으로 경매신고가 완료되었습니다.",
        AuctionReportResponse.builder().title(auction.getTitle()).build());
  }

  private Auction updateValue(AuctionRequest request, Long auctionId,
      List<String> auctionImgList) {

    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));

    String email = MemberAuthUtil.getLoginUserId();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    if (member != auction.getMember()) {
      throw new CustomException(NOT_AUCTION_SELLER);
    }

    String url = setImageUrl(auctionImgList);

    Auction updateAuction = auction.toBuilder()
        .title(request.getTitle())
        .auctionType(request.isAuctionType())
        .category(request.getCategory())
        .auctionImage(url)
        .description(request.getDescription())
        .currentBidPrice(request.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(request.getBuyNowPrice())
        .favoriteCnt(auction.getFavoriteCnt())
        .createDate(LocalDate.now())
        .endDate(request.getEndDate())
        .status(auction.isStatus())
        .build();
    auctionRepository.save(updateAuction);

    return updateAuction;
  }

  private Auction toAuction(AuctionRequest request, List<String> auctionImageList) {
    return Auction.builder()
        .member(memberRepository.findById(getLoginUserId())
            .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER)))
        .title(request.getTitle())
        .category(request.getCategory())
        .auctionType(request.isAuctionType())
        .auctionImage(setImageUrl(auctionImageList))
        .description(request.getDescription())
        .currentBidPrice(request.getCurrentBidPrice())
        .currentBidder(null)
        .buyNowPrice(request.getBuyNowPrice())
        .favoriteCnt(0L)
        .createDate(LocalDate.now())
        .endDate(request.getEndDate())
        .status(true)
        .build();
  }

  private AuctionResponse toResponse(Auction auction) {
    Member seller = memberRepository.findById(auction.getMember().getMemberId())
        .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER));

    // 이미지가 존재하지 않는 경우
    if (auction.getAuctionImage() == null) {
      throw new CustomException(IMAGE_NOT_FOUND);
    }

    return AuctionResponse.builder()
        .auctionId(auction.getAuctionId())
        .title(auction.getTitle())
        .auctionUserNickname(seller.getNickname())
        .category(auction.getCategory())
        .auctionType(auction.isAuctionType())
        .auctionImage(
            Arrays.stream(auction.getAuctionImage().split(",")).map(x -> imageUrl + x).toList())
        .description(auction.getDescription())
        .currentBidPrice(auction.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(auction.getBuyNowPrice())
        .favoriteCnt(auction.getFavoriteCnt())
        .createDate(auction.getCreateDate())
        .endDate(LocalDateTime.of(auction.getEndDate(), LocalTime.of(23, 59, 59)))
        .status(auction.isStatus())
        .rating(seller.getRating())
        .BidCount(bidRepository.countByAuctionId(auction))
        .build();
  }

  /**
   * Page<Auction> -> Page<Response>
   */
  private Page<AuctionResponse> covertToResponseList(Page<Auction> auctionList) {
    List<AuctionResponse> responseList = auctionList.stream().map(this::toResponse)
        .collect(Collectors.toList());

    return new PageImpl<>(responseList, auctionList.getPageable(), auctionList.getTotalElements());
  }

  /**
   * 이미지 저장
   */

  public List<String> saveImages(Long auctionId, List<MultipartFile> images) {
    try {
      List<String> imageUrls = new ArrayList<>();
      for (int i = 0; i < images.size(); i++) {
        if (images.get(i) != null && !images.get(i).isEmpty()) {
          String imageUrl = s3Uploader.upload(images.get(i), "auction-images/" + auctionId, i + 1);
          imageUrls.add(imageUrl.replace(this.imageUrl, ""));
        } else {
          throw new CustomException(ErrorCode.IMAGE_EXCEPTION);
        }
      }

      return imageUrls;
    } catch (IOException e) {
      throw new CustomException(ErrorCode.IMAGE_IO_ERROR);
    }
  }

  public List<String> updateImages(Long auctionId, Map<String, String[]> imageType,
      Map<String, MultipartFile> images) {

    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_AUCTION));
    String currentImage = auction.getAuctionImage();
    String[] currentImages = currentImage.split(",");

    // URL과 파일을 처리할 리스트 초기화
    List<String> auctionImageUrl = new ArrayList<>();

    int cnt = 0;
    for (int i = 0; i < imageType.size(); i++) {
      if (imageType.get("auctionImage[" + i + "].value") != null) {
        cnt += 1;
      }
    }

    // 순서와 타입에 따라 데이터를 처리
    try {
      for (int i = 0; i < imageType.size() - cnt; i++) {
        String type = imageType.get("auctionImage[" + i + "].type")[0].toString();
        if (type.equals("url")) {
          String url = imageType.get("auctionImage[" + i + "].value")[0].toString();
          auctionImageUrl.add(url.replace(this.imageUrl, ""));
        } else if (type.equals("file")) {
          MultipartFile file = images.get("auctionImage[" + i + "].value");
          String imageUrl = s3Uploader.upload(file, "auction-images/" + auctionId, i + 1);
          auctionImageUrl.add(imageUrl.replace(this.imageUrl, ""));
        }
      }

      // currentImages 중 auctionImageUrl에 없는 이미지를 삭제
      for (String image : currentImages) {
        if (!auctionImageUrl.contains(image)) {
          URL exProfileImageUrl = new URL(imageUrl+image);
          String exProfileImage = exProfileImageUrl.getPath().substring(1);
          s3Uploader.deleteFile(exProfileImage);
        }
      }

      return auctionImageUrl;
    } catch (IOException e) {
      throw new CustomException(IMAGE_IO_ERROR);
    }
  }

  /**
   * 이미지 url세팅
   */
  private String setImageUrl(List<String> auctionImgList) {
    try {
      return auctionImgList == null ? null : String.join(",", auctionImgList);
    } catch (SecurityException e) {
      throw new CustomException(IMAGE_ACCESS_DENIED);
    } catch (InvalidPathException e) {
      throw new CustomException(IMAGE_NOT_HAVE_PATH);
    } catch (Exception e) {
      throw new CustomException(IMAGE_EXCEPTION);
    }
  }

  /**
   * 페이지 갯수 및 페이지 번호 설정
   */
  private static PageRequest getPageable(int page) {
    return PageRequest.of(page, 10);
  }

  private Long getLoginUserId() {
    String email = getLoginUserEmail();
    if (email != null) {
      return memberRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(NOT_FOUND_MEMBER))
          .getMemberId();
    }
    throw new RuntimeException("User is not authenticated");
  }

  public String getLoginUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails) {
        return ((UserDetails) principal).getUsername();
      } else {
        return principal.toString();
      }
    }
    return null;
  }

  private List<String> convertAuctionToCategory(List<Auction> endAuctionCategoryList) {
    return endAuctionCategoryList.stream().map(x -> x.getCategory().name()).toList();
  }
}