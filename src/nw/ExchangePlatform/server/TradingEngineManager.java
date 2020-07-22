package nw.ExchangePlatform.server;

import nw.ExchangePlatform.commonData.ServerQueue;
import nw.ExchangePlatform.trading.limitOrderBook.LimitOrderBookWareHouse;
import nw.ExchangePlatform.trading.TradingEngineGroup;

import java.util.concurrent.locks.ReadWriteLock;

public class TradingEngineManager{
    private ServerQueue systemServerQueue;
    private LimitOrderBookWareHouse dataWareHouse;
    private ReadWriteLock[] locks;

    public TradingEngineManager(ServerQueue systemQueue, LimitOrderBookWareHouse dataWareHouse, ReadWriteLock[] locks) {
        this.systemServerQueue = systemQueue;
        this.dataWareHouse = dataWareHouse;
        this.locks = locks;
    }

    public void Start() throws Exception{
        int numOfOrderQueues = systemServerQueue.getNumberOfOrderQueues();

        for(int i = 0; i < numOfOrderQueues; i++) {
            TradingEngineGroup engineGroup = new TradingEngineGroup(systemServerQueue, i, dataWareHouse, locks[i]);
            Thread engine = new Thread(engineGroup);
            engine.start();
        }
    }
}
