package com.fantion.backend.auction.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auction")
public class AuctionController {

//<<<<<<< Updated upstream
}
  private final AuctionService auctionService;
  private final MemberRepository memberRepository;

  @PostMapping("")
  public ResponseEntity<?> createAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage)
      throws IOException {
    memberRepository.save(
        new Member(1L, "email",
            "password", "nickname",
            true, true, true,
            "address", 0, 0, 0,
            MemberStatus.ACTIVE, null, LocalDateTime.now()));

    return ResponseEntity.ok(auctionService.createAuction(request, auctionImage));
  }

  @PutMapping("/{auctionId}")
  public ResponseEntity<?> updateAuction(
      @Valid @RequestPart("request") AuctionDto.Request request,
      @RequestPart("auctionImage") List<MultipartFile> auctionImage,
      @PathVariable("auctionId") Long auctionId) throws IOException {
    return ResponseEntity.ok(auctionService.updateAuction(request, auctionImage, auctionId));
  }

  @DeleteMapping("/{auctionId}")
  public ResponseEntity<?> deleteAuction(
      @PathVariable("auctionId") Long auctionId) throws IOException {
    return ResponseEntity.ok(auctionService.deleteAuction(auctionId));
  }

  @GetMapping("/images/auction/{userId}/{filename:.+}")
  public ResponseEntity<Resource> getImage(
      @PathVariable("userId") String userId,
      @PathVariable("filename") String filename) {
    try {
      Path imagePath = basePath.resolve(userId).resolve(filename);
      Resource imageResource = new UrlResource(imagePath.toUri());

      if (imageResource.exists() || imageResource.isReadable()) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "image/jpeg");
        return new ResponseEntity<>(imageResource, headers, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/bid")
  private BidDto.Response createBid(@RequestBody BidDto.Request request) {
    log.info("[Controller] createBid");
    return auctionService.createBid(request);
  }
}
>>>>>>> Stashed changes
