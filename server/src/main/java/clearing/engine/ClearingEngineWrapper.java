package clearing.engine;

import clearing.data.DTCCWarehouse;
import common.OrderStatus;
import common.TradingOutput;
import common.utility.MessageGenerator;
import common.ServerQueue;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearingEngineWrapper implements Runnable {
    private ServerQueue systemServerQueue;
    private int clearingEngineWrapperIndex;
    private DTCCWarehouse DTCC;


    public ClearingEngineWrapper(ServerQueue systemServerQueue, int clearingEngineWrapperIndex, DTCCWarehouse DTCC) {
        this.systemServerQueue = systemServerQueue;
        this.clearingEngineWrapperIndex = clearingEngineWrapperIndex;
        this.DTCC = DTCC;
    }

    public void Start() throws Exception {
        while(true) {
            ClearingEngine clearingEngine = new ClearingEngine(DTCC);
            TradingOutput output = systemServerQueue.TakeTradingOutput(clearingEngineWrapperIndex);

            clearingEngine.ClearTrade(output.Transactions);

            HashMap<Integer, List<OrderStatus>> userMessagesMap = MessageGenerator.GenerateMessages(output);

            for (Map.Entry<Integer, List<OrderStatus>> pair : userMessagesMap.entrySet()) {
                for(OrderStatus status: pair.getValue())
                    systemServerQueue.PutOrderStatus(pair.getKey(), status);
            }
        }
    }

    public void run() {
            try {
                Start();
            } catch (Exception e) {
                StringBuilder errorString =  new StringBuilder();
                errorString.append("Error in ClearingWrapper with index :" + clearingEngineWrapperIndex);
                errorString.append(System.lineSeparator());
                errorString.append(e.toString());

                for(StackTraceElement elem: e.getStackTrace()){
                    errorString.append(System.lineSeparator());
                    errorString.append(elem.toString());
                }

                System.out.println(errorString.toString());
            }
    }
}
