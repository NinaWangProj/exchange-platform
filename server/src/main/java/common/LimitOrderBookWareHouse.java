package common;

import com.fasterxml.jackson.databind.ObjectMapper;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import commonData.DataType.MarketDataType;
import session.Session;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.BidPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import trading.limitOrderBook.OrderComparatorType;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LimitOrderBookWareHouse {
    private ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>> limitOrderBooks;
    private OrderComparator bidComparator;
    private OrderComparator askComparator;
    private ConcurrentHashMap<String,ReadWriteLock> locks;

    public LimitOrderBookWareHouse(OrderComparatorType comparatorType) {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>>();
        if(comparatorType==OrderComparatorType.PriceTimePriority) {
            bidComparator = new BidPriceTimeComparator();
            askComparator = new AskPriceTimeComparator();
        }

        locks = new ConcurrentHashMap<String,ReadWriteLock>();
    }

    public boolean ValidateRequest(String tickerSymbol) {
        boolean found = false;
        if(limitOrderBooks.containsKey(tickerSymbol)) {
            found = true;
        }
        return found;
    }

    public Pair<SortedOrderList, SortedOrderList> GetLimitOrderBook(String tickerSymbol) {

        locks.putIfAbsent(tickerSymbol, new ReentrantReadWriteLock());
        ReadWriteLock lock = locks.get(tickerSymbol);

        limitOrderBooks.putIfAbsent(tickerSymbol, new Pair<>(new SortedOrderList(bidComparator,lock,tickerSymbol, Direction.BUY),
                new SortedOrderList(askComparator,lock,tickerSymbol,Direction.SELL)));

        return limitOrderBooks.get(tickerSymbol);
    }

    public void AddNewLimitOrderBook(String tickerSymbol, ReadWriteLock lock) {

    }

    public Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> GetLimitOrderBookCopy
            (String tickerSymbol, MarketDataType type, Session session) throws Exception{

        Pair<SortedOrderList, SortedOrderList> limitOrderBook = GetLimitOrderBook(tickerSymbol);

        ReadWriteLock lock = locks.get(tickerSymbol);
        lock.readLock().lock();

        Pair<SortedOrderList, SortedOrderList> bookToCopy;
        switch (type) {
            case Level1:
                bookToCopy = new Pair<> (new SortedOrderList(bidComparator,new ReentrantReadWriteLock(),tickerSymbol,Direction.BUY),
                        new SortedOrderList(askComparator,new ReentrantReadWriteLock(),tickerSymbol,Direction.SELL));

                MarketParticipantOrder topOfBids = limitOrderBook.getKey().get(0);
                if(topOfBids == null)
                    topOfBids = new MarketParticipantOrder();

                MarketParticipantOrder topOfAsks = limitOrderBook.getValue().get(0);
                if(topOfAsks == null)
                    topOfAsks = new MarketParticipantOrder();

                bookToCopy.getKey().add(topOfBids);
                bookToCopy.getValue().add(topOfAsks);
                break;
            case Level3:
                bookToCopy = limitOrderBook;
                break;
            case ContinuousLevel3:
                //register session with bid book and ask book to have continuous level 3 data
                bookToCopy = limitOrderBook;
                limitOrderBooks.get(tickerSymbol).getKey().RegisterSessionForContinuousData(session);
                limitOrderBooks.get(tickerSymbol).getValue().RegisterSessionForContinuousData(session);
                break;
            default:
                throw new IllegalArgumentException("Cannot handle enum MarketDataType with value of " + type.name());
        }

        Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> marketData = FormMarketDataFromOrderBook(bookToCopy);

        lock.readLock().unlock();

        return marketData;
    }

    private Pair<ArrayList<MarketDataItem>,ArrayList<MarketDataItem>> FormMarketDataFromOrderBook(
            Pair<SortedOrderList, SortedOrderList> limitOrderBook) {
        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();

        for(MarketParticipantOrder order : limitOrderBook.getKey().getSortedList()) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            bids.add(item);
        }

        for(MarketParticipantOrder order : limitOrderBook.getValue().getSortedList()) {
            MarketDataItem item = new MarketDataItem(order.getTickerSymbol(),order.getSize(),order.getPrice());
            asks.add(item);
        }

        return new Pair<>(bids,asks);
    }

    public void WriteToJSONString(OutputStream outputStream) {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        try {
            String jsonStr = JSONObjectMapper.writeValueAsString(this);
            outputWriter.write(jsonStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
