package commonData.marketData;

import commonData.Order.MarketParticipantOrder;

public class MarketDataItem {
    private final String tickerSymbol;
    private int size;
    private final double price;

    public MarketDataItem(String tickerSymbol, int size, double price) {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
    }

    public MarketDataItem(MarketParticipantOrder marketParticipantOrder) {
        this.tickerSymbol = marketParticipantOrder.getTickerSymbol();
        this.size = marketParticipantOrder.getSize();
        this.price = marketParticipantOrder.getPrice();
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }
}
