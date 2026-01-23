package com.back.market.adapter.in;

import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.Role;
import com.back.market.mapper.MarketProductMapper;
import com.back.market.mapper.MarketUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * 더미 데이터 생성을 위한 클래스(MarketUser, MarketProduct, Bidding, Order)
 * CommandLineRunner를 통해 스프링 application이 시작될 때 run() 메소드도 실행하게끔 구현
 */
@Component
@RequiredArgsConstructor
public class MarketDataInit implements CommandLineRunner {

    // 주입
    private final MarketUserRepository marketUserRepository;
    private final MarketProductRepository marketProductRepository;
    private final MarketUserMapper marketUserMapper;
    private final MarketProductMapper marketProductMapper;
    private final JdbcTemplate jdbcTemplate; //SQL 직접 실행을 위해 추가

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
        MarketUser seller = marketUserMapper.toEntity(
                1L,
                Role.USER,
                "나이키매니아",
                "seller@resello.com",
                "서울시 강남구 역삼동",
                "010-1234-5678",
                "https://dummyimage.com/100x100/000/fff&text=Seller"
        );

        MarketUser savedSeller = marketUserRepository.save(seller);

        // 유저 2: 구매자 역할 (ID: 2L)
        MarketUser buyer = marketUserMapper.toEntity(
                2L,
                Role.USER,
                "신발덕후",
                "buyer@resello.com",
                "경기도 성남시 분당구",
                "010-9876-5432",
                null
        );
        MarketUser savedBuyer = marketUserRepository.save(buyer);

        // 2. MarketProduct 생성 (ID 수동 지정)
        // 상품 1: 나이키 조던 1 - 270 사이즈
        MarketProduct product1 = marketProductMapper.toEntity(
                100L,
                "Nike",
                "Jordan 1 Retro High OG Chicago",
                "JD-101-CHI",
                "270",
                209000L, // Long 타입으로 편하게 입력
                "Sneakers",
                "https://dummyimage.com/600x400/000/fff&text=Jordan1"
        );
        MarketProduct savedProduct = marketProductRepository.saveAndFlush(product1);

        // 상품 2: 나이키 조던 1 - 280 사이즈 (같은 모델, 다른 사이즈)
        MarketProduct product2 = marketProductMapper.toEntity(
                101L,
                "Nike",
                "Jordan 1 Retro High OG Chicago",
                "JD-101-CHI",
                "280",
                209000L,
                "Sneakers",
                "https://dummyimage.com/600x400/000/fff&text=Jordan1"
        );
        marketProductRepository.saveAndFlush(product2);

        // 상품 3: 아디다스 이지 부스트 - 260 사이즈
        MarketProduct product3 = marketProductMapper.toEntity(
                200L,
                "Adidas",
                "Yeezy Boost 350 V2 Zebra",
                "CP9654",
                "260",
                289000L,
                "Sneakers",
                "https://dummyimage.com/600x400/000/fff&text=Yeezy"
        );
        marketProductRepository.saveAndFlush(product3);

        // --------------------------------------------------------
        // 3. 입찰 및 주문 데이터 생성 (JDBC Template - SQL 직접 실행)
        // --------------------------------------------------------
        // JPA Auditing(자동 날짜 설정)을 우회하고 특정 과거/미래 날짜를 넣기 위해 SQL을 직접 실행합니다.

        Long sId = savedSeller.getId();
        Long bId = savedBuyer.getId();
        Long pId = savedProduct.getId();

        System.out.println(">> 정산 테스트용 주문 데이터 생성 중...");

        // [Case 0] 초기 수동 생성 예제 (1월 15일)
        createTestOrder(sId, bId, pId, 209000, "2026-01-15 10:00:00", "2026-01-15 12:00:00");

        // [Case 1] 1월 5일 (정상)
        createTestOrder(sId, bId, pId, 50000, "2026-01-05 10:00:00", "2026-01-05 12:00:00");

        // [Case 2] 1월 10일 (정상)
        createTestOrder(sId, bId, pId, 60000, "2026-01-10 10:00:00", "2026-01-10 12:00:00");

        // [Case 3] 1월 20일 (정상)
        createTestOrder(sId, bId, pId, 70000, "2026-01-20 10:00:00", "2026-01-20 12:00:00");

        // [Case 4] 1월 25일 (정상)
        createTestOrder(sId, bId, pId, 80000, "2026-01-25 10:00:00", "2026-01-25 12:00:00");

        // [Case 5] 1월 31일 말일 (정상)
        createTestOrder(sId, bId, pId, 90000, "2026-01-31 23:50:00", "2026-01-31 23:59:59");

        // [Case 6] 12월 31일 (제외 대상 - 과거)
        createTestOrder(sId, bId, pId, 30000, "2025-12-31 10:00:00", "2025-12-31 23:59:59");

        // [Case 7] 2월 1일 (제외 대상 - 미래)
        createTestOrder(sId, bId, pId, 40000, "2026-02-01 00:00:01", "2026-02-01 00:00:01");

        System.out.println("======= [Market] 초기 데이터 생성 완료 (User: 2건, Product: 3건, Order: 8건, Bidding: 16건) =======");
    }

    /**
     * 입찰(Buy/Sell) 생성 후 주문(Order)까지 한 번에 생성하는 헬퍼 메서드
     */
    private void createTestOrder(Long sellerId, Long buyerId, Long productId, long price, String reqDate, String modDate) {
        // 1. 판매 입찰 생성
        String sqlSell = "INSERT INTO biddings (user_id, product_id, price, position, status, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, 'SELL', 'PROCESS', NOW(), NOW())";
        jdbcTemplate.update(sqlSell, sellerId, productId, price);

        // 2. 구매 입찰 생성
        String sqlBuy = "INSERT INTO biddings (user_id, product_id, price, position, status, created_at, last_modified_at) " +
                "VALUES (?, ?, ?, 'BUY', 'PROCESS', NOW(), NOW())";
        jdbcTemplate.update(sqlBuy, buyerId, productId, price);

        // 3. 주문 생성 (방금 만든 입찰 ID를 서브쿼리로 찾아서 연결)
        // last_modified_at을 인자로 받은 날짜(modDate)로 강제 주입하는 것이 핵심
        String sqlOrder = "INSERT INTO orders (buy_bidding_id, sell_bidding_id, price, address, order_status, request_payment_date, created_at, last_modified_at) " +
                "VALUES (" +
                "(SELECT id FROM biddings WHERE user_id = ? AND position = 'BUY' AND price = ? ORDER BY id DESC LIMIT 1), " +
                "(SELECT id FROM biddings WHERE user_id = ? AND position = 'SELL' AND price = ? ORDER BY id DESC LIMIT 1), " +
                "?, '서울시 테스트구', 'COMPLETED', " +
                "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), " + // request_payment_date
                "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), " + // created_at
                "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS')" +   // last_modified_at (정산 기준일)
                ")";

        jdbcTemplate.update(sqlOrder,
                buyerId, price, // buy bidding 찾기용
                sellerId, price, // sell bidding 찾기용
                price,
                reqDate, reqDate, modDate); // 날짜들
    }
}
