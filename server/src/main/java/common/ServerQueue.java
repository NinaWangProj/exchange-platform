package common;

import common.utility.BinSelector;
import commonData.DTO.Transferable;
import commonData.Order.MarketParticipantOrder;
import trading.data.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerQueue {
    private final int numberOfOrderQueues;
    private final int numOfEngineResultQueues;

    private LinkedBlockingQueue<MarketParticipantOrder>[] orders;
    private LinkedBlockingQueue<TradingOutput>[] tradingEngineResults;
    private ConcurrentHashMap<Integer,LinkedBlockingQueue<OrderStatus>> sessionOrderStatusMap;
    private ConcurrentHashMap<Integer,LinkedBlockingQueue<Transferable>> sessionResponseDTOMap;

    public ServerQueue(int numberOfOrderQueues, int numOfEngineResultQueues) {
        //each queue will contain ticker symbols with initial char from a subset of 26 alphabetical letters
        this.numberOfOrderQueues = numberOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        orders = new LinkedBlockingQueue[numberOfOrderQueues];
        for (int i = 0; i < numberOfOrderQueues; i ++) {
           orders[i] = new LinkedBlockingQueue<MarketParticipantOrder>();
        }
        tradingEngineResults = new LinkedBlockingQueue[numOfEngineResultQueues];
        for (int j = 0; j < numOfEngineResultQueues; j ++) {
            tradingEngineResults[j] = new LinkedBlockingQueue<TradingOutput>();
        }
        sessionOrderStatusMap = new ConcurrentHashMap<Integer,LinkedBlockingQueue<OrderStatus>>();
        sessionResponseDTOMap = new ConcurrentHashMap<Integer,LinkedBlockingQueue<Transferable>>();
    }

    public void PutOrder(MarketParticipantOrder order) throws Exception {
        String tickerSymbol = order.getTickerSymbol();
        int orderQueueIndex = BinSelector.ChooseAlphabeticalBin(tickerSymbol,getNumberOfOrderQueues());

        orders[orderQueueIndex].put(order);
    }

    public MarketParticipantOrder TakeOrder (int tradingEngineGroupID) throws Exception {
        MarketParticipantOrder order = orders[tradingEngineGroupID].take();
        return order;
    }

    public LinkedBlockingQueue<MarketParticipantOrder>[] getOrders() {
        return orders;
    }

    public int getNumberOfOrderQueues() {
        return numberOfOrderQueues;
    }

    public void PutTradingResult(TradingOutput result) throws Exception {
        Map<Integer,TradingOutput> groupedOutputs = GroupOutputsByUserID(result);

        for(Map.Entry<Integer,TradingOutput> entry : groupedOutputs.entrySet()) {
            int userID = entry.getKey();
            int resultQueueIndex = userID % numOfEngineResultQueues;
            tradingEngineResults[resultQueueIndex].put(entry.getValue());
        }
    }

    private Map<Integer,TradingOutput> GroupOutputsByUserID(TradingOutput result) {
        Map<Integer,TradingOutput> groupedTradingOutputs = new HashMap<Integer,TradingOutput>();
        if(result.Transactions != null) {
            for (Transaction transaction : result.Transactions) {
                int userID = transaction.getUserID();
                if (!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).Transactions.add(transaction);
            }
        }

        if(result.PendingOrders != null) {
            for(PendingOrder pendingOrder : result.PendingOrders) {
                int userID = pendingOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).PendingOrders.add(pendingOrder);
            }
        }

        if(result.UnfilledOrders != null) {
            for(UnfilledOrder unfilledOrder : result.UnfilledOrders) {
                int userID = unfilledOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).UnfilledOrders.add(unfilledOrder);
            }
        }
        return groupedTradingOutputs;
    }

    private Map<Integer,TradingOutput> GroupOutputsBySessionID(TradingOutput result) {
        Map<Integer,TradingOutput> groupedTradingOutputs = new HashMap<Integer,TradingOutput>();
        if(result.Transactions != null) {
            for (Transaction transaction : result.Transactions) {
                int sessionID = transaction.getSessionID();
                if (!groupedTradingOutputs.containsKey(sessionID)) {
                    groupedTradingOutputs.put(sessionID, new TradingOutput());
                }
                groupedTradingOutputs.get(sessionID).Transactions.add(transaction);
            }
        }

        if(result.PendingOrders != null) {
            for(PendingOrder pendingOrder : result.PendingOrders) {
                int userID = pendingOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).PendingOrders.add(pendingOrder);
            }
        }

        if(result.UnfilledOrders != null) {
            for(UnfilledOrder unfilledOrder : result.UnfilledOrders) {
                int userID = unfilledOrder.getUserID();
                if(!groupedTradingOutputs.containsKey(userID)) {
                    groupedTradingOutputs.put(userID, new TradingOutput());
                }
                groupedTradingOutputs.get(userID).UnfilledOrders.add(unfilledOrder);
            }
        }
        return groupedTradingOutputs;
    }

    public int getNumOfEngineResultQueues() {
        return numOfEngineResultQueues;
    }

    public LinkedBlockingQueue<TradingOutput>[] getTradingEngineResults() {
        return tradingEngineResults;
    }

    public TradingOutput TakeTradingOutput (int clearingEngineGroupID) throws Exception {
        TradingOutput output = tradingEngineResults[clearingEngineGroupID].take();
        return output;
    }

    public void RegisterSessionWithQueue(int sessionID) {
        sessionOrderStatusMap.put(sessionID, new LinkedBlockingQueue<OrderStatus>());
        sessionResponseDTOMap.put(sessionID, new LinkedBlockingQueue<Transferable>());
    }

    public void PutOrderStatus(int sessionID, OrderStatus orderStatus) throws Exception{
        sessionOrderStatusMap.get(sessionID).put(orderStatus);
    }

    public OrderStatus TakeOrderStatus(int sessionID) throws Exception{
        OrderStatus orderStatus = sessionOrderStatusMap.get(sessionID).take();
        return orderStatus;
    }

    public void PutResponseDTO(int sessionID, Transferable DTO) throws Exception{
        sessionResponseDTOMap.get(sessionID).put(DTO);
    }

    public Transferable TakeResponseDTO(int sessionID) throws Exception{
        Transferable DTO = sessionResponseDTOMap.get(sessionID).take();
        return DTO;
    }

    public ConcurrentHashMap<Integer, LinkedBlockingQueue<Transferable>> getSessionResponseDTOMap() {
        return sessionResponseDTOMap;
    }
}
