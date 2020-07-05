package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataWareHouse {
    //<tickerSymbol,<bids,asks>>
    private ConcurrentHashMap<String, Pair<OrderDataWareHouse, OrderDataWareHouse>> limitOrderBooks;

    public MarketDataWareHouse() {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<OrderDataWareHouse, OrderDataWareHouse>>();
    }

    public boolean ValidateRequest(String tickerSymbol) {
        boolean found = false;
        if(limitOrderBooks.containsKey(tickerSymbol)) {
            found = true;
        }
        return found;
    }

    public Pair<OrderDataWareHouse, OrderDataWareHouse> GetLimitOrderBook(String tickerSymbol) {
        return limitOrderBooks.get(tickerSymbol);
    }

    public void AddNewLimitOrderBook(String tickerSymbol) {
        limitOrderBooks.put(tickerSymbol, new Pair(new ArrayList<MarketParticipantOrder>(),
                new ArrayList<MarketParticipantOrder>()));
    }

    public Pair<OrderDataWareHouse, OrderDataWareHouse> GetMarketData
            (String tickerSymbol, MarketDataType type) {
        Pair<OrderDataWareHouse, OrderDataWareHouse> marketData =
                new Pair<>(new OrderDataWareHouse(),new OrderDataWareHouse());
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
