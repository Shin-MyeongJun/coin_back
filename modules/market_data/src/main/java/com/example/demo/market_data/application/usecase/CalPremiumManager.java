package com.example.demo.market_data.application.usecase;

import com.example.demo.market_data.application.port.in.CalPremiumUseCase;
import com.example.demo.market_data.application.port.in.PublishPriceDataUseCase;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.application.port.out.PremiumMarketCodeRegistryPort;
import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.*;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CalPremiumManager implements CalPremiumUseCase {


    //buffers
    private final PriceValueBuffer<Premium>  buffer;
    private final PriceValueBuffer<PremiumDetail> detailBuffer;

    //cache
    private final GetCacheDataPort<Long,Tick> tickGetter;
    private final GetCacheDataPort<Long, MarketCodeSnapShotVal> codeGetter;
    private final GetCacheDataPort<Long, ExchangeSnapShotVal> exchangeGetter;
    private final GetCacheDataPort<FxKey, Fx> fxGetter;


    //publisher kafka
    private final PublishPriceDataUseCase<Premium> premiumPublisher;
    private final PublishPriceDataUseCase<PremiumDetail> premiumDetailPublisher;

    //mapList
    private final PremiumMarketCodeRegistryPort marketCodeList;





    public void cal(Long id){

        Optional<MarketCodeSnapShotVal> snap = codeGetter.get(id);
        if(snap.isEmpty()){return;}
        String base = snap.get().baseAsset();
        marketCodeList.put(base,id);
        Optional<Tick> baseTick =tickGetter.get(id);
        if(baseTick.isEmpty())return;

        Set<Long> codes = new HashSet<>(marketCodeList.get(base));
        for (Long code : codes) {
              if(code.equals(id))continue;
              Optional<Tick> tick = tickGetter.get(code);
              if(tick.isEmpty())continue;
              handling(baseTick.get(),tick.get());

        }
    }

    private void handling(Tick a ,Tick b){
        Optional<MarketCodeSnapShotVal> mcAOpt = codeGetter.get(a.marketCodeId());
        if (mcAOpt.isEmpty()) return;
        MarketCodeSnapShotVal mcA = mcAOpt.get();

        Optional<MarketCodeSnapShotVal> mcBOpt = codeGetter.get(b.marketCodeId());
        if (mcBOpt.isEmpty()) return;
        MarketCodeSnapShotVal mcB = mcBOpt.get();

        Optional<ExchangeSnapShotVal> exAOpt = exchangeGetter.get(mcA.exchangeId());
        if (exAOpt.isEmpty()) return;
        ExchangeSnapShotVal exA = exAOpt.get();

        Optional<ExchangeSnapShotVal> exBOpt = exchangeGetter.get(mcB.exchangeId());
        if (exBOpt.isEmpty()) return;
        ExchangeSnapShotVal exB = exBOpt.get();

        String symbol =  mcA.baseAsset();
        Long baseId =    mcA.exchangeId();
        Long compareId = mcB.exchangeId();

        String quoteA =normalize(exA.quote());
        String quoteB =normalize(exB.quote());

        if (filtering(quoteA, quoteB)) {
            Optional<Fx> fxOpt = fxGetter.get(new FxKey(quoteA, quoteB));
            if (fxOpt.isEmpty()) { return; }
            Fx fx = fxOpt.get();

            // fx.val(): 1 fx.base == val fx.compare (예: USD/KRW → 1 USD = 1325 KRW).
            // 두 호가를 fx.compare 통화로 환산한다: base 통화 leg × val, compare 통화 leg × 1.
            BigDecimal fxA = toCompareCurrency(quoteA, fx);
            BigDecimal fxB = toCompareCurrency(quoteB, fx);
            if (fxA == null || fxB == null) { return; }

            BigDecimal bidA = a.bid();
            BigDecimal askA = a.ask();

            BigDecimal bidB = b.bid();
            BigDecimal askB = b.ask();

            Long currentTimeMillis = System.currentTimeMillis();

            Premium premium = makePremium(symbol, baseId, compareId,
                    bidA.multiply(fxA), askA.multiply(fxA),
                    bidB.multiply(fxB), askB.multiply(fxB), currentTimeMillis);
            PremiumDetail premiumDetail = makePremiumDetail(symbol, baseId, compareId,
                    bidA, askA, fxA, bidB, askB, fxB, currentTimeMillis);

            buffer.add(premium);
            detailBuffer.add(premiumDetail);
            premiumPublisher.process(premium);
            premiumDetailPublisher.process(premiumDetail);
        }

    }




    private String normalize(String input) {
        if (input == null) return null;

        String upper = input.toUpperCase();


        if (upper.startsWith("USD") || upper.endsWith("USD")) {
            return "USD";
        }

        if (upper.startsWith("KRW") || upper.endsWith("KRW")) {
            return "KRW";
        }

        if (upper.startsWith("JPY") || upper.endsWith("JPY")) {
            return "JPY";
        }

        return null;
    }


    //나중에는 제거할수 있도록 모든페어 대응 힘들어서 일단 막기
    private boolean filtering(String quoteA , String quoteB){
        if(quoteA  == null || quoteB == null) return false;
        return !quoteA.equals(quoteB);
    }

    /**
     * 호가 통화를 FX 쌍의 compare 통화로 환산하는 배수.
     *  - quote == fx.base    → val  (base → compare)
     *  - quote == fx.compare → 1    (compare → compare)
     *  - 그 외               → null (환산 불가, 호출부에서 스킵)
     */
    private BigDecimal toCompareCurrency(String quote, Fx fx) {
        if (quote.equals(fx.base()))    return fx.val();
        if (quote.equals(fx.compare())) return BigDecimal.ONE;
        return null;
    }



    private  Premium makePremium(String symbol, Long baseId,Long compareId,
                                 BigDecimal bidA,BigDecimal askA,
                                 BigDecimal bidB, BigDecimal askB,Long currentTimeMillis){




        BigDecimal bid = bidB.divide(askA,8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        BigDecimal ask = askB.divide(bidA,8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        return new Premium(
                symbol,
                baseId,
                compareId,
                bid,
                ask,
                currentTimeMillis

        );
    }

    private PremiumDetail makePremiumDetail(String symbol, Long baseId,Long compareId,
                                            BigDecimal bidA,BigDecimal askA,BigDecimal fxA,
                                            BigDecimal bidB, BigDecimal askB,BigDecimal fxB,Long currentTimeMillis){

        return new PremiumDetail(
                symbol,
                baseId,
                compareId,
                bidA,
                askA,
                fxA,
                bidB,
                askB,
                fxB,
                currentTimeMillis
        );

    }

}
