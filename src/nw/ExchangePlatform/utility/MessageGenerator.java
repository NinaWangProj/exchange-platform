package nw.ExchangePlatform.utility;

import nw.ExchangePlatform.data.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageGenerator {

    public static HashMap<String, ArrayList<String>> GenerateMessages (TradingOutput output) {
        HashMap<String,ArrayList<String>> userMessengerMap = new HashMap<>();
        if (output.Transactions.size() > 0) {
            for (Transaction transaction : output.Transactions) {
                //check if contains userID
                if (!userMessengerMap.containsKey(transaction.userID)) {
                    userMessengerMap.put(transaction.userID, new ArrayList<String>());
                }
                String message = GenerateMessage(MessageType.TransactionMessage, transaction.userID, transaction.orderID, transaction.tickerSymbol, transaction.size,
                        transaction.tradePrice,"");
                userMessengerMap.get(transaction.userID).add(message);
            }
        }

        if (output.UnfilledOrders.size() >0) {
            for (UnfilledOrder unfilledOrder : output.UnfilledOrders) {
                //check if contains userID
                if (!userMessengerMap.containsKey(unfilledOrder.userID)) {
                    userMessengerMap.put(unfilledOrder.userID, new ArrayList<String>());
                }
                String message = GenerateMessage(MessageType.UnfilledOrderMessage, unfilledOrder.userID, unfilledOrder.orderID,
                        unfilledOrder.tickerSymbol, unfilledOrder.size,
                        unfilledOrder.price, unfilledOrder.reason);
                userMessengerMap.get(unfilledOrder.userID).add(message);
            }
        }

        if(output.PendingOrders.size() >0) {
            for (PendingOrder pendingOrder : output.PendingOrders) {
                //check if contains userID
                if (!userMessengerMap.containsKey(pendingOrder.userID)) {
                    userMessengerMap.put(pendingOrder.userID, new ArrayList<String>());
                }
                String message = GenerateMessage(MessageType.PendingOrderMessage,
                        pendingOrder.userID, pendingOrder.orderID, pendingOrder.tickerSymbol, pendingOrder.size,
                        pendingOrder.price, pendingOrder.pendingMessage);
                userMessengerMap.get(pendingOrder.userID).add(message);
            }
        }
        return userMessengerMap;
    }

    public static String GenerateMessage(MessageType messageType,String userName,int orderID,String tickerSymbol,
                                         int size, double tradePrice, String reason) {
        String message = "";
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
