package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.clearing.ClearingEngine;
import nw.ExchangePlatform.clearing.ClearingEngineWrapper;
import nw.ExchangePlatform.data.DTCCWarehouse;
import nw.ExchangePlatform.trading.TradingEngineGroup;

public class ClearingEngineManager {
    private Queue systemQueue;
    private DTCCWarehouse DTCC;

    public ClearingEngineManager(Queue systemQueue, DTCCWarehouse DTCC) {
        this.systemQueue = systemQueue;
        this.DTCC = DTCC;
    }

    public void Start() {
        int numOfEngineResultQueues = systemQueue.getNumOfEngineResultQueues();

        for(int i = 0; i < numOfEngineResultQueues; i++) {
            ClearingEngineWrapper clearingEngineWrapper = new ClearingEngineWrapper(systemQueue,i,DTCC);
            Thread clearingEngine = new Thread(clearingEngineWrapper);
            clearingEngine.start();
        }
    }
}
