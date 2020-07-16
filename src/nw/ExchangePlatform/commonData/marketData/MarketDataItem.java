package nw.ExchangePlatform.commonData.marketData;

import nw.ExchangePlatform.trading.data.Direction;
import nw.ExchangePlatform.trading.data.OrderDuration;
import nw.ExchangePlatform.trading.data.OrderType;

import java.util.ArrayList;
import java.util.Date;

public class MarketDataItem {
    private final String tickerSymbol;
    private int size;
    private final double price;

    public MarketDataItem(String tickerSymbol, int size, double price) {
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
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
