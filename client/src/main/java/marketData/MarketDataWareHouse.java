package marketData;

import commonData.limitOrderBook.ChangeOperation;
import commonData.marketData.MarketDataItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MarketDataWareHouse {
    //<tickerSymbol, Pair<bids,asks>>
    private HashMap<String, MarketData> marketDataMap;

    public MarketDataWareHouse() {
        marketDataMap = new HashMap<String, MarketData>();
    }

    public MarketData getMarketData(String tickerSymbol) {
        return marketDataMap.get(tickerSymbol);
    }

    public void setMarketData(String tickerSymbol, ArrayList<MarketDataItem> bids, ArrayList<MarketDataItem> asks ) {
        marketDataMap.get(tickerSymbol).setBids(bids);
        marketDataMap.get(tickerSymbol).setAsks(asks);
    }

    public void applyBookChanges(String tickerSymbol, List<ChangeOperation> bookChanges) {

    }
}
