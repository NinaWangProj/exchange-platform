package nw.ExchangePlatform.server;

import nw.ExchangePlatform.clearing.ClearingEngineWrapper;
import nw.ExchangePlatform.clearing.data.DTCCWarehouse;
import nw.ExchangePlatform.commonData.ServerQueue;

public class ClearingEngineManager {
    private ServerQueue systemServerQueue;
    private DTCCWarehouse DTCC;

    public ClearingEngineManager(ServerQueue systemServerQueue, DTCCWarehouse DTCC) {
        this.systemServerQueue = systemServerQueue;
        this.DTCC = DTCC;
    }

    public void Start() {
        int numOfEngineResultQueues = systemServerQueue.getNumOfEngineResultQueues();

        for(int i = 0; i < numOfEngineResultQueues; i++) {
            ClearingEngineWrapper clearingEngineWrapper = new ClearingEngineWrapper(systemServerQueue,i,DTCC);
            Thread clearingEngine = new Thread(clearingEngineWrapper);
            clearingEngine.start();
        }
    }
}
