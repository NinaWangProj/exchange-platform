package trading.workflow;

import common.ServerQueue;
import common.LimitOrderBookWareHouse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

public class TradingEngineManager{
    private ServerQueue systemServerQueue;
    private LimitOrderBookWareHouse dataWareHouse;
    private ConcurrentHashMap<String,ReadWriteLock> locks;

    public TradingEngineManager(ServerQueue systemQueue, LimitOrderBookWareHouse dataWareHouse, ConcurrentHashMap<String,ReadWriteLock> locks) {
        this.systemServerQueue = systemQueue;
        this.dataWareHouse = dataWareHouse;
        this.locks = locks;
    }

    public void Start() throws Exception{
        int numOfOrderQueues = systemServerQueue.getNumberOfOrderQueues();

        for(int i = 0; i < numOfOrderQueues; i++) {
            TradingEngineGroup engineGroup = new TradingEngineGroup(systemServerQueue, i, dataWareHouse, locks);
            Thread engine = new Thread(engineGroup);
            engine.start();
        }
    }
}
