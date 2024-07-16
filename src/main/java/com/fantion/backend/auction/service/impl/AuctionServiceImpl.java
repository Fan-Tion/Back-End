package com.fantion.backend.auction.service.impl;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.CategoryDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.exception.impl.AuctionHttpMessageNotReadableException;
import com.fantion.backend.exception.impl.AuctionJsonProcessingException;
import com.fantion.backend.exception.impl.AuctionNotFoundException;
import com.fantion.backend.exception.impl.ImageException;
import com.fantion.backend.exception.impl.ImageIOException;
import com.fantion.backend.exception.impl.ImageInternalServerException;
import com.fantion.backend.exception.impl.ImageInvalidPathException;
import com.fantion.backend.exception.impl.ImageMalformedURLException;
import com.fantion.backend.exception.impl.ImageSecurityException;
import com.fantion.backend.exception.impl.NotFoundMemberException;
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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
  @Autowired
  private final MemberRepository memberRepository;

  private final RedisTemplate<String, Object> redisTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private String serverUrl = "http://localhost:8080/auction/";

  @Override
  @Transactional
  public AuctionDto.Response createAuction(@Valid AuctionDto.Request request, List<MultipartFile> auctionImage) {
    saveImages(auctionImage);

    Auction auction = toAuction(request);

    auctionRepository.save(auction);

    return toResponse(auction);
  }

  // 경매 상세보기
  @Override
  public AuctionDto.Response findAuction(Long auctionId) {
    // 상세보기할 경매 조회
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    return toResponse(auction);
  }

  /**
   * 경매 수정
   */
  @Override
  @Transactional
  public AuctionDto.Response updateAuction(
      @Valid AuctionDto.Request request,
      List<MultipartFile> auctionImage,
      Long auctionId) {
    Auction auction = updateValue(request, auctionId);

    emptyDirectory(getImgPath());
    saveImages(auctionImage);

    return toResponse(auction);
  }


  /**
   * 경매 삭제
   */
  @Override
  @Transactional
  public boolean deleteAuction(Long auctionId) {
    auctionRepository.deleteById(auctionId);

    emptyDirectory(getImgPath());

    return true;
  }

  /**
   * 경매 리스트
   */
  @Override
  public Page<AuctionDto.Response> getList(int page) {
    Pageable pageable = getPageable(page);
    return covertToResponseList(auctionRepository.findAll(pageable));
  }

  /**
   * 경매 검색
   */
  @Override
  public Page<AuctionDto.Response> getSearchList(
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
        } else if (keyword == null){
          auctionPage = auctionRepository.findByCategory(categoryType, pageable);
        } else {
          auctionPage = auctionRepository.findByCategoryAndTitleContaining(categoryType, keyword, pageable);
        }
      }
    } catch (Exception e) {
      throw new AuctionHttpMessageNotReadableException();
    }

    return covertToResponseList(auctionPage);
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
      throw new ImageMalformedURLException();
    } catch (InternalResourceException e) {
      throw new ImageInternalServerException();
    }
  }

  /**
   * 옥션 기간별 거래목록 생성 및 갱신
   */
  @Override
  public void endAuctionSaveOrUpdate(String categoryName) {
    Map<String, Integer> map = getAuctionDateValue();
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(categoryName, map.getOrDefault(categoryName, 0) + 1);

    try {
      String json = objectMapper.writeValueAsString(map);
      redisTemplate.opsForValue().set(LocalDate.now().toString(), json, Duration.ofDays(1));
    } catch (JsonProcessingException e) {
      throw new AuctionJsonProcessingException();
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
        throw new AuctionJsonProcessingException();
      }
    }
    return null;
  }


  @Override
  public List<CategoryDto> getAllAuctionCategory() {
    CategoryType[] categoryArray = CategoryType.values();
    List<CategoryDto> categoryList = new ArrayList<>();

    for (int i = 1; i < categoryArray.length; i++) {
      categoryList.add(new CategoryDto(
          categoryArray[i].name(),
          serverUrl + "search?searchOption=CATEGORY&categoryOption="
              + categoryArray[i].name() + "&keyword=&page=0"));
    }

    return categoryList;
  }

  @Override
  public List<CategoryDto> getFavoriteAuctionCategory(Map<String, Integer> map) {
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

    return categoryList;
  }


  private Auction updateValue(AuctionDto.Request request, Long auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    auction.setMember(memberRepository.findByEmail(getLoginUserEmail())
        .orElseThrow(NotFoundMemberException::new));
    auction.setTitle(request.getTitle());
    auction.setAuctionType(request.isAuctionType());
    auction.setCategory(request.getCategory());
    auction.setAuctionImage(setImageUrl());
    auction.setDescription(request.getDescription());
    auction.setCurrentBidPrice(request.getCurrentBidPrice());
    auction.setCurrentBidder(null);
    auction.setBuyNowPrice(request.getBuyNowPrice());
    auction.setFavoriteCnt(0L);
    auction.setCreateDate(LocalDateTime.now());
    auction.setEndDate(request.getEndDate());
    auction.setStatus(true);

    return auction;
  }

  private Auction toAuction(AuctionDto.Request request) {
    return Auction.builder()
        .member(memberRepository.findByEmail(getLoginUserEmail())
            .orElseThrow(NotFoundMemberException::new))
        .title(request.getTitle())
        .category(request.getCategory())
        .auctionType(request.isAuctionType())
        .auctionImage(setImageUrl())
        .description(request.getDescription())
        .currentBidPrice(request.getCurrentBidPrice())
        .currentBidder(null)
        .buyNowPrice(request.getBuyNowPrice())
        .favoriteCnt(0L)
        .createDate(LocalDateTime.now())
        .endDate(request.getEndDate())
        .status(true)
        .build();
  }

  private AuctionDto.Response toResponse(Auction auction) {
    return AuctionDto.Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(memberRepository.findByEmail(getLoginUserEmail())
            .orElseThrow(NotFoundMemberException::new).getNickname())
        .category(auction.getCategory())
        .auctionType(auction.isAuctionType())
        .auctionImage(
            Arrays.stream(auction.getAuctionImage().split(",")).toList())
        .description(auction.getDescription())
        .currentBidPrice(auction.getCurrentBidPrice())
        .currentBidder(auction.getCurrentBidder())
        .buyNowPrice(auction.getBuyNowPrice())
        .favoritePrice(auction.getFavoriteCnt())
        .createDate(auction.getCreateDate())
        .endDate(auction.getEndDate())
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
  public void saveImages(List<MultipartFile> images) {
    try {
      if (!Files.exists(getImgPath())) {
        Files.createDirectories(getImgPath());
      }

      for (int i = 0; i < images.size(); i++) {
        String filename = (i + 1) + ".jpg";
        Path filePath = getImgPath().resolve(filename);

        Files.write(filePath, images.get(i).getBytes());
      }
    } catch (IOException e) {
      throw new ImageIOException();
    }
  }

  /**
   * 이미지 url세팅
   */
  private String setImageUrl() {
    try {
      List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

      // 폴더 내의 모든 파일 경로를 필터링하여 이미지 파일 경로만 수집
      List<String> imagePaths = Files.walk(getImgPath())
          .filter(Files::isRegularFile)
          .map(Path::toString)
          .filter(x -> {
            String fileExtension = getFileExtension(x);
            return imageExtensions.contains(fileExtension);
          })
          .map(x -> serverUrl + x.replace("\\", "/"))
          .collect(Collectors.toList());

      // 이미지 파일 경로를 콤마로 구분된 문자열로 변환
      return String.join(",", imagePaths);
    } catch (IOException e) {
      throw new ImageIOException();
    } catch (SecurityException e) {
      throw new ImageSecurityException();
    } catch (InvalidPathException e) {
      throw new ImageInvalidPathException();
    } catch (Exception e) {
      throw new ImageException();
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
        throw new ImageIOException();
      }
    }
  }

  /**
   * 페이지 갯수 및 페이지 번호 설정
   */
  private static PageRequest getPageable(int page) {
    return PageRequest.of(page, 10);
  }

  private  Path getImgPath() {
    return Paths.get("images/auction/" + getLoginUserId() + "/");
  }

  private Long getLoginUserId() {
    String email = getLoginUserEmail();
    if (email != null) {
      return memberRepository.findByEmail(email)
          .orElseThrow(NotFoundMemberException::new)
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


}
