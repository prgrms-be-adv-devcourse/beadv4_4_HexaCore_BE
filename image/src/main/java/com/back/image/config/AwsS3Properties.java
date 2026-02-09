package com.back.image.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cloud.aws") // yaml의 cloud.aws 경로와 매핑
public class AwsS3Properties {

    private Credentials credentials;
    private Region region;
    private S3 s3;

    @Getter
    @Setter
    public static class Credentials {
        private String accessKey; // cloud.aws.credential.access-key 와 매핑 (kebab-case 자동 변환)
        private String secretKey;
    }

    @Getter
    @Setter
    public static class Region {
        private String staticRegion; // yaml에서는 'static'이지만 자바 예약어라 변수명 다르게 함

        // yaml의 'static' 키를 이 메서드로 매핑
        public void setStatic(String staticRegion) {
            this.staticRegion = staticRegion;
        }
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
    }
}
