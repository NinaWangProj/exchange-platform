package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.clearing.ClearingWarehouse;
import nw.ExchangePlatform.data.*;
import nw.ExchangePlatform.trading.TradingEngine;
import nw.ExchangePlatform.utility.MessageFactory;
import nw.ExchangePlatform.utility.Messenger;
import nw.ExchangePlatform.utility.MessengerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WrapperEngine {
    public static long previousTransactionID;
    private int batchSize;
    private HashMap<String,TradingEngine> tradingEngineMap;
    private ClearingWarehouse clearingWarehouse;

    //constructor
    public WrapperEngine(int batchSize, long previousTransactionID,DTCCWarehouse dtccWarehouse) {
        this.batchSize = batchSize;
        this.previousTransactionID = previousTransactionID;
        this.clearingWarehouse = Initialize(dtccWarehouse);
    }

    private ClearingWarehouse Initialize(DTCCWarehouse dtccWarehouse) {
        //initialize ClearingWarehouse
        return new ClearingWarehouse(dtccWarehouse);
    }

    //methods
    public List<HashMap<String,HashMap<MessengerType,Messenger>>> ProcessOrders(ArrayList<MarketParticipantOrder> Orders) {
        //create new batch for each ticker that is being traded in this stream of orders
        ArrayList<OrderBatch> batches = GroupOrdersIntoBatches(Orders);
        List<HashMap<String,HashMap<MessengerType,Messenger>>> finalMessageMap = null;

        for (OrderBatch batch : batches) {
            HashMap<String,HashMap<MessengerType,Messenger>> messageMap = new HashMap<>();
            messageMap = ProcessOrderBatch(batch);
            finalMessageMap.add(messageMap);
        }

        return finalMessageMap;
    }

    private ArrayList<OrderBatch> GroupOrdersIntoBatches(ArrayList<MarketParticipantOrder> Orders){
        //current implementation: group by tickerSymbol; one ticker Symbol related orders per batch
        HashMap<String,OrderBatch> orderBatchMap = new HashMap<>();
        for (MarketParticipantOrder order : Orders) {
            String tickerSymbol = order.tickerSymbol;
            if (orderBatchMap.containsKey(tickerSymbol)) {
                orderBatchMap.get(tickerSymbol).batch.add(order);
            } else {
                ArrayList<MarketParticipantOrder> orderArrayList = new ArrayList<>();
                orderArrayList.add(order);
                OrderBatch batch = new OrderBatch(tickerSymbol, orderArrayList);
                orderBatchMap.put(tickerSymbol,batch);
            }
        }

        ArrayList<OrderBatch> batches = new ArrayList<>(orderBatchMap.values());
        return batches;
    }

    public HashMap<String,HashMap<MessengerType,Messenger>>  ProcessOrderBatch(OrderBatch orderBatch) {
        if (!tradingEngineMap.containsKey(orderBatch.tickerSymbol)) {
            TradingEngine tradingEngine = new TradingEngine(orderBatch.tickerSymbol);
            tradingEngineMap.put(orderBatch.tickerSymbol,tradingEngine);
        }
        TradingOutput output = tradingEngineMap.get(orderBatch.tickerSymbol).Process(orderBatch.batch);
        clearingWarehouse.ClearTransactions(output.Transactions,clearingWarehouse.dtccWarehouse);

        MessageFactory messageFactory = new MessageFactory(output);
        return messageFactory.SummarizeTradingOutcomePerMarketParticipant();


    }

}