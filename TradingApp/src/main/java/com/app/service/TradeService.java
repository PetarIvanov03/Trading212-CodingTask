package com.app.service;

import com.app.dto.TradeRequestDTO;
import com.app.model.Trade;
import com.app.model.Holding;
import com.app.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeService {

    @Autowired
    private TradeRepository tradeRepo;

    public void reset() {
        tradeRepo.clearAll();
    }

    public List<Trade> getHistory() {
        return tradeRepo.getAllTrades();
    }

    public double getBalance() {
        return tradeRepo.getBalance();
    }

    public List<Holding> getHoldings() {
        return tradeRepo.getAllHoldings();
    }

    public void processTrade(TradeRequestDTO req, double price) {
        double balance = tradeRepo.getBalance();
        Holding holding = tradeRepo.getHolding(req.getSymbol());
        double totalCost = price * req.getAmount();
        double profitLoss = 0.0;

        if (req.getType().equals("buy")) {
            if (balance < totalCost) throw new RuntimeException("Insufficient balance");

            double newQty = (holding == null ? 0 : holding.getQuantity()) + req.getAmount();
            double newAvg = holding == null ? price :
                    ((holding.getAvgPrice() * holding.getQuantity()) + totalCost) / newQty;

            tradeRepo.updateBalance(balance - totalCost);
            tradeRepo.upsertHolding(req.getSymbol(), newQty, newAvg);
        } else if (req.getType().equals("sell")) {
            if (holding == null || holding.getQuantity() < req.getAmount()) throw new RuntimeException("Insufficient holdings");

            profitLoss = (price - holding.getAvgPrice()) * req.getAmount();
            double newQty = holding.getQuantity() - req.getAmount();

            tradeRepo.updateBalance(balance + totalCost);
            if (newQty > 0) {
                tradeRepo.upsertHolding(req.getSymbol(), newQty, holding.getAvgPrice());
            } else {
                tradeRepo.deleteHoldingIfZero(req.getSymbol());
            }
        }

        Trade t = new Trade();
        t.setSymbol(req.getSymbol());
        t.setAmount(req.getAmount());
        t.setPrice(price);
        t.setProfitLoss(profitLoss);
        t.setBalanceAfter(tradeRepo.getBalance());
        t.setType(req.getType());
        t.setTimestamp(LocalDateTime.now());
        tradeRepo.saveTrade(t);
    }
}
