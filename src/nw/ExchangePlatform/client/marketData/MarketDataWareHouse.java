package nw.ExchangePlatform.client.marketData;

import javafx.util.Pair;
import nw.ExchangePlatform.trading.limitOrderBook.BookOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public void applyBookChanges(String tickerSymbol, List<Pair<BookOperation, Object[]>> bookChanges) {

    }
}
