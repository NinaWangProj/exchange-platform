package nw.ExchangePlatform.trading;

import javafx.util.Pair;
import nw.ExchangePlatform.trading.limitOrderBook.LimitOrderBookWareHouse;
import nw.ExchangePlatform.trading.data.MarketParticipantOrder;
import nw.ExchangePlatform.trading.data.TradingOutput;
import nw.ExchangePlatform.trading.limitOrderBook.sortedOrderList;
import nw.ExchangePlatform.commonData.ServerQueue;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;

public class TradingEngineGroup implements Runnable{
    private ServerQueue systemServerQueue;
    //tradingEngineGroupID is used as an index to look up the correct order queue in the queue array;
    private final int tradingEngineGroupID;
    private HashMap<String,TradingEngine> tradingEngineMap;
    private LimitOrderBookWareHouse dataWareHouse;
    private ReadWriteLock lock;

    public TradingEngineGroup (ServerQueue systemServerQueue, int tradingEngineGroupID,
                               LimitOrderBookWareHouse dataWareHouse, ReadWriteLock lock) {
        this.systemServerQueue = systemServerQueue;
        this.tradingEngineGroupID = tradingEngineGroupID;
        this.dataWareHouse = dataWareHouse;
        this.lock = lock;
    }

    public void Start() throws Exception{
        while(true) {
            MarketParticipantOrder order = systemServerQueue.TakeOrder(tradingEngineGroupID);
            String tickerSymbol = order.getTickerSymbol();

            if (!tradingEngineMap.containsKey(tickerSymbol)) {
                dataWareHouse.AddNewLimitOrderBook(tickerSymbol,lock);
                Pair<sortedOrderList, sortedOrderList> limitOrderBook
                        = dataWareHouse.GetLimitOrderBook(tickerSymbol);
                TradingEngine tradingEngine = new TradingEngine(tickerSymbol,limitOrderBook);
                tradingEngineMap.put(tickerSymbol,tradingEngine);
            }
            TradingOutput output = tradingEngineMap.get(tickerSymbol).Process(order);
            systemServerQueue.PutTradingResult(output);
        }
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {

        }
    }
}
