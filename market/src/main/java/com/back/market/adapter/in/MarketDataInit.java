package com.back.market.adapter.in;

import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 더미 데이터 생성을 위한 클래스(MarketUser, MarketProduct)
 * CommandLineRunner를 통해 스프링 application이 시작될 때 run() 메소드도 실행하게끔 구현
 */
@Component
@RequiredArgsConstructor
public class MarketDataInit implements CommandLineRunner {

    // 주입
    private final MarketUserRepository marketUserRepository;
    private final MarketProductRepository marketProductRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 중복 실행 방지: 데이터가 존재하면 건너뜀
        if(marketUserRepository.count() > 0) {
            return;
        }
        System.out.println("======= [Market] 초기 테스트 데이터 생성 시작 =======");

        // 1. MarketUser 생성 (ID 수동 지정)
        // 유저 1: 판매자 역할 (ID: 1L)
        MarketUser seller = MarketUser.builder()
                .id(1L) // 다른 모듈(User)의 ID를 그대로 쓴다고 가정
                .email("seller@resello.com")
                .nickname("나이키매니아")
                .role(Role.USER)
                .address("서울시 강남구 역삼동")
                .phone("010-1234-5678")
                .build();
        marketUserRepository.save(seller);

        // 유저 2: 구매자 역할 (ID: 2L)
        MarketUser buyer = MarketUser.builder()
                .id(2L)
                .email("buyer@resello.com")
                .nickname("신발덕후")
                .role(Role.USER)
                .address("경기도 성남시 분당구")
                .phone("010-9876-5432")
                .build();
        marketUserRepository.save(buyer);

        // 2. MarketProduct 생성 (ID 수동 지정)
        // 상품 1: 나이키 조던 1 - 270 사이즈
        MarketProduct product1 = MarketProduct.builder()
                .id(100L) // 다른 모듈(Product)의 ID
                .brandName("Nike")
                .name("Jordan 1 Retro High OG Chicago")
                .productNumber("JD-101-CHI")
                .size("270")
                .originalPrice(209000L)
                .categoryName("Sneakers")
                .thumbnailImage("https://dummyimage.com/600x400/000/fff&text=Jordan1") // 임시 이미지
                .build();
        marketProductRepository.save(product1);

        // 상품 2: 나이키 조던 1 - 280 사이즈 (같은 모델, 다른 사이즈)
        MarketProduct product2 = MarketProduct.builder()
                .id(101L)
                .brandName("Nike")
                .name("Jordan 1 Retro High OG Chicago")
                .productNumber("JD-101-CHI")
                .size("280")
                .originalPrice(209000L)
                .categoryName("Sneakers")
                .thumbnailImage("https://dummyimage.com/600x400/000/fff&text=Jordan1")
                .build();
        marketProductRepository.save(product2);

        // 상품 3: 아디다스 이지 부스트 - 260 사이즈
        MarketProduct product3 = MarketProduct.builder()
                .id(200L)
                .brandName("Adidas")
                .name("Yeezy Boost 350 V2 Zebra")
                .productNumber("CP9654")
                .size("260")
                .originalPrice(289000L)
                .categoryName("Sneakers")
                .thumbnailImage("https://dummyimage.com/600x400/000/fff&text=Yeezy")
                .build();
        marketProductRepository.save(product3);

        System.out.println("======= [Market] 초기 데이터 생성 완료 (User: 2건, Product: 3건) =======");
    }
}
