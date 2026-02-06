package com.back.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP 요청에서 클라이언트 IP 주소를 추출하는 유틸리티 클래스
 */
@Slf4j
public class IpAddressExtractor {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
    };

    private static final String UNKNOWN = "unknown";

    public static String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            log.warn("[IP 추출 실패] HttpServletRequest가 null입니다");
            return null;
        }

        try {
            for (String header : IP_HEADER_CANDIDATES) {
                String ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    if (ip.contains(",")) {
                        ip = ip.split(",")[0].trim();
                    }
                    log.debug("[IP 추출 성공] Header: {}, IP: {}", header, ip);
                    return ip;
                }
            }

            String remoteAddr = request.getRemoteAddr();
            log.debug("[IP 추출] RemoteAddr 사용: {}", remoteAddr);
            return remoteAddr;

        } catch (Exception e) {
            log.error("[IP 추출 중 오류 발생]", e);
            return null;
        }
    }

    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !ip.isBlank() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    private IpAddressExtractor() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다");
    }
}
