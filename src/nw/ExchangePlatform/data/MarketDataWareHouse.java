package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class MarketDataWareHouse {
    //<tickerSymbol,<bids,asks>>
    private ConcurrentHashMap<String, Pair<sortedOrderList, sortedOrderList>> limitOrderBooks;

    public MarketDataWareHouse() {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<sortedOrderList, sortedOrderList>>();
    }

    public boolean ValidateRequest(String tickerSymbol) {
        boolean found = false;
        if(limitOrderBooks.containsKey(tickerSymbol)) {
            found = true;
        }
        return found;
    }

    public Pair<sortedOrderList, sortedOrderList> GetLimitOrderBook(String tickerSymbol) {
        return limitOrderBooks.get(tickerSymbol);
    }

    public void AddNewLimitOrderBook(String tickerSymbol) {
        limitOrderBooks.put(tickerSymbol, new Pair<>(new sortedOrderList(),new sortedOrderList()));
    }

    public Pair<sortedOrderList, sortedOrderList> GetMarketData
            (String tickerSymbol, MarketDataType type) {
        Pair<sortedOrderList, sortedOrderList> marketData =
                new Pair<>(new sortedOrderList(),new sortedOrderList());
        switch (type) {
            case Level1:
                marketData.getKey().add(limitOrderBooks.get(tickerSymbol).getKey().get(0));
                marketData.getValue().add(limitOrderBooks.get(tickerSymbol).getValue().get(0));

            case Level2:
                marketData = limitOrderBooks.get(tickerSymbol);
        }
        return marketData;
    }
}
