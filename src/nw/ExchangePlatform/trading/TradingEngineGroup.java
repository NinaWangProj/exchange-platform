package nw.ExchangePlatform.trading;

import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.wrapper.Queue;

import java.util.HashMap;

public class TradingEngineGroup implements Runnable{
    private Queue systemQueue;
    //tradingEngineGroupID is used as an index to look up the correct order queue in the queue array;
    private final int tradingEngineGroupID;
    private HashMap<String,TradingEngine> tradingEngineMap;

    public TradingEngineGroup (Queue systemQueue, int tradingEngineGroupID) {
        this.systemQueue = systemQueue;
        this.tradingEngineGroupID = tradingEngineGroupID;
    }

    public void Start() throws Exception{
        while(true) {
            MarketParticipantOrder order = systemQueue.TakeOrder(tradingEngineGroupID);
            String tickerSymbol = order.getTickerSymbol();

            if (!tradingEngineMap.containsKey(tickerSymbol)) {
                TradingEngine tradingEngine = new TradingEngine(tickerSymbol);
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
