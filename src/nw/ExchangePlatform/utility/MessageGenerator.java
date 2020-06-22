package nw.ExchangePlatform.utility;

import nw.ExchangePlatform.data.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageGenerator {

    public static HashMap<Integer, ArrayList<String>> GenerateMessages (TradingOutput tradingOutput) {
        HashMap<Integer, ArrayList<String>> userMessengerMap = new HashMap<>();

        if (tradingOutput.Transactions.size() >0 ) {
            GenerateMessagesPerOutputType(userMessengerMap, MessageType.TransactionMessage, tradingOutput.Transactions);
        }
        if (tradingOutput.PendingOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userMessengerMap, MessageType.PendingOrderMessage, tradingOutput.Transactions);
        }
        if(tradingOutput.UnfilledOrders.size() > 0 ) {
            GenerateMessagesPerOutputType(userMessengerMap, MessageType.UnfilledOrderMessage, tradingOutput.Transactions);
        }

        return userMessengerMap;
    }

    public static void GenerateMessagesPerOutputType (HashMap<Integer, ArrayList<String>> userMessengerMap, MessageType messageType, ArrayList<? extends Info> TradingOutputs) {
        for (Info tradingOutput : TradingOutputs) {
            int sessionID = tradingOutput.getSessionID();
            if (!userMessengerMap.containsKey(sessionID)) {
                userMessengerMap.put(sessionID, new ArrayList<String>());
            }
            String message = GenerateMessage(messageType,tradingOutput);
            userMessengerMap.get(sessionID).add(message);
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
                message = "Congradulation!  " + userName + " Your order with orderID: " + orderID
                        + " has been filled with " + size + " shares, @$" + tradePrice + " per share.";
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
