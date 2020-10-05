package common.utility;

import commonData.DTO.DTOType;
import commonData.DataType.MessageType;
import commonData.DataType.OrderStatusType;
import commonData.Order.Info;
import common.OrderStatus;
import common.TradingOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;

public class MessageGenerator {

    public static HashMap<Integer, List<OrderStatus>> GenerateMessages (TradingOutput tradingOutput) {
        HashMap<Integer, List<OrderStatus>> userOrderStatusMap = new HashMap<>();

        if (tradingOutput.Transactions.size() >0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.PartiallyFilled, tradingOutput.Transactions);
        }
        if (tradingOutput.PendingOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.Pending, tradingOutput.PendingOrders);
        }
        if(tradingOutput.UnfilledOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.Unfilled, tradingOutput.UnfilledOrders);
        }

        return userOrderStatusMap;
    }

    public static void GenerateMessagesPerOutputType (HashMap<Integer, List<OrderStatus>> userOrderStatusMap, OrderStatusType orderStatusType, ArrayList<? extends Info> TradingOutputs) {
        for (Info tradingOutput : TradingOutputs) {
            int sessionID = tradingOutput.getSessionID();
            if (!userOrderStatusMap.containsKey(sessionID)) {
                userOrderStatusMap.put(sessionID, new ArrayList<>());
            }
            String statusMessage = GenerateOrderStatusMessage(orderStatusType,tradingOutput);
            OrderStatus status = new OrderStatus(tradingOutput.getOrderID(), orderStatusType, statusMessage);
            userOrderStatusMap.get(sessionID).add(status);
        }
    }

    public static String GenerateOrderStatusMessage(OrderStatusType orderStatusType, Info tradingOutputInfo) {
        String userName = tradingOutputInfo.getName();
        int orderID = tradingOutputInfo.getOrderID();
        int size = tradingOutputInfo.getSize();
        String tradePrice = String.format(java.util.Locale.US,"%.2f", tradingOutputInfo.getPrice());
        String reason = tradingOutputInfo.getReason();

        JSONObject message = new JSONObject();
        message.put(MessageJSONConstants.name, userName);
        message.put(MessageJSONConstants.orderID, orderID);
        message.put(MessageJSONConstants.tickerSymbol, tradingOutputInfo.getTickerSymbol());

        switch (orderStatusType) {
            case PartiallyFilled:
                message.put(MessageJSONConstants.size, size);
                message.put(MessageJSONConstants.price, tradePrice);
                message.put(MessageJSONConstants.reason, MessageJSONConstants.congrats);
                break;
            case Unfilled:
            case Pending:
                message.put(MessageJSONConstants.reason, reason);
                break;
        }

        return message.toJSONString();
    }

    public static String GenerateStatusMessage(MessageType messageType, DTOType dtoType) {
        String message = "";
        switch (messageType) {
            case SuccessMessage:
                message = dtoType + " completed successfully.";
                break;
            case ErrorMessage:
                message = dtoType + " failed.";
                break;
        }
        return message;
    }
}
