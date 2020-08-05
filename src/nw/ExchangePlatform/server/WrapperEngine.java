package nw.ExchangePlatform.server;

import nw.ExchangePlatform.clearing.ClearingWarehouse;
import nw.ExchangePlatform.clearing.data.DTCCWarehouse;
import nw.ExchangePlatform.trading.TradingEngine;
import nw.ExchangePlatform.trading.data.MarketParticipantOrder;
import nw.ExchangePlatform.trading.data.OrderBatch;
import nw.ExchangePlatform.trading.data.OrderStatus;
import nw.ExchangePlatform.trading.data.TradingOutput;
import nw.ExchangePlatform.utility.MessageGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WrapperEngine {
    public static long previousTransactionID;
    private int batchSize;
    private HashMap<String,TradingEngine> tradingEngineMap;
    private ClearingWarehouse clearingWarehouse;

    //constructor
    public WrapperEngine(int batchSize, long previousTransactionID, DTCCWarehouse dtccWarehouse) {
        this.batchSize = batchSize;
        this.previousTransactionID = previousTransactionID;
        this.clearingWarehouse = Initialize(dtccWarehouse);
    }

    private ClearingWarehouse Initialize(DTCCWarehouse dtccWarehouse) {
        //initialize ClearingWarehouse
        return new ClearingWarehouse(dtccWarehouse);
    }

    //methods
    public List<HashMap<Integer, OrderStatus>> ProcessOrders(ArrayList<MarketParticipantOrder> Orders) throws Exception {
        //create new batch for each ticker that is being traded in this stream of orders
        ArrayList<OrderBatch> batches = GroupOrdersIntoBatches(Orders);
        List<HashMap<Integer, OrderStatus>> finalMessageMap = null;

        for (OrderBatch batch : batches) {
            HashMap<Integer, OrderStatus> messageMap = new HashMap<>();
            messageMap = ProcessOrderBatch(batch);
            finalMessageMap.add(messageMap);
        }

        return finalMessageMap;
    }

    private ArrayList<OrderBatch> GroupOrdersIntoBatches(ArrayList<MarketParticipantOrder> Orders){
        //current implementation: group by tickerSymbol; one ticker Symbol related orders per batch
        HashMap<String,OrderBatch> orderBatchMap = new HashMap<>();
        for (MarketParticipantOrder order : Orders) {
            String tickerSymbol = order.getTickerSymbol();
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

    public HashMap<Integer, OrderStatus> ProcessOrderBatch(OrderBatch orderBatch) throws Exception {
        if (!tradingEngineMap.containsKey(orderBatch.tickerSymbol)) {
            //TradingEngine tradingEngine = new TradingEngine(orderBatch.tickerSymbol);
            //tradingEngineMap.put(orderBatch.tickerSymbol,tradingEngine);
        }
        TradingOutput output = tradingEngineMap.get(orderBatch.tickerSymbol).ProcessBatch(orderBatch.batch);
        clearingWarehouse.ClearTransactions(output.Transactions,clearingWarehouse.dtccWarehouse);

        HashMap<Integer, OrderStatus> userMessagesMap = MessageGenerator.GenerateMessages(output);
        return userMessagesMap;
    }
}
