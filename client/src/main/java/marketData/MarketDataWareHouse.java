package marketData;

import commonData.Order.Direction;
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

        if (!marketDataMap.containsKey(tickerSymbol)) {
            marketDataMap.put(tickerSymbol,new MarketData(tickerSymbol, new ArrayList<MarketDataItem>(),
                    new ArrayList<MarketDataItem>()));
        }

        marketDataMap.get(tickerSymbol).setBids(bids);
        marketDataMap.get(tickerSymbol).setAsks(asks);
    }

    public void applyBookChanges(String tickerSymbol, Direction direction, List<ChangeOperation> bookChanges) {
        if(!marketDataMap.containsKey(tickerSymbol))
            marketDataMap.put(tickerSymbol, new MarketData(tickerSymbol));

        marketDataMap.get(tickerSymbol).applyBookOperations(direction,bookChanges);
    }
}
