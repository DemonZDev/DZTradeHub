package online.demonzdevelopment.dztradehub.data;

import java.util.UUID;

public class CoinFlipRequest {
    private final UUID requestId;
    private final UUID fromPlayer;
    private final UUID toPlayer;
    private final String currencyType; // MONEY, MOBCOIN, GEM
    private final double amount;
    private final CoinSide side;
    private final long createdTime;
    private final long expiryTime;

    public enum CoinSide {
        HEAD, TAIL
    }

    public CoinFlipRequest(UUID fromPlayer, UUID toPlayer, String currencyType, double amount, CoinSide side, int expiryMinutes) {
        this.requestId = UUID.randomUUID();
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.currencyType = currencyType;
        this.amount = amount;
        this.side = side;
        this.createdTime = System.currentTimeMillis();
        this.expiryTime = createdTime + (expiryMinutes * 60 * 1000L);
    }

    public UUID getRequestId() { return requestId; }
    public UUID getFromPlayer() { return fromPlayer; }
    public UUID getToPlayer() { return toPlayer; }
    public String getCurrencyType() { return currencyType; }
    public double getAmount() { return amount; }
    public CoinSide getSide() { return side; }
    public long getCreatedTime() { return createdTime; }
    public long getExpiryTime() { return expiryTime; }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
    
    public long getRemainingTime() {
        return Math.max(0, expiryTime - System.currentTimeMillis());
    }
}
