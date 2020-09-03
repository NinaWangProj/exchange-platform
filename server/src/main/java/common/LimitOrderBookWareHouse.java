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
    private ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>> limitOrderBooks;
    private OrderComparator bidComparator;
    private OrderComparator askComparator;

    public LimitOrderBookWareHouse(OrderComparatorType comparatorType) {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>>();
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

    public Pair<SortedOrderList, SortedOrderList> GetLimitOrderBook(String tickerSymbol) {
        return limitOrderBooks.get(tickerSymbol);
    }

    public void AddNewLimitOrderBook(String tickerSymbol, ReadWriteLock lock) {
        limitOrderBooks.put(tickerSymbol, new Pair<SortedOrderList, SortedOrderList>(new SortedOrderList(bidComparator,lock,tickerSymbol, Direction.BUY)
                ,new SortedOrderList(askComparator,lock,tickerSymbol,Direction.SELL)));
    }

    public Pair<SortedOrderList, SortedOrderList> GetLimitOrderBook
            (String tickerSymbol, MarketDataType type, Session session) throws Exception{

        Pair<SortedOrderList, SortedOrderList> limitOrderBook = new Pair<SortedOrderList, SortedOrderList>
                (new SortedOrderList(bidComparator,Direction.BUY),new SortedOrderList(askComparator,Direction.SELL));

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
