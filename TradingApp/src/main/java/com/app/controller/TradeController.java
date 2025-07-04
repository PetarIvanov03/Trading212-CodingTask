package com.app.controller;

import com.app.dto.TradeRequestDTO;
import com.app.model.Trade;
import com.app.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String handle(RuntimeException ex) {
        return ex.getMessage();
    }

    @PostMapping("/execute")
    public String execute(@RequestBody TradeRequestDTO req, @RequestParam double price) {
        tradeService.processTrade(req, price);
        return "Trade executed successfully.";
    }

    @PostMapping("/reset")
    public String reset() {
        tradeService.reset();
        return "Account reset.";
    }

    @GetMapping("/history")
    public List<Trade> history() {
        return tradeService.getHistory();
    }

    @GetMapping("/balance")
    public double balance() {
        return tradeService.getBalance();
    }

    @GetMapping("/holdings")
    public List<com.app.model.Holding> holdings() {
        return tradeService.getHoldings();
    }
}
