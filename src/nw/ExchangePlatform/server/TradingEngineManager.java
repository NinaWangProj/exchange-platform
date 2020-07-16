package nw.ExchangePlatform.server;

import nw.ExchangePlatform.commonData.ServerQueue;
import nw.ExchangePlatform.commonData.limitOrderBook.LimitOrderBookWareHouse;
import nw.ExchangePlatform.trading.TradingEngineGroup;

public class TradingEngineManager{
    private ServerQueue systemServerQueue;
    private LimitOrderBookWareHouse dataWareHouse;

    public TradingEngineManager(ServerQueue systemQueue, LimitOrderBookWareHouse dataWareHouse) {
        this.systemServerQueue = systemQueue;
        this.dataWareHouse = dataWareHouse;
    }

    public void Start() throws Exception{
        int numOfOrderQueues = systemServerQueue.getNumberOfOrderQueues();

        for(int i = 0; i < numOfOrderQueues; i++) {
            TradingEngineGroup engineGroup = new TradingEngineGroup(systemServerQueue, i, dataWareHouse);
            Thread engine = new Thread(engineGroup);
            engine.start();
        }
    }
}
