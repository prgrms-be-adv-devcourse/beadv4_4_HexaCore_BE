package com.back.product.app;

import com.back.detector.app.DetectorFacade;
import com.back.detector.exception.CrawlingDetectedException;
import com.back.product.adapter.out.ProductDocumentRepository;
import com.back.product.dto.request.ProductSearchRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductFacade 크롤링 감지 통합 테스트")
class ProductFacadeCrawlingTest {

    @Autowired
    private ProductFacade productFacade;

    @MockitoBean
    private DetectorFacade detectorFacade;

    @MockitoBean
    private ProductDocumentRepository productDocumentRepository;

    @Nested
    @DisplayName("getProductDetail 메서드")
    class GetProductDetailTest {

        @Test
        @DisplayName("크롤링 감지 AOP가 호출된다")
        void getProductDetail_CallsCrawlingDetection() {
            // given
            Long productInfoId = 1L;
            setRequestContext("192.168.1.100");

            // when
            try {
                productFacade.getProductDetail(productInfoId);
            } catch (Exception e) {
                // ProductInfo 관련 예외 무시
            }

            // then
            verify(detectorFacade).detectCrawling(any());
        }

        @Test
        @DisplayName("크롤링 감지되면 예외가 발생한다")
        void getProductDetail_ThrowsException_WhenCrawlingDetected() {
            // given
            Long productInfoId = 1L;
            setRequestContext("192.168.1.100");
            doThrow(new CrawlingDetectedException())
                    .when(detectorFacade).detectCrawling(any());

            // when & then
            assertThatThrownBy(() -> productFacade.getProductDetail(productInfoId))
                    .isInstanceOf(CrawlingDetectedException.class);
        }
    }

    @Nested
    @DisplayName("findProductPage 메서드")
    class FindProductPageTest {

        @Test
        @DisplayName("크롤링 감지 AOP가 호출된다")
        void findProductPage_CallsCrawlingDetection() {
            // given
            ProductSearchRequestDto request = ProductSearchRequestDto.builder()
                    .keyword("")
                    .build();
            setRequestContext("192.168.1.100");

            // when
            try {
                productFacade.findProductPage(request, 0L, 10L);
            } catch (Exception e) {
                // 검색 관련 예외 무시
            }

            // then
            verify(detectorFacade).detectCrawling(any());
        }
    }

    private void setRequestContext(String ip) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
