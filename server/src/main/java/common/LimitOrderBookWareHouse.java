package common;

import commonData.Order.Direction;
import javafx.util.Pair;
import commonData.DataType.MarketDataType;
import session.Session;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.BidPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import trading.limitOrderBook.OrderComparatorType;


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
        limitOrderBooks.put(tickerSymbol, new Pair<sortedOrderList, sortedOrderList>(new sortedOrderList(bidComparator,lock,tickerSymbol, Direction.BUY)
                ,new sortedOrderList(askComparator,lock,tickerSymbol,Direction.SELL)));
    }

    public Pair<sortedOrderList, sortedOrderList> GetLimitOrderBook
            (String tickerSymbol, MarketDataType type, Session session) throws Exception{

        Pair<sortedOrderList, sortedOrderList> limitOrderBook = new Pair<sortedOrderList, sortedOrderList>
                (new sortedOrderList(bidComparator,Direction.BUY),new sortedOrderList(askComparator,Direction.SELL));

        switch (type) {
            case Level1:
                limitOrderBook.getKey().add(limitOrderBooks.get(tickerSymbol).getKey().get(0));
                limitOrderBook.getValue().add(limitOrderBooks.get(tickerSymbol).getValue().get(0));
                break;
            case Level3:
                limitOrderBook = limitOrderBooks.get(tickerSymbol);
                break;
            case ContinuousLevel3:
                //register session with bid book and ask book to have continuous level 3 data
                limitOrderBook = limitOrderBooks.get(tickerSymbol);
                limitOrderBooks.get(tickerSymbol).getKey().RegisterSessionForContinuousData(session);
                limitOrderBooks.get(tickerSymbol).getValue().RegisterSessionForContinuousData(session);
                break;
        }
        return limitOrderBook;
    }
}
