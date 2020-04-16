package nw.ExchangePlatform.utility;

import nw.ExchangePlatform.data.PendingOrder;
import nw.ExchangePlatform.data.TradingOutput;
import nw.ExchangePlatform.data.Transaction;
import nw.ExchangePlatform.data.UnfilledOrder;

import java.util.HashMap;

public class MessageFactory {
    private final TradingOutput output;

    //constructor
    public MessageFactory(TradingOutput output) {
        this.output = output;
    }

    public HashMap<String,HashMap<MessengerType,Messenger>> SummarizeTradingOutcomePerMarketParticipant () {
        //userID,messengerType,messenger
        HashMap<String,HashMap<MessengerType,Messenger>> userMessengerMap = new HashMap<>();
        if (output.Transactions.size() > 0) {
            for (Transaction transaction : output.Transactions) {
                //check if contains userID
                if (!userMessengerMap.containsKey(transaction.userID)) {
                    userMessengerMap.put(transaction.userID, new HashMap<>());
                }
                //check if contains MessengerType
                if (!userMessengerMap.get(transaction.userID).containsKey(MessengerType.TransactionMessenger)) {
                    TransactionMessenger tranctMessenger = new TransactionMessenger();
                    userMessengerMap.get(transaction.userID).put(MessengerType.TransactionMessenger, tranctMessenger);
                }

                userMessengerMap.get(transaction.userID).get(MessengerType.TransactionMessenger).AddMessages(
                        transaction.userID, transaction.orderID, transaction.tickerSymbol, transaction.size,
                        transaction.tradePrice,"");
            }
        }

        if (output.UnfilledOrders.size() >0) {
            for (UnfilledOrder unfilledOrder : output.UnfilledOrders) {
                //check if contains userID
                if (!userMessengerMap.containsKey(unfilledOrder.userID)) {
                    userMessengerMap.put(unfilledOrder.userID, new HashMap<>());
                }
                //check if contains MessengerType
                if (!userMessengerMap.get(unfilledOrder.userID).containsKey(MessengerType.UnfilledOrderMessenger)) {
                    UnfilledOrderMessenger unfilledOrderMessenger = new UnfilledOrderMessenger();
                    userMessengerMap.get(unfilledOrder.userID).put(MessengerType.UnfilledOrderMessenger, unfilledOrderMessenger);
                }

                userMessengerMap.get(unfilledOrder.userID).get(MessengerType.UnfilledOrderMessenger).AddMessages(
                        unfilledOrder.userID, unfilledOrder.orderID, unfilledOrder.tickerSymbol, unfilledOrder.size,
                        unfilledOrder.price, unfilledOrder.reason);
            }
        }

        if(output.PendingOrders.size() >0) {
            for (PendingOrder pendingOrder : output.PendingOrders) {
                //check if contains userID
                if (!userMessengerMap.containsKey(pendingOrder.userID)) {
                    userMessengerMap.put(pendingOrder.userID, new HashMap<>());
                }
                //check if contains MessengerType
                if (!userMessengerMap.get(pendingOrder.userID).containsKey(MessengerType.PendingOrderMessenger)) {
                    PendingOrderMessenger pendingOrderMessenger = new PendingOrderMessenger();
                    userMessengerMap.get(pendingOrder.userID).put(MessengerType.PendingOrderMessenger, pendingOrderMessenger);
                }

                userMessengerMap.get(pendingOrder.userID).get(MessengerType.UnfilledOrderMessenger).AddMessages(
                        pendingOrder.userID, pendingOrder.orderID, pendingOrder.tickerSymbol, pendingOrder.size,
                        pendingOrder.price, pendingOrder.pendingMessage);
            }
        }
        return userMessengerMap;
    }



}
