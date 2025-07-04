package com.app.service;

import com.app.dto.CryptoPriceDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceService {


    private static final List<String> SYMBOLS = List.of(
            "BTC/USDT", "ETH/USDT", "ADA/USDT", "SOL/USDT", "DOGE/USDT",
            "XRP/USDT", "TRX/USDT", "DOT/USDT", "LTC/USDT", "BCH/USDT",
            "LINK/USDT", "MATIC/USDT", "ATOM/USDT", "XLM/USDT", "UNI/USDT",
            "XMR/USDT", "ETC/USDT", "FIL/USDT", "ICP/USDT", "NEAR/USDT"
    );

    private final Map<String, Double> prices = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();


    @PostConstruct
    public void init() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            WebSocket ws = client.newWebSocketBuilder()
                    .buildAsync(URI.create("wss://ws.kraken.com/v2"), new WsListener())
                    .join();

            String subscribe = mapper.writeValueAsString(Map.of(
                    "method", "subscribe",
                    "params", Map.of(
                            "channel", "ticker",
                            "symbol", SYMBOLS
                    )
            ));
            ws.sendText(subscribe, true);

        } catch (Exception e) {
            //log.error("WebSocket init failed", e);
        }
    }

    private class WsListener implements WebSocket.Listener {
        private final StringBuilder buf = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket,
                                         CharSequence data,
                                         boolean last) {
            buf.append(data);
            if (last) {
                handle(buf.toString());
                buf.setLength(0);
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            //log.error("WebSocket error", error);
        }

        private void handle(String json) {
            try {
                JsonNode root = mapper.readTree(json);

                if (root.has("success")) {
                    boolean ok = root.get("success").asBoolean();
                    String sym = root.path("result").path("symbol").asText();
                    if (ok) {
                        //log.info("ACK success=true for {}", sym);
                    } else {
                        //log.warn("ACK FAILED \u21D2 {}  (symbol={})",
                                //root.path("error").asText(), sym);
                    }
                    return;
                }

                if ("ticker".equals(root.path("channel").asText())) {
                    JsonNode tick = root.path("data").get(0);     // always single element
                    String symbol = tick.get("symbol").asText();
                    double last = tick.get("last").asDouble();
                    prices.put(symbol, last);
                    //log.debug("{} \u2192 {}", symbol, last);
                }

                if (root.has("error")) {
                    //log.warn("Kraken error: {}", root.get("error").asText());
                }

            } catch (Exception ex) {
                //log.error("Parse error, raw={}", json, ex);
            }
        }
    }

    public List<CryptoPriceDTO> getTopPrices() {
        List<CryptoPriceDTO> list = new ArrayList<>();
        for (String s : SYMBOLS) {
            list.add(new CryptoPriceDTO(s, s,
                    prices.getOrDefault(s, 0.0)));
        }
        return list;
    }
}
