package com.amo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amo.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
public class AwsS3Config {
	@Autowired
	private AwsProperties awsProperties;

	public AwsS3Config(AwsProperties awsProperties) {
		this.awsProperties = awsProperties;
	}

	@Bean
	public S3Client s3Client() throws Exception {

		SecretsManagerClient secretsClient = SecretsManagerClient.builder().region(Region.of(awsProperties.getRegion()))
				.build();

		GetSecretValueResponse secretValue = secretsClient
				.getSecretValue(GetSecretValueRequest.builder().secretId(awsProperties.getSecretName()).build());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(secretValue.secretString());

		String accessKey = jsonNode.get(Constants.aws_key_name).asText();
		String secretKey = jsonNode.get(Constants.aws_key_secret).asText();

		return S3Client.builder().region(Region.of(awsProperties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
	}
}
