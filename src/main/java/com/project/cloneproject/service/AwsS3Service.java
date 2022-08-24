package com.project.cloneproject.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.cloneproject.controller.dto.request.PostRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket.url}")
    private String defaultEndpointUrl;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public static File getImageFromBase64(String base64String, String fileName) {
        String[] strings = base64String.split(",");

        String extension;
        switch (strings[0]) {
            case "data:image/jpeg;base64":
                extension = ".jpeg";
                break;
            case "data:image/png;base64":
                extension = ".png";
                break;
            default:
                extension = ".jpg";
                break;
        }

        byte[] data = Base64.getDecoder().decode(strings[1]);
        File file = new File(fileName + extension);

        try(OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

    @Transactional
    public String getSavedS3ImageUrl(PostRequestDto postRequestDto) {

        if(postRequestDto.getImageUrl() == null ||
                Objects.equals(postRequestDto.getImageUrl(), "")) return null;

        String fileName = UUID.randomUUID().toString();
        String fileUrl;

        try {
            File file = getImageFromBase64(postRequestDto.getImageUrl(),fileName);
            fileUrl = defaultEndpointUrl + "/" + fileName;

            uploadFileToS3Bucket(fileName, file);
            file.delete();
            log.info("File uploaded to S3 successfully");
        } catch (Exception e) {
            log.error("Error while uploading the file to S3" + e);
            throw e;
        }
        return fileUrl;
    }

    private void uploadFileToS3Bucket(String fileName, File file) {
        amazonS3.putObject(new PutObjectRequest(bucket, fileName, file).
                withCannedAcl(CannedAccessControlList.PublicRead));
    }

    @Transactional
    public void deleteImage(String deleteUrl) {
        String deleteFileName = deleteUrl.substring(defaultEndpointUrl.length() + 1);
        System.out.println(deleteFileName);
        try {
            amazonS3.deleteObject(bucket,deleteFileName);
//            amazonS3.deleteObject(new DeleteObjectRequest(bucket,deleteFileName));
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

}
