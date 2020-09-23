package common.utility;

import commonData.DTO.DTOType;
import commonData.DTO.Transferable;
import commonData.DataType.MessageType;
import commonData.Order.Info;
import common.OrderStatus;
import common.TradingOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageGenerator {

    public static HashMap<Integer, OrderStatus> GenerateMessages (TradingOutput tradingOutput) {
        HashMap<Integer, OrderStatus> userOrderStatusMap = new HashMap<Integer, OrderStatus>();

        if (tradingOutput.Transactions.size() >0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.TransactionMessage, tradingOutput.Transactions);
        }
        if (tradingOutput.PendingOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.PendingOrderMessage, tradingOutput.Transactions);
        }
        if(tradingOutput.UnfilledOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userOrderStatusMap, OrderStatusType.UnfilledOrderMessage, tradingOutput.Transactions);
        }

        return userOrderStatusMap;
    }

    public static void GenerateMessagesPerOutputType (HashMap<Integer, OrderStatus> userOrderStatusMap, OrderStatusType messageType, ArrayList<? extends Info> TradingOutputs) {
        commonData.DataType.OrderStatusType orderStatusType = null;
        switch (messageType) {
            case TransactionMessage:
                orderStatusType = commonData.DataType.OrderStatusType.PartiallyFilled;
                break;
            case PendingOrderMessage:
                orderStatusType = commonData.DataType.OrderStatusType.Pending;
                break;
            case UnfilledOrderMessage:
                orderStatusType = commonData.DataType.OrderStatusType.Unfilled;
                break;
        }

        for (Info tradingOutput : TradingOutputs) {
            int sessionID = tradingOutput.getSessionID();
            if (!userOrderStatusMap.containsKey(sessionID)) {
                userOrderStatusMap.put(sessionID, new OrderStatus(tradingOutput.getOrderID(), orderStatusType,new ArrayList<String>()));
            }
            String statusMessage = GenerateOrderStatusMessage(messageType,tradingOutput);
            userOrderStatusMap.get(sessionID).getStatusMessages().add(statusMessage);
        }
    }

    public static String GenerateOrderStatusMessage(OrderStatusType orderStatusType, Info tradingOutputInfo) {
        String message = "";
        String userName = tradingOutputInfo.getName();
        String orderID = String.valueOf(tradingOutputInfo.getOrderID());
        String size = String.valueOf(tradingOutputInfo.getSize());
        String tradePrice = String.valueOf(tradingOutputInfo.getPrice());
        String reason = tradingOutputInfo.getReason();

        switch (orderStatusType) {
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
                break;
        }
        return message;
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
