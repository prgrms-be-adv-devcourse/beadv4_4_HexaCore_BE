package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import com.back.market.dto.response.ProductSizePriceResponseDto;
import com.back.market.mapper.MarketProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 즉시구매가, 즉시판매가 조회
 */
@Service
@RequiredArgsConstructor
public class GetInstantPriceUseCase {

    private final MarketSupport marketSupport;
    private final MarketProductMapper marketProductMapper;

    @Transactional(readOnly = true)
    public List<ProductSizePriceResponseDto> getAllSizePrices(Long productInfoId) {
        List<MarketProduct> productList = marketSupport.findAllByProductInfoId(productInfoId);
        return productList.stream()
                .map(product -> {
                    BigDecimal buyPrice = marketSupport.findInstantBuyPrice(product.getId(), BiddingPosition.SELL, BiddingStatus.PROCESS).map(Bidding::getPrice).orElse(null);

                    BigDecimal sellPrice = marketSupport.findInstantSellPrice(product.getId(), BiddingPosition.BUY, BiddingStatus.PROCESS).map(Bidding::getPrice).orElse(null);
                    return marketProductMapper.toSizePriceDto(product, buyPrice, sellPrice);
                }).toList();
    }

    /**
     * 즉시 구매가 조회(단건 조회)
     * @param productId 조회할 상품 ID
     * @return InstantBuyPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantBuyPriceResponseDto getBuyNowPrice(Long productId) {
        // 상품 존재 여부 검증
        verifyProductExists(productId);
        
        BigDecimal price = marketSupport.findInstantBuyPrice(
                productId,
                BiddingPosition.SELL,
                BiddingStatus.PROCESS
        ).map(Bidding::getPrice).orElse(null);

        return InstantBuyPriceResponseDto.of(productId, price);
    }

    /**
     * 즉시 판매가 조회(단건 조회)
     * @param productId 조회할 상품 ID
     * @return InstantSellPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantSellPriceResponseDto getSellNowPrice(Long productId) {
        //상품 존재 여부 검증
        verifyProductExists(productId);
        BigDecimal price = marketSupport.findInstantSellPrice(
                productId,
                BiddingPosition.BUY,
                BiddingStatus.PROCESS
        ).map(Bidding::getPrice).orElse(null);

        return InstantSellPriceResponseDto.of(productId, price);
    }

    private void verifyProductExists(Long productId) {
        if (!marketSupport.existsByMarketProduct(productId)) {
            throw new BadRequestException(FailureCode.PRODUCT_NOT_FOUND);
        }
    }
}