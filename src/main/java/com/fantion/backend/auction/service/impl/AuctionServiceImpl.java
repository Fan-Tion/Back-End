package com.fantion.backend.auction.service.impl;

import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_AUCTION;
import static com.fantion.backend.exception.ErrorCode.NOT_FOUND_MEMBER;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.AuctionFavoriteDto;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.entity.FavoriteAuction;
import com.fantion.backend.auction.repository.AuctionRepository;
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
import com.fantion.backend.type.SearchType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final MemberRepository memberRepository;
  private final FavoriteAuctionRepository favoriteAuctionRepository;

  private final RedisTemplate<String, Object> redisTemplate;

  private final S3Uploader s3Uploader;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private String serverUrl = "https://fantion-bucket.s3.ap-northeast-2.amazonaws.com/auction-images/";

  @Override
  @Transactional
  public ResultDTO<Map<String, Long>> createAuction(@Valid AuctionDto.Request request,
      List<MultipartFile> auctionImage) {
    auctionRepository.save(toAuction(request, null));

    /*
    * 저장한 auciton을 가져와서 변경
    * */
    Auction auction = auctionRepository.findTopByMemberOrderByAuctionIdDesc(
        memberRepository.findById(getLoginUserId()).orElseThrow(
            () -> new CustomException(NOT_FOUND_MEMBER))).orElseThrow(
                () -> new CustomException(NOT_FOUND_AUCTION));

    auction.setAuctionImage(
        setImageUrl(saveImages(auction.getAuctionId(), auctionImage)));



    return ResultDTO.of("경매 생성에 성공했습니다.", Map.of("auctionId", auction.getAuctionId()));
  }

  // 경매 상세보기
  @Override
  public ResultDTO<AuctionDto.Response> findAuction(Long auctionId) {
    // 상세보기할 경매 조회
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

    return ResultDTO.of("경매 업데이트 성공했습니다.", toResponse(auction));
  }

  /**
   * 경매 수정
   */
  @Override
  @Transactional
  public ResultDTO<AuctionDto.Response> updateAuction(
      @Valid AuctionDto.Request request,
      List<MultipartFile> auctionImage,
      Long auctionId) {
    s3Uploader.deleteFolder(auctionId);

    Auction auction = updateValue(request, auctionId, saveImages(auctionId, auctionImage));

    return ResultDTO.of("성공적으로 경매를 변경했습니다.", toResponse(auction));
  }


  /**
   * 경매 삭제
   */
  @Override
  @Transactional
  public ResultDTO<Boolean> deleteAuction(Long auctionId) {
    auctionRepository.findById(auctionId).orElseThrow(
        () -> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

    auctionRepository.deleteById(auctionId);
    s3Uploader.deleteFolder(auctionId);

    return ResultDTO.of("경매 삭제 성공했습니다.", true);
  }

  /**
   * 경매 리스트
   */
  @Override
  public ResultDTO<Page<AuctionDto.Response>> getList(int page) {
    Pageable pageable = getPageable(page);
    return ResultDTO.of("경매 전체 페이지를 불러오는데 성공했습니다.",
        covertToResponseList(auctionRepository.findAll(pageable)));
  }

  /**
   * 경매 검색
   */
  @Override
  public ResultDTO<Page<AuctionDto.Response>> getSearchList(
      int page,
      SearchType searchOption,
      CategoryType categoryType,
      String keyword) {
    Pageable pageable = getPageable(page);
    Page<Auction> auctionPage = null;

    try {
      if (searchOption == SearchType.TITLE) {
        auctionPage = auctionRepository.findByTitleContaining(keyword, pageable);
      } else if (searchOption == SearchType.CATEGORY) {
        if (categoryType == CategoryType.ALL) {
          auctionPage = auctionRepository.findAll(pageable);
        } else if (keyword == null) {
          auctionPage = auctionRepository.findByCategory(categoryType, pageable);
        } else {
          auctionPage = auctionRepository.findByCategoryAndTitleContaining(categoryType, keyword,
              pageable);
        }
      }
    } catch (Exception e) {
      throw new CustomException(ErrorCode.ENUM_INVALID_FORMAT);
    }

    return ResultDTO.of("경매 검색을 완료했습니다", covertToResponseList(auctionPage));
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
      throw new CustomException(ErrorCode.IMAGE_MALFORMED);
    } catch (InternalResourceException e) {
      throw new CustomException(ErrorCode.IMAGE_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * 옥션 기간별 거래목록 생성 및 갱신
   */
  @Override
  public void endAuctionSaveOrUpdate() {
    List<Auction> endAuctionCategoryList
        = auctionRepository.findByEndDateBetweenAndStatus(
        LocalDateTime.now().with(LocalTime.MIN),
        LocalDateTime.now().with(LocalTime.MAX),
        false);

    List<String> categoryList = convertAuctionToCategory(endAuctionCategoryList);

    Map<String, Integer> map = getAuctionDateValue();
    if (map == null) {
      map = new HashMap<>();
    }
    for(String categoryName : categoryList) {
      map.put(categoryName, map.getOrDefault(categoryName, 0) + 1);
    }

    try {
      String json = objectMapper.writeValueAsString(map);
      redisTemplate.opsForValue().set(LocalDate.now().toString(), json, Duration.ofDays(1));
    } catch (JsonProcessingException e) {
      throw new CustomException(ErrorCode.PARSING_ERROR);
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
        throw new CustomException(ErrorCode.PARSING_ERROR);
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
          serverUrl + "search?searchOption=CATEGORY&categoryOption="
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
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(5)
        .map(entry -> new CategoryDto(
            entry.getKey(), serverUrl + "search?searchOption=CATEGORY&categoryOption="
            + entry.getKey() + "&keyword=&page=0"))
        .collect(Collectors.toList());

    Random random = new Random();
    CategoryType[] categoryArray = CategoryType.values();

    while (categoryList.size() < 5) {
      String categoryTypeStr
          = categoryArray[random.nextInt(categoryArray.length)].name();

      if (ableCategoryCheck(categoryList, categoryTypeStr)) {
        categoryList.add(
            new CategoryDto(
                categoryTypeStr,
                serverUrl + "search?searchOption=CATEGORY&categoryOption="
                    + categoryTypeStr + "&keyword=&page=0"));
      }
    }

    return ResultDTO.of("인기 카테고리를 불러오는데 성공했습니다.", categoryList);
  }
  @Transactional
  @Override
  public AuctionFavoriteDto.Response favoriteChk(Long auctionId) {
    // 찜 여부 확인할 경매 조회
    Auction favoriteChkAuction = auctionRepository.findById(auctionId)
            .orElseThrow(()-> new CustomException(NOT_FOUND_AUCTION));

    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member member = memberRepository.findByEmail(loginEmail)
            .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

    // 찜 조회
    Optional<FavoriteAuction> favoriteAuction = favoriteAuctionRepository
            .findByAuctionAndMember(favoriteChkAuction, member);

    AuctionFavoriteDto.Response response = AuctionFavoriteDto.Response.builder()
            .favoriteChk(true)
            .title(favoriteChkAuction.getTitle())
            .build();

    // 찜이 되어있지 않는 경우
    if (favoriteAuction.isEmpty()) {
      response.setFavoriteChk(false);
    }

    return response;
  }
  @Transactional
  @Override
  public AuctionFavoriteDto.Response favoriteAuction(Long auctionId) {
    // 찜하거나 찜 취소할 경매 조회
    Auction favoriteChkAuction = auctionRepository.findById(auctionId)
            .orElseThrow(()-> new CustomException(NOT_FOUND_AUCTION));

    // 로그인한 사용자 가져오기
    String loginEmail = MemberAuthUtil.getLoginUserId();

    // 사용자 조회
    Member member = memberRepository.findByEmail(loginEmail)
            .orElseThrow(()-> new CustomException(NOT_FOUND_MEMBER));

    // 찜 조회
    Optional<FavoriteAuction> favoriteAuction = favoriteAuctionRepository
            .findByAuctionAndMember(favoriteChkAuction, member);

    AuctionFavoriteDto.Response response = AuctionFavoriteDto.Response.builder()
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

    return response;
  }

  private Auction updateValue(AuctionDto.Request request, Long auctionId, List<String> auctionImgList) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_AUCTION));

    auction.setMember(memberRepository.findById(auctionId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)));
    auction.setTitle(request.getTitle());
    auction.setAuctionType(request.isAuctionType());
    auction.setCategory(request.getCategory());
    auction.setAuctionImage(setImageUrl(auctionImgList));
    auction.setDescription(request.getDescription());
    auction.setCurrentBidPrice(request.getCurrentBidPrice());
    auction.setCurrentBidder(auction.getCurrentBidder());
    auction.setBuyNowPrice(request.getBuyNowPrice());
    auction.setFavoriteCnt(auction.getFavoriteCnt());
    auction.setCreateDate(LocalDate.now());
    auction.setEndDate(request.getEndDate());
    auction.setStatus(auction.isStatus());

    return auction;
  }

  private Auction toAuction(AuctionDto.Request request, List<String> auctionImageList) {
    return Auction.builder()
        .member(memberRepository.findById(getLoginUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)))
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

  private AuctionDto.Response toResponse(Auction auction) {
    return AuctionDto.Response.builder()
        .auctionId(auction.getAuctionId())
        .title(auction.getTitle())
        .auctionUserNickname(memberRepository.findById(auction.getMember().getMemberId())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER)).getNickname())
        .category(auction.getCategory())
        .auctionType(auction.isAuctionType())
        .auctionImage(
            Arrays.stream(auction.getAuctionImage().split(","))
                .map(x -> serverUrl + x).toList())
        .description(auction.getDescription())
        .currentBidPrice(auction.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(auction.getBuyNowPrice())
        .favoritePrice(auction.getFavoriteCnt())
        .createDate(auction.getCreateDate())
        .endDate(LocalDateTime.of(auction.getEndDate(), LocalTime.of(23, 59,59)))
        .status(auction.isStatus())
        .build();
  }

  /**
   * Page<Auction> -> Page<Response>
   */
  private Page<AuctionDto.Response> covertToResponseList(Page<Auction> auctionList) {
    List<AuctionDto.Response> responseList = auctionList.stream().map(this::toResponse)
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
          imageUrls.add(imageUrl.replace(serverUrl, ""));
        } else {
          throw new CustomException(ErrorCode.IMAGE_EXCEPTION);
        }
      }

      return imageUrls;
    } catch (IOException e) {
      throw new CustomException(ErrorCode.IMAGE_IO_ERROR);
    }
  }

  /**
   * 이미지 url세팅
   */
  private String setImageUrl(List<String> auctionImgList) {
    try {
      return auctionImgList == null ? null : String.join(",", auctionImgList);
    } catch (SecurityException e) {
      throw new CustomException(ErrorCode.IMAGE_ACCESS_DENIED);
    } catch (InvalidPathException e) {
      throw new CustomException(ErrorCode.IMAGE_NOT_HAVE_PATH);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.IMAGE_EXCEPTION);
    }
  }

  /**
   * 파일 확장자를 추출
   */
  private static String getFileExtension(String filePath) {
    String fileName = new File(filePath).getName();
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
  }

  /**
   * 폴더 비우기
   */
  public void emptyDirectory(Path directory) {
    if (Files.isDirectory(directory)) {
      try {
        DirectoryStream<Path> stream
            = Files.newDirectoryStream(directory);
        for (Path entry : stream) {
          deleteRecursively(entry);
        }
      } catch (IOException e) {
        throw new CustomException(ErrorCode.IMAGE_IO_ERROR);
      }
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
          .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_MEMBER))
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

  private static boolean ableCategoryCheck(List<CategoryDto> categoryList, String categoryTypeStr) {
    return categoryList.stream().noneMatch(dto -> dto.getTitle().equals(categoryTypeStr))
        && !categoryTypeStr.equals(CategoryType.ALL.name())
        && !categoryTypeStr.equals(CategoryType.OTHER.name());
  }

  private List<String> convertAuctionToCategory(List<Auction> endAuctionCategoryList) {
    return endAuctionCategoryList.stream().map(x -> x.getCategory().name()).toList();
  }
}
