package com.back.image.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final AwsS3Properties awsS3Properties;

    @Bean
    @Primary
    public AmazonS3Client amazonS3Client() {
        String accessKey = awsS3Properties.getCredentials().getAccessKey();
        String secretKey = awsS3Properties.getCredentials().getSecretKey();
        String region = awsS3Properties.getRegion().getStaticRegion();

        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}
