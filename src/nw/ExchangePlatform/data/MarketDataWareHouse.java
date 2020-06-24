package nw.ExchangePlatform.data;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataWareHouse {
    //<tickerSymbol,<bids,asks>>
    private ConcurrentHashMap<String, Pair<ArrayList<MarketParticipantOrder>,ArrayList<MarketParticipantOrder>>> limitOrderBooks;

    public MarketDataWareHouse() {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<ArrayList<MarketParticipantOrder>,ArrayList<MarketParticipantOrder>>>();
    }

    public boolean ValidateRequest(String tickerSymbol) {
        boolean found = false;
        if(limitOrderBooks.containsKey(tickerSymbol)) {
            found = true;
        }
        return found;
    }

    public Pair<ArrayList<MarketParticipantOrder>,ArrayList<MarketParticipantOrder>> GetLimitOrderBok(String tickerSymbol) {
        return limitOrderBooks.get(tickerSymbol);
    }

    public void AddNewLimitOrderBook(String tickerSymbol) {
        limitOrderBooks.put(tickerSymbol, new Pair(new ArrayList<MarketParticipantOrder>(),
                new ArrayList<MarketParticipantOrder>()));
    }

    public Pair<ArrayList<MarketParticipantOrder>,ArrayList<MarketParticipantOrder>> GetMarketData
            (String tickerSymbol, MarketDataType type) {
        Pair<ArrayList<MarketParticipantOrder>,ArrayList<MarketParticipantOrder>> marketData =
                new Pair<>(new ArrayList<>(),new ArrayList<>());
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
