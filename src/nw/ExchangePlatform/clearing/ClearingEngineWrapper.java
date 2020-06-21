package nw.ExchangePlatform.clearing;

import nw.ExchangePlatform.data.DTCCWarehouse;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.utility.MessageGenerator;
import nw.ExchangePlatform.wrapper.Queue;

import java.util.ArrayList;
import java.util.HashMap;

public class ClearingEngineWrapper implements Runnable {
    private Queue systemQueue;
    private int clearingEngineWrapperIndex;
    private DTCCWarehouse DTCC;


    public ClearingEngineWrapper(Queue systemQueue, int clearingEngineWrapperIndex, DTCCWarehouse DTCC) {
        this.systemQueue = systemQueue;
        this.clearingEngineWrapperIndex = clearingEngineWrapperIndex;
    }

    public void Start() throws Exception {
        ClearingEngine clearingEngine = new ClearingEngine(DTCC);
        TradingOutput output = systemQueue.TakeTradingOutput(clearingEngineWrapperIndex);

        clearingEngine.ClearTrade(output.Transactions);

        HashMap<Integer, ArrayList<String>> userMessagesMap = MessageGenerator.GenerateMessages(output);
    }

    public void run() {
            try {
                Start();
            } catch (Exception e) {
            }
    }
}
