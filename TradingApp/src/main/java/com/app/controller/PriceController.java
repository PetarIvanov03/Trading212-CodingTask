package com.app.controller;

import com.app.dto.CryptoPriceDTO;
import com.app.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@CrossOrigin(origins = "*")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @GetMapping
    public List<CryptoPriceDTO> getPrices() {
        return priceService.getTopPrices();
    }
}
