# Crypto Trading Simulator

### Quick start

1. Start MySQL (localhost:3306) and create a DB called `crypto_sim`.
2. In the **TradingApp** folder run:

   ```bash
   ./mvnw spring-boot:run
   ```
3. Visit **[http://localhost:8080/index.html](http://localhost:8080/index.html)**.

* Live prices for the top‑20 coins stream from Kraken.
* You trade with a virtual \$10 000 balance.
* Schema loads automatically from `src/main/resources/schema.sql`.

---

Demo only — no real money, no login.
