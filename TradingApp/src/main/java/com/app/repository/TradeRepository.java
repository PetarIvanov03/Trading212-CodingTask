package com.app.repository;

import com.app.model.Trade;
import com.app.model.Holding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TradeRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public void saveTrade(Trade trade) {
        jdbc.update(
                "INSERT INTO trades (symbol, amount, price, profit_loss, balance_after, type, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)",
                trade.getSymbol(), trade.getAmount(), trade.getPrice(), trade.getProfitLoss(),
                trade.getBalanceAfter(), trade.getType(), trade.getTimestamp()
        );
    }

    public List<Trade> getAllTrades() {
        return jdbc.query(
                "SELECT * FROM trades ORDER BY timestamp DESC",
                (rs, rowNum) -> {
                    Trade trade = new Trade();
                    trade.setId(rs.getInt("id"));
                    trade.setSymbol(rs.getString("symbol"));
                    trade.setAmount(rs.getDouble("amount"));
                    trade.setPrice(rs.getDouble("price"));
                    trade.setProfitLoss(rs.getDouble("profit_loss"));
                    trade.setBalanceAfter(rs.getDouble("balance_after"));
                    trade.setType(rs.getString("type"));
                    trade.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    return trade;
                }
        );
    }

    public void clearAll() {
        jdbc.update("DELETE FROM trades");
        jdbc.update("DELETE FROM holdings");
        jdbc.update("UPDATE balance SET amount = 10000 WHERE id = 1");
    }

    public double getBalance() {
        return jdbc.queryForObject("SELECT amount FROM balance WHERE id = 1", Double.class);
    }

    public void updateBalance(double amount) {
        jdbc.update("UPDATE balance SET amount = ? WHERE id = 1", amount);
    }

    public void upsertHolding(String symbol, double quantity, double avgPrice) {
        jdbc.update("""
            INSERT INTO holdings(symbol, quantity, avg_price)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), avg_price = VALUES(avg_price)
        """, symbol, quantity, avgPrice);
    }

    public Holding getHolding(String symbol) {
        List<Holding> results = jdbc.query("""
            SELECT * FROM holdings WHERE symbol = ?
        """, new Object[]{symbol}, (rs, rowNum) -> {
            Holding h = new Holding();
            h.setSymbol(rs.getString("symbol"));
            h.setQuantity(rs.getDouble("quantity"));
            h.setAvgPrice(rs.getDouble("avg_price"));
            return h;
        });
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Holding> getAllHoldings() {
        return jdbc.query("SELECT * FROM holdings", (rs, rowNum) -> {
            Holding h = new Holding();
            h.setSymbol(rs.getString("symbol"));
            h.setQuantity(rs.getDouble("quantity"));
            h.setAvgPrice(rs.getDouble("avg_price"));
            return h;
        });
    }

    public void deleteHoldingIfZero(String symbol) {
        jdbc.update("DELETE FROM holdings WHERE symbol = ? AND quantity <= 0", symbol);
    }
}
