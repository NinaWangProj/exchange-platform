package nw.ExchangePlatform.commonData.marketData;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class MarketDataWareHouse {
    //<tickerSymbol, Pair<bids,asks>>
    private HashMap<String, MarketData> marketDataMap;

    public MarketDataWareHouse() {
        marketDataMap = new HashMap<>();
    }

    public MarketData getMarketData(String tickerSymbol) {
        return marketDataMap.get(tickerSymbol);
    }

    public void setMarketData(String tickerSymbol, ArrayList<MarketDataItem> bids, ArrayList<MarketDataItem> asks ) {
        marketDataMap.get(tickerSymbol).setBids(bids);
        marketDataMap.get(tickerSymbol).setAsks(asks);
    }
}
