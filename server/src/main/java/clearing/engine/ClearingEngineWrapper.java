package clearing.engine;

import clearing.data.DTCCWarehouse;
import common.OrderStatus;
import common.TradingOutput;
import common.utility.MessageGenerator;
import common.ServerQueue;


import java.util.HashMap;
import java.util.Map;

public class ClearingEngineWrapper implements Runnable {
    private ServerQueue systemServerQueue;
    private int clearingEngineWrapperIndex;
    private DTCCWarehouse DTCC;


    public ClearingEngineWrapper(ServerQueue systemServerQueue, int clearingEngineWrapperIndex, DTCCWarehouse DTCC) {
        this.systemServerQueue = systemServerQueue;
        this.clearingEngineWrapperIndex = clearingEngineWrapperIndex;
    }

    public void Start() throws Exception {
        ClearingEngine clearingEngine = new ClearingEngine(DTCC);
        TradingOutput output = systemServerQueue.TakeTradingOutput(clearingEngineWrapperIndex);

        clearingEngine.ClearTrade(output.Transactions);

        HashMap<Integer, OrderStatus> userMessagesMap = MessageGenerator.GenerateMessages(output);

        for(Map.Entry<Integer, OrderStatus> pair: userMessagesMap.entrySet()) {
            systemServerQueue.PutOrderStatus(pair.getKey(),pair.getValue());
        }
    }

    public void run() {
            try {
                Start();
            } catch (Exception e) {
            }
    }
}
