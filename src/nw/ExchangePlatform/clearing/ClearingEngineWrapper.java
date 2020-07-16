package nw.ExchangePlatform.clearing;

import nw.ExchangePlatform.clearing.data.DTCCWarehouse;
import nw.ExchangePlatform.trading.data.TradingOutput;
import nw.ExchangePlatform.utility.MessageGenerator;
import nw.ExchangePlatform.commonData.ServerQueue;

import java.util.ArrayList;
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

        HashMap<Integer, ArrayList<String>> userMessagesMap = MessageGenerator.GenerateMessages(output);

        for(Map.Entry<Integer, ArrayList<String>> pair: userMessagesMap.entrySet()) {
            systemServerQueue.PutMessage(pair.getKey(),pair.getValue());
        }
    }

    public void run() {
            try {
                Start();
            } catch (Exception e) {
            }
    }
}
