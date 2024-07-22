package com.fantion.backend.common.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class S3Uploader {

  private final AmazonS3 amazonS3;
  private final String bucket;

  public S3Uploader(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}") String bucket) {
    this.amazonS3 = amazonS3;
    this.bucket = bucket;
  }

  public String upload(MultipartFile file, String dirName) throws IOException {
    String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
    amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), null)
        .withCannedAcl(CannedAccessControlList.PublicRead));
    return amazonS3.getUrl(bucket, fileName).toString();
  }
}
