package clearing.engine;

import clearing.data.DTCCWarehouse;
import common.ServerQueue;


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
