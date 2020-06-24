package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.MarketDataWareHouse;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.trading.TradingEngine;
import nw.ExchangePlatform.trading.TradingEngineGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class TradingEngineManager{
    private Queue systemQueue;
    private MarketDataWareHouse dataWareHouse;

    public TradingEngineManager(Queue systemQueue, MarketDataWareHouse dataWareHouse) {
        this.systemQueue = systemQueue;
        this.dataWareHouse = dataWareHouse;
    }

    public void Start() throws Exception{
        int numOfOrderQueues = systemQueue.getNumberOfOrderQueues();

        for(int i = 0; i < numOfOrderQueues; i++) {
            TradingEngineGroup engineGroup = new TradingEngineGroup(systemQueue, i, dataWareHouse);
            Thread engine = new Thread(engineGroup);
            engine.start();
        }
    }
}
