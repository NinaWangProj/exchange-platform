package nw.ExchangePlatform.trading;

import javafx.util.Pair;
import nw.ExchangePlatform.data.MarketDataWareHouse;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.data.sortedOrderList;
import nw.ExchangePlatform.wrapper.Queue;

import java.util.HashMap;

public class TradingEngineGroup implements Runnable{
    private Queue systemQueue;
    //tradingEngineGroupID is used as an index to look up the correct order queue in the queue array;
    private final int tradingEngineGroupID;
    private HashMap<String,TradingEngine> tradingEngineMap;
    private MarketDataWareHouse dataWareHouse;

    public TradingEngineGroup (Queue systemQueue, int tradingEngineGroupID, MarketDataWareHouse dataWareHouse) {
        this.systemQueue = systemQueue;
        this.tradingEngineGroupID = tradingEngineGroupID;
        this.dataWareHouse = dataWareHouse;
    }

    public void Start() throws Exception{
        while(true) {
            MarketParticipantOrder order = systemQueue.TakeOrder(tradingEngineGroupID);
            String tickerSymbol = order.getTickerSymbol();

            if (!tradingEngineMap.containsKey(tickerSymbol)) {
                dataWareHouse.AddNewLimitOrderBook(tickerSymbol);
                Pair<sortedOrderList,sortedOrderList> limitOrderBook
                        = dataWareHouse.GetLimitOrderBook(tickerSymbol);
                TradingEngine tradingEngine = new TradingEngine(tickerSymbol,limitOrderBook);
                tradingEngineMap.put(tickerSymbol,tradingEngine);
            }
            TradingOutput output = tradingEngineMap.get(tickerSymbol).Process(order);
            systemQueue.PutTradingResult(output);
        }
    }

    public void run() {
        try {
            Start();
        } catch (Exception e) {

        }
    }
}
