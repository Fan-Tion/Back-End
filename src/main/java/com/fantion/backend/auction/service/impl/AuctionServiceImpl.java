package com.fantion.backend.auction.service.impl;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

import com.fantion.backend.auction.dto.AuctionDto.Request;
import com.fantion.backend.auction.dto.AuctionDto.Response;
import com.fantion.backend.auction.entity.Auction;
import com.fantion.backend.auction.repository.AuctionRepository;
import com.fantion.backend.auction.service.AuctionService;
import com.fantion.backend.member.MemberRepository;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
  private final AuctionRepository auctionRepository;
  private final MemberRepository memberRepository;

  private Path userDir = Paths.get("images/", "auction/");
  private String serverUrl = "";

  @Override
  @Transactional
  public Response createAuction(Request request, List<MultipartFile> auctionImage) throws IOException {
    //에러처리할 걸 찾기

    saveImages(auctionImage);

    Auction auction = toAuction(request);

    auctionRepository.save(auction);

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

    emptyDirectory(userDir);

    saveImages(auctionImage);

    return toResponse(updateValue(request, auctionId));
  }

  @Override
  @Transactional
  public boolean deleteAuction(Long auctionId) throws IOException {
    //에러처리할 걸 찾기
    auctionRepository.deleteById(auctionId);

    emptyDirectory(userDir);

    return true;
  }

  /**
   * member쪽은 임시 데이터임
   * */
  private Auction updateValue(Request request, Long auctionId) {
    Auction auction = Auction.builder()
        .auctionId(auctionId)
        .member(memberRepository.findById(1L).orElseThrow(
            () -> new RuntimeException("imsi error")
        ))
        .title(request.getTitle())
        .auctionType(request.isAuctionType())
        .auctionImage(setMainImage())
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
   * */
  private Auction toAuction(Request request) {
    return Auction.builder()
        .member(memberRepository.findById(1L).orElseThrow(
            () -> new RuntimeException("imsi error")
        ))
        .title(request.getTitle())
        .auctionType(request.isAuctionType())
        .auctionImage(setMainImage())
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
   * */
  private Response toResponse(Auction auction) {
    return Response.builder()
        .title(auction.getTitle())
        .auctionUserNickname(memberRepository.findById(1L).orElseThrow(
            () -> new RuntimeException("imsi error")).getNickname()
        )
        .auctionType(auction.isAuctionType())
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
   if (!Files.exists(userDir)) {
      Files.createDirectories(userDir);
    }

    for (int i = 0; i < images.size(); i++) {
      String filename = (i + 1) + ".jpg";
      Path filePath = userDir.resolve(filename);
      Files.write(filePath, images.get(i).getBytes());
    }
  }

  /**
   * userId를 임시 지정
   * */
  private Long getUserId() {
    return 1L;
  }

  private String setMainImage() {
    return serverUrl + userDir + "1/" + getUserId() + ".jpg";
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
