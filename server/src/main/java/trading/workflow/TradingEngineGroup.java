package trading.workflow;

import javafx.util.Pair;
import common.ServerQueue;
import common.LimitOrderBookWareHouse;
import commonData.Order.MarketParticipantOrder;
import common.TradingOutput;
import common.sortedOrderList;


import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TradingEngineGroup implements Runnable{
    private ServerQueue systemServerQueue;
    //tradingEngineGroupID is used as an index to look up the correct order queue in the queue array;
    private final int tradingEngineGroupID;
    private HashMap<String, TradingEngine> tradingEngineMap;
    private LimitOrderBookWareHouse dataWareHouse;
    private ConcurrentHashMap<String,ReadWriteLock> locks;

    public TradingEngineGroup (ServerQueue systemServerQueue, int tradingEngineGroupID,
                               LimitOrderBookWareHouse dataWareHouse, ConcurrentHashMap<String,ReadWriteLock> locks) {
        this.systemServerQueue = systemServerQueue;
        this.tradingEngineGroupID = tradingEngineGroupID;
        this.dataWareHouse = dataWareHouse;
        this.locks = locks;
    }

    public void Start() throws Exception{
        while(true) {
            MarketParticipantOrder order = systemServerQueue.TakeOrder(tradingEngineGroupID);
            String tickerSymbol = order.getTickerSymbol();

            if (!tradingEngineMap.containsKey(tickerSymbol)) {
                ReadWriteLock lock = new ReentrantReadWriteLock();
                locks.put(tickerSymbol,lock);
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
