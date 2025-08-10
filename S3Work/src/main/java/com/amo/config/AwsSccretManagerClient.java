package com.amo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amo.utils.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
public class AwsSccretManagerClient
{

    @Autowired
    private AwsProperties awsProperties; // Holds region + secretName

    public StaticCredentialsProvider getCredentialsProvider() throws Exception {
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .build();

        GetSecretValueResponse secretValue = secretsClient
                .getSecretValue(GetSecretValueRequest.builder()
                        .secretId(awsProperties.getSecretName())
                        .build());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(secretValue.secretString());

        String accessKey = jsonNode.get(Constants.aws_key_name).asText();
        String secretKey = jsonNode.get(Constants.aws_key_secret).asText();

		return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }
}