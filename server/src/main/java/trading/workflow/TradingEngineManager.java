package trading.workflow;

import common.ServerQueue;
import common.LimitOrderBookWareHouse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;

public class TradingEngineManager{
    private ServerQueue systemServerQueue;
    private LimitOrderBookWareHouse dataWareHouse;
    private static AtomicLong previousTransactionID;

    public TradingEngineManager(ServerQueue systemQueue, LimitOrderBookWareHouse dataWareHouse,
                                AtomicLong previousTransactionID) {
        this.systemServerQueue = systemQueue;
        this.dataWareHouse = dataWareHouse;
        this.previousTransactionID = previousTransactionID;
    }

    public void Start() throws Exception{
        int numOfOrderQueues = systemServerQueue.getNumberOfOrderQueues();

        for(int i = 0; i < numOfOrderQueues; i++) {
            TradingEngineGroup engineGroup = new TradingEngineGroup(systemServerQueue, i, dataWareHouse);
            Thread engine = new Thread(engineGroup);
            engine.start();
        }
    }

    public static long getNewTransactionID() {
        return previousTransactionID.incrementAndGet();
    }

    public static long getTransactionID() {
        return previousTransactionID.get();
    }
}
