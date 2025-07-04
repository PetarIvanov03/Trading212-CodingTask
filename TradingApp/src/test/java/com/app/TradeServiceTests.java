package com.app;

import com.app.dto.TradeRequestDTO;
import com.app.model.Holding;
import com.app.model.Trade;
import com.app.service.TradeService;
import com.app.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TradeServiceTests {

    @Autowired
    private TradeService tradeService;
    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void buyAndSellUpdatesStateAndProfit() {
        tradeService.reset();

        TradeRequestDTO buy = new TradeRequestDTO();
        buy.setSymbol("BTC/USDT");
        buy.setAmount(1);
        buy.setType("buy");
        tradeService.processTrade(buy, 100.0);

        assertEquals(9900.0, tradeRepository.getBalance(), 0.01);
        Holding h = tradeRepository.getHolding("BTC/USDT");
        assertNotNull(h);
        assertEquals(1.0, h.getQuantity(), 0.01);
        assertEquals(100.0, h.getAvgPrice(), 0.01);

        TradeRequestDTO sell = new TradeRequestDTO();
        sell.setSymbol("BTC/USDT");
        sell.setAmount(1);
        sell.setType("sell");
        tradeService.processTrade(sell, 120.0);

        assertEquals(10020.0, tradeRepository.getBalance(), 0.01);
        assertNull(tradeRepository.getHolding("BTC/USDT"));

        List<Trade> history = tradeRepository.getAllTrades();
        Trade last = history.get(0);
        assertEquals(20.0, last.getProfitLoss(), 0.01);
    }
}
