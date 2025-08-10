package com.amo.service;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amo.config.AwsProperties;
import com.amo.model.ApiResponse;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class S3Service {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(S3Service.class);
	
	private final S3Client s3Client;
    private final String bucketName;

	@Autowired

	private AwsProperties awsProperties;
	private SqsClient sqsClient;
	

	@Autowired
	public S3Service(S3Client s3Client , AwsProperties awsProperties ) {
		this.s3Client = s3Client;
		this.bucketName = awsProperties.getBucket().getName();
		this.sqsClient = sqsClient;
		
		log.info("Service Constructor classed "+bucketName);
	}

	

	public ApiResponse uploadDocs(String fileName, byte[] content) {

		try {
			log.info("upload file to bucket{} bucketname " + bucketName);
			// create request object for s2 upload
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(fileName).build();
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
			//callsqs
			notifyProcessor(fileName, fileName);

			log.info("Successfully Upload the file " + fileName);
			return new ApiResponse(true, "file upload Done", fileName);

		} catch (Exception exc) {
			log.error("Error in file upload in S3 Bucket" + exc.getMessage());
			return new ApiResponse(false, "file not upload to s3", fileName);
		}

	}
	
	public void notifyProcessor(String bucket, String key) {
	    sqsClient.sendMessage(SendMessageRequest.builder()
	        .queueUrl("quequrl")
	        .messageBody("{\"bucket\":\"" + bucket + "\", \"key\":\"" + key + "\"}")
	        .build()
	    );
	}

	public List<String> listFiles() {
		try {
			ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
					.bucket(awsProperties.getBucket().getName()).build();

			ListObjectsV2Response listOfDocs = s3Client.listObjectsV2(listRequest);

			return listOfDocs.contents().stream().map(S3Object::key).collect(Collectors.toUnmodifiableList());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public byte[] downloadDocs(String fileName) {
		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

			ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(getObjectRequest);

			return response.asByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

		
	}
}
