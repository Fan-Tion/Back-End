package com.fantion.backend.auction.service.impl;

import com.fantion.backend.auction.dto.AuctionDto;
import static org.springframework.util.FileSystemUtils.deleteRecursively;
import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.exception.impl.ImageException;
import com.fantion.backend.exception.impl.ImageIOException;
import com.fantion.backend.exception.impl.ImageInvalidPathException;
import com.fantion.backend.exception.impl.ImageSecurityException;
import com.fantion.backend.exception.impl.NotFoundMemberException;
import com.fantion.backend.member.repository.MemberRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

  private final AuctionRepository auctionRepository;
  private final MemberRepository memberRepository;
  private final BidRepository bidRepository;
  private Path imgPath = Paths.get("images/auction/" + getUserId() + "/");
  private String serverUrl = "https://localhost:8080/auction/";

  @Override
  @Transactional
  public Response createAuction(Request request, List<MultipartFile> auctionImage)
      throws IOException {
    //에러처리할 걸 찾기

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

  @Override
  @Transactional
  public Response updateAuction(
      Request request,
      List<MultipartFile> auctionImage,
      Long auctionId)
      throws IOException {
    //에러처리할 걸 찾기

    emptyDirectory(imgPath);

    saveImages(auctionImage);

    return toResponse(updateValue(request, auctionId));
  }

  @Override
  @Transactional
  public boolean deleteAuction(Long auctionId) throws IOException {
    //에러처리할 걸 찾기
    auctionRepository.deleteById(auctionId);

    emptyDirectory(imgPath);

    return true;
  }

  /**
   * member쪽은 임시 데이터임
   */
  private Auction updateValue(Request request, Long auctionId) {
    Auction auction = Auction.builder()
        .auctionId(auctionId)
        .member(memberRepository.findById(1L).orElseThrow(NotFoundMemberException::new))
        .title(request.getTitle())
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

    return auctionRepository.save(auction);
  }

  /**
   * member쪽은 임시 데이터임
   */


  private Auction toAuction(Request request) {
    return Auction.builder()
        .member(memberRepository.findById(1L).orElseThrow(NotFoundMemberException::new))
        .title(request.getTitle())
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
   * userNickname쪽은 임시 데이터임
   */
  private Response toResponse(Auction auction) {
    return Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(memberRepository.findById(1L).orElseThrow(
            NotFoundMemberException::new).getNickname())
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

  public void saveImages(List<MultipartFile> images) throws IOException {
    if (!Files.exists(imgPath)) {
      Files.createDirectories(imgPath);
    }

    for (int i = 0; i < images.size(); i++) {
      String filename = (i + 1) + ".jpg";
      Path filePath = imgPath.resolve(filename);
      Files.write(filePath, images.get(i).getBytes());
    }
  }

  /**
   * userId를 임시 지정
   */
  private Long getUserId() {
    return 1L;
  }

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
          .map( x -> serverUrl + x.replace("\\", "/"))
          .collect(Collectors.toList());

      // 이미지 파일 경로를 콤마로 구분된 문자열로 변환
      return String.join(",", imagePaths);
    } catch (IOException e ) {
      throw new ImageIOException();
    } catch (SecurityException e) {
      throw new ImageSecurityException();
    } catch (InvalidPathException e) {
      throw new ImageInvalidPathException();
    } catch (Exception e) {
      throw new ImageException();
    }
  }

  private static String getFileExtension(String filePath) {
    String fileName = new File(filePath).getName();
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1).toLowerCase();
  }

  public void emptyDirectory(Path directory) throws IOException {
    if (Files.isDirectory(directory)) {
      try (DirectoryStream<Path> stream
          = Files.newDirectoryStream(directory)) {
        for (Path entry : stream) {
          deleteRecursively(entry);
        }
      }
    }
  }
}
