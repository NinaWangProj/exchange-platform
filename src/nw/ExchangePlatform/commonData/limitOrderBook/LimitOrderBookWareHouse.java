package nw.ExchangePlatform.commonData.limitOrderBook;

import javafx.util.Pair;
import nw.ExchangePlatform.commonData.marketData.*;

import java.util.concurrent.ConcurrentHashMap;

public class LimitOrderBookWareHouse {
    //<tickerSymbol,<bids,asks>>
    private ConcurrentHashMap<String, Pair<sortedOrderList, sortedOrderList>> limitOrderBooks;
    private OrderComparator bidComparator;
    private OrderComparator askComparator;

    public LimitOrderBookWareHouse(OrderComparatorType comparatorType) {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<sortedOrderList, sortedOrderList>>();
        if(comparatorType==OrderComparatorType.PriceTimePriority) {
            bidComparator = new BidPriceTimeComparator();
            askComparator = new AskPriceTimeComparator();
        }
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
        limitOrderBooks.put(tickerSymbol, new Pair<>(new sortedOrderList(bidComparator),new sortedOrderList(askComparator)));
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
