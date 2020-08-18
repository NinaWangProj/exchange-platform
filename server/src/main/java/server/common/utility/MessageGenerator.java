package server.common.utility;

import commonData.DataType.OrderStatusType;
import commonData.Order.Info;
import server.common.OrderStatus;
import server.common.TradingOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageGenerator {

    public static HashMap<Integer, OrderStatus> GenerateMessages (TradingOutput tradingOutput) {
        HashMap<Integer, OrderStatus> userOrderStatusMap = new HashMap<Integer, OrderStatus>();

        if (tradingOutput.Transactions.size() >0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, MessageType.TransactionMessage, tradingOutput.Transactions);
        }
        if (tradingOutput.PendingOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, MessageType.PendingOrderMessage, tradingOutput.Transactions);
        }
        if(tradingOutput.UnfilledOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, MessageType.UnfilledOrderMessage, tradingOutput.Transactions);
        }

        return userOrderStatusMap;
    }

    public static void GenerateMessagesPerOutputType (HashMap<Integer, OrderStatus> userOrderStatusMap, MessageType messageType, ArrayList<? extends Info> TradingOutputs) {
        OrderStatusType orderStatusType = null;
        switch (messageType) {
            case TransactionMessage:
                orderStatusType = OrderStatusType.PartiallyFilled;
            case PendingOrderMessage:
                orderStatusType = OrderStatusType.Pending;
            case UnfilledOrderMessage:
                orderStatusType = OrderStatusType.Unfilled;
        }

        for (Info tradingOutput : TradingOutputs) {
            int sessionID = tradingOutput.getSessionID();
            if (!userOrderStatusMap.containsKey(sessionID)) {
                userOrderStatusMap.put(sessionID, new OrderStatus(tradingOutput.getOrderID(), orderStatusType,new ArrayList<String>()));
            }
            String statusMessage = GenerateMessage(messageType,tradingOutput);
            userOrderStatusMap.get(sessionID).getStatusMessages().add(statusMessage);
        }
    }

    public static String GenerateMessage(MessageType messageType, Info tradingOutputInfo) {
        String message = "";
        String userName = tradingOutputInfo.getName();
        String orderID = String.valueOf(tradingOutputInfo.getOrderID());
        String size = String.valueOf(tradingOutputInfo.getSize());
        String tradePrice = String.valueOf(tradingOutputInfo.getPrice());
        String reason = tradingOutputInfo.getReason();

        switch (messageType) {
            case TransactionMessage:
                message = "Congradulation!  " + userName + ", Your order with orderID: " + orderID
                        + " has been filled with: " + size + ", shares, @$" + tradePrice + " per share.";
                break;
            case UnfilledOrderMessage:
                message = "Sorry " + userName + " .Unfortunately your order with orderID: " + orderID
                        + " is not filled for the reason that " + reason;
                break;
            case PendingOrderMessage:
                message = "Dear " + userName + " Your order with orderID: " + orderID
                        + " is in pending now for the resason " + reason;
        }
        return message;
    }

}
