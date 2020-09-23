package trading.workflow;

import javafx.util.Pair;
import common.ServerQueue;
import common.LimitOrderBookWareHouse;
import commonData.Order.MarketParticipantOrder;
import common.TradingOutput;
import common.SortedOrderList;


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

    public TradingEngineGroup (ServerQueue systemServerQueue, int tradingEngineGroupID,
                               LimitOrderBookWareHouse dataWareHouse) {
        this.systemServerQueue = systemServerQueue;
        this.tradingEngineGroupID = tradingEngineGroupID;
        this.dataWareHouse = dataWareHouse;
        tradingEngineMap = new HashMap<>();
    }

    public void Start() throws Exception{
        while(true) {
            MarketParticipantOrder order = systemServerQueue.TakeOrder(tradingEngineGroupID);
            String tickerSymbol = order.getTickerSymbol();

            if (!tradingEngineMap.containsKey(tickerSymbol)) {

                Pair<SortedOrderList, SortedOrderList> limitOrderBook
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
