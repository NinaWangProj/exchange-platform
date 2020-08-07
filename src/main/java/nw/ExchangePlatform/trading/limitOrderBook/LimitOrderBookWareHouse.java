package nw.ExchangePlatform.trading.limitOrderBook;

import javafx.util.Pair;
import nw.ExchangePlatform.commonData.DataType.MarketDataType;
import nw.ExchangePlatform.server.session.Session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

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

    public void AddNewLimitOrderBook(String tickerSymbol, ReadWriteLock lock) {
        limitOrderBooks.put(tickerSymbol, new Pair<sortedOrderList, sortedOrderList>(new sortedOrderList(bidComparator,lock,tickerSymbol),new sortedOrderList(askComparator,lock,tickerSymbol)));
    }

    public Pair<sortedOrderList, sortedOrderList> GetLimitOrderBook
            (String tickerSymbol, MarketDataType type, Session session) throws Exception{
        Pair<sortedOrderList, sortedOrderList> marketData =
                new Pair<sortedOrderList, sortedOrderList>(new sortedOrderList(bidComparator),new sortedOrderList(askComparator));
        switch (type) {
            case Level1:
                marketData.getKey().add(limitOrderBooks.get(tickerSymbol).getKey().get(0));
                marketData.getValue().add(limitOrderBooks.get(tickerSymbol).getValue().get(0));

            case Level3:
                marketData = limitOrderBooks.get(tickerSymbol);

            case ContinuousLevel3:
                //register session with bid book and ask book to have continuous level 3 data
                marketData = limitOrderBooks.get(tickerSymbol);
                limitOrderBooks.get(tickerSymbol).getKey().RegisterSessionForContinuousData(session);
                limitOrderBooks.get(tickerSymbol).getValue().RegisterSessionForContinuousData(session);
        }
        return marketData;
    }
}
