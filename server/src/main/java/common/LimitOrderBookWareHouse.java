package common;

import clearing.data.CredentialRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import commonData.DataType.MarketDataType;
import jdk.internal.util.xml.impl.Input;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder;
import org.graalvm.compiler.core.common.type.ArithmeticOpTable;
import session.Session;
import trading.limitOrderBook.AskPriceTimeComparator;
import trading.limitOrderBook.BidPriceTimeComparator;
import trading.limitOrderBook.OrderComparator;
import trading.limitOrderBook.OrderComparatorType;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class LimitOrderBookWareHouse {
    private ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>> limitOrderBooks;
    private OrderComparator bidComparator;
    private OrderComparator askComparator;
    private ConcurrentHashMap<String,ReadWriteLock> locks;

    public LimitOrderBookWareHouse(OrderComparatorType comparatorType,ConcurrentHashMap<String,ReadWriteLock> locks,
                                   ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>> limitOrderBooks) {
        this.limitOrderBooks = limitOrderBooks;

        if(comparatorType==OrderComparatorType.PriceTimePriority) {
            bidComparator = new BidPriceTimeComparator();
            askComparator = new AskPriceTimeComparator();
        }

        this.locks = locks;
    }

    public LimitOrderBookWareHouse(OrderComparatorType comparatorType) {
        limitOrderBooks = new ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>>();
        if(comparatorType==OrderComparatorType.PriceTimePriority) {
            bidComparator = new BidPriceTimeComparator();
            askComparator = new AskPriceTimeComparator();
        }

        locks = new ConcurrentHashMap<String,ReadWriteLock>();
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

    public void WriteToCSV(OutputStream bidsOutputStream, OutputStream asksOutputStream) {
        OutputStreamWriter bidsOutputWriter = new OutputStreamWriter(bidsOutputStream);
        OutputStreamWriter asksOutputWriter = new OutputStreamWriter(asksOutputStream);


        List<MarketParticipantOrder> bids = limitOrderBooks.entrySet()
                .stream()
                .flatMap(e->e.getValue().getKey().getSortedList().stream())
                .collect(Collectors.toList());

        List<MarketParticipantOrder> asks = limitOrderBooks.entrySet()
                .stream()
                .flatMap(e->e.getValue().getValue().getSortedList().stream())
                .collect(Collectors.toList());

        StatefulBeanToCsv bidsToCsv = new StatefulBeanToCsvBuilder(bidsOutputWriter).build();
        StatefulBeanToCsv asksToCsv = new StatefulBeanToCsvBuilder(asksOutputWriter).build();

        try {
            bidsToCsv.write(bids);
            asksToCsv.write(asks);
            bidsOutputWriter.close();
            asksOutputWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LimitOrderBookWareHouse BuildFromCSV(InputStream bidsInputStream, InputStream asksInputStream,
                                                       OrderComparatorType comparatorType) {
        LimitOrderBookWareHouse limitOrderBookWareHouse = null;

        InputStreamReader bidsInputReader = new InputStreamReader(bidsInputStream);
        InputStreamReader asksInputReader = new InputStreamReader(asksInputStream);

        CsvToBean<MarketParticipantOrder> csvBidsReader = new CsvToBeanBuilder(bidsInputReader)
                .withType(MarketParticipantOrder.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<MarketParticipantOrder> bids = csvBidsReader.parse();

        CsvToBean<MarketParticipantOrder> csvAsksReader = new CsvToBeanBuilder(asksInputReader)
                .withType(MarketParticipantOrder.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<MarketParticipantOrder> asks = csvAsksReader.parse();

        Map<String, ArrayList<MarketParticipantOrder>> bidBooks = bids.stream().collect(Collectors.groupingBy(
                MarketParticipantOrder::getTickerSymbol, Collectors.toCollection(ArrayList::new)));
        Map<String, ArrayList<MarketParticipantOrder>> askBooks = asks.stream().collect(Collectors.groupingBy(
                MarketParticipantOrder::getTickerSymbol, Collectors.toCollection(ArrayList::new)));

        ConcurrentHashMap<String, Pair<SortedOrderList, SortedOrderList>> deserializedOrderBooks = new ConcurrentHashMap<>();
        OrderComparator bidComparator = new BidPriceTimeComparator();
        OrderComparator askComparator = new AskPriceTimeComparator();
        ConcurrentHashMap<String,ReadWriteLock> locks = new ConcurrentHashMap<>();

        for(Map.Entry<String, ArrayList<MarketParticipantOrder>> entry : bidBooks.entrySet()) {
            String tickerSymbol = entry.getKey();
            ReadWriteLock lock = new ReentrantReadWriteLock();
            locks.put(tickerSymbol, lock);
            SortedOrderList bidsList = new SortedOrderList(bidComparator, entry.getValue(), lock,
                    tickerSymbol, Direction.BUY);
            SortedOrderList asksList;
            if(askBooks.containsKey(tickerSymbol)) {
                asksList = new SortedOrderList(askComparator,askBooks.get(tickerSymbol), lock,
                        tickerSymbol, Direction.SELL);
            } else {
                asksList = new SortedOrderList(askComparator,lock,tickerSymbol,Direction.SELL);
            }

            deserializedOrderBooks.putIfAbsent(tickerSymbol, new Pair<>(bidsList, asksList));
        }

        for(Map.Entry<String, ArrayList<MarketParticipantOrder>> entry : askBooks.entrySet()) {
            String tickerSymbol = entry.getKey();
            ReadWriteLock lock = new ReentrantReadWriteLock();
            locks.put(tickerSymbol, lock);
            SortedOrderList asksList = new SortedOrderList(askComparator, entry.getValue(), lock,
                    tickerSymbol, Direction.SELL);

            deserializedOrderBooks.putIfAbsent(tickerSymbol, new Pair<>(new SortedOrderList(bidComparator,lock,tickerSymbol, Direction.BUY)
                    , asksList));
        }

        limitOrderBookWareHouse = new LimitOrderBookWareHouse(comparatorType, locks, deserializedOrderBooks);

        return limitOrderBookWareHouse;
    }
}
