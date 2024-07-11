package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.AuctionDto;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.dto.SearchDto;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.exception.impl.*;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.type.SearchType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.jca.endpoint.GenericMessageEndpointFactory.InternalResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final MemberRepository memberRepository;
  private Path imgPath = Paths.get("images/auction/" + getUserId() + "/");
  private String serverUrl = "https://localhost:8080/auction/";

  /**
   * 경매 생성
   */
  @Override
  @Transactional
  public Response createAuction(@Valid Request request, List<MultipartFile> auctionImage) {
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
            .orElseThrow(()-> new RuntimeException());

    return toResponse(auction);
  }

  /**
   * 경매 수정
   */

  @Override
  @Transactional
  public Response updateAuction(
      @Valid Request request,
      List<MultipartFile> auctionImage,
      Long auctionId) {
    Auction auction = updateValue(request, auctionId);

    emptyDirectory(imgPath);
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

    emptyDirectory(imgPath);

    return true;
  }

  /**
   * 경매 리스트
   */
  @Override
  public Page<Response> getList(int page) {
    Pageable pageable = getPageable(page);
    return covertToResponseList(auctionRepository.findAll(pageable));
  }

  /**
   * 경매 검색
   */
  @Override
  public Page<Response> getSearchList(@Valid SearchDto searchDto) {
    Pageable pageable = getPageable(searchDto.getPage());
    Page<Auction> auctionPage = null;

    try {
      if (searchDto.getSearchOption() == SearchType.TITLE) {
        auctionPage = auctionRepository.findByTitleContaining(searchDto.getKeyword(), pageable);
      }
    } catch (Exception e) {
      throw new AuctionHttpMessageNotReadableException();
    }

    return covertToResponseList(auctionPage);
  }

  /**
   * 경매데이터 수정 member쪽은 임시 데이터임
   */
  private Auction updateValue(Request request, Long auctionId) {
    Auction auction = auctionRepository.findById(auctionId)
        .orElseThrow(AuctionNotFoundException::new);

    auction.setMember(memberRepository.findById(1L).orElseThrow(NotFoundMemberException::new));
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
   * request -> auction member쪽은 임시 데이터임
   */
  private Auction toAuction(Request request) {
    return Auction.builder()
        .member(memberRepository.findById(1L).orElseThrow(NotFoundMemberException::new))
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

  /**
   * auction -> response userNickname쪽은 임시 데이터임
   */
  private Response toResponse(Auction auction) {
    return Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(memberRepository.findById(1L).orElseThrow(
            NotFoundMemberException::new).getNickname())
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
  private Page<Response> covertToResponseList(Page<Auction> auctionList) {
    List<Response> responseList = auctionList.stream().map(this::toResponse)
        .collect(Collectors.toList());

    return new PageImpl<>(responseList, auctionList.getPageable(), auctionList.getTotalElements());
  }

  /**
   * 이미지 저장
   */
  public void saveImages(List<MultipartFile> images) {
    try {
      if (!Files.exists(imgPath)) {
        Files.createDirectories(imgPath);
      }

      for (int i = 0; i < images.size(); i++) {
        String filename = (i + 1) + ".jpg";
        Path filePath = imgPath.resolve(filename);

        Files.write(filePath, images.get(i).getBytes());
      }
    } catch (IOException e) {
      throw new ImageIOException();
    }
  }

  /**
   * userId를 가져오기
   */
  private Long getUserId() {
    return 1L;
  }

  /**
   * 이미지 url세팅
   */
  private String setImageUrl() {
    try {
      List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

      // 폴더 내의 모든 파일 경로를 필터링하여 이미지 파일 경로만 수집
      List<String> imagePaths = Files.walk(imgPath)
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
}
