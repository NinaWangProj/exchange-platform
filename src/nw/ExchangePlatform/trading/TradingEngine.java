package nw.ExchangePlatform.trading;

import nw.ExchangePlatform.data.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class TradingEngine {

    //fields
    ArrayList<MarketParticipantOrder> bids;
    ArrayList<MarketParticipantOrder> asks;
    long previousTransactionID;

    //constructor
    public TradingEngine(long previousTransactionID) {
        this.previousTransactionID = previousTransactionID;
    }

    //public methods
    public TradingOutput Process(ArrayList<MarketParticipantOrder> orders) {
        TradingOutput finalTradingOutput = new TradingOutput();

        for (MarketParticipantOrder order : orders) {
            TradingOutput output = MatchOrder(order);
            finalTradingOutput.Transaction.addAll(output.Transaction);
        }
        return finalTradingOutput;
    }

    //private methods
    private TradingOutput MatchOrder(MarketParticipantOrder order) {
        boolean valid = true;
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<UnfilledOrder> unfilledOrders = new ArrayList<>();
        ArrayList<PendingOrder> pendingOrders = new ArrayList<>();
        ArrayList<MarketParticipantOrder> counterPartyLimitOrderBook = new ArrayList<>();
        ArrayList<MarketParticipantOrder> currentLimitOrderBook = new ArrayList<>();

        switch (order.direction) {
            case BUY:
                currentLimitOrderBook = bids;
                counterPartyLimitOrderBook = asks;
                break;
            case SELL:
                currentLimitOrderBook = asks;
                counterPartyLimitOrderBook = bids;
        }

        while(valid) {
            switch (order.orderType) {
                case MARKETORDER:
                    valid = FillMarketOrder(order, counterPartyLimitOrderBook, transactions, unfilledOrders);
                    break;
                case LIMITORDER:
                    valid = FillLimitOrder(order, currentLimitOrderBook, counterPartyLimitOrderBook, transactions, unfilledOrders, pendingOrders);
            }
        }
        return new TradingOutput(transactions, unfilledOrders);
    }

    private boolean FillMarketOrder(MarketParticipantOrder order, ArrayList<MarketParticipantOrder> counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                    ArrayList<UnfilledOrder> unfilledOrders) {
        boolean active;

        if(counterPartyLimitOrderBook == null  || !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
            UnfilledOrder unfilled = new UnfilledOrder(order, "Could not match market order price");
            unfilledOrders.add(unfilled);
            active = false;

        } else {
            MarketParticipantOrder topCounterLimitOrder = counterPartyLimitOrderBook.get(0);

            double transactionPrice = topCounterLimitOrder.price;
            int transactionSize;

            if(order.size == topCounterLimitOrder.size) {
                transactionSize = order.size;
                counterPartyLimitOrderBook.remove(0);
                active = false;
            } else if (order.size < topCounterLimitOrder.size) {
                transactionSize = order.size;
                active = false;
            } else {
                transactionSize = topCounterLimitOrder.size;
                order.size -= transactionSize;
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterPartyTransaction = new Transaction(topCounterLimitOrder.userID, topCounterLimitOrder.name, previousTransactionID +1, topCounterLimitOrder.orderID, new Date(),
                    topCounterLimitOrder.direction, topCounterLimitOrder.tickerSymbol, transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.userID, order.name, previousTransactionID +2, order.orderID, new Date(),
                    order.direction, order.tickerSymbol, transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterPartyTransaction);
            previousTransactionID += 2;
        }
        return active;
    }

    private boolean FillLimitOrder(MarketParticipantOrder order, ArrayList<MarketParticipantOrder> currentLimitOrderBook, ArrayList<MarketParticipantOrder> counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                   ArrayList<UnfilledOrder> unfilledOrders, ArrayList<PendingOrder> pendingOrders) {
        boolean active;

        //If no match is found, add order to limit order book, return pending message to participant
        if(counterPartyLimitOrderBook == null  || !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
            //add limit order to limit order book:
            currentLimitOrderBook.add(order);
            Collections.sort(currentLimitOrderBook);

            PendingOrder pendingOrder = new PendingOrder(order, "Your order has been processed. Will notify you when we find a match");
            pendingOrders.add(pendingOrder);
            active = false;

        } else {
            //If there is a match, match order, put remaining in limit order book if possible
            MarketParticipantOrder topCounterLimitOrder = counterPartyLimitOrderBook.get(0);
            double transactionPrice = topCounterLimitOrder.price;
            int transactionSize;

            if(order.size == topCounterLimitOrder.size) {
                transactionSize = order.size;
                counterPartyLimitOrderBook.remove(0);
                active = false;
            } else if (order.size < topCounterLimitOrder.size) {
                transactionSize = order.size;
                active = false;
            } else {
                transactionSize = topCounterLimitOrder.size;
                order.size -= transactionSize;
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterSideTransaction = new Transaction(topCounterLimitOrder.userID, topCounterLimitOrder.name, previousTransactionID +1, topCounterLimitOrder.orderID, new Date(),
                    topCounterLimitOrder.direction, topCounterLimitOrder.tickerSymbol, transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.userID, order.name, previousTransactionID +2, order.orderID, new Date(),
                    order.direction, order.tickerSymbol, transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterSideTransaction);
            previousTransactionID += 2;
        }
        return active;
    }

    private boolean CheckTradeViability(MarketParticipantOrder order, MarketParticipantOrder counterLimitOrder) {
        boolean viable;

        if((order.direction == Direction.BUY && order.price >= counterLimitOrder.price)
            || (order.direction == Direction.SELL && order.price <= counterLimitOrder.price)) {
            viable = true;
        } else {
            viable = false;
        }
        return viable;
    }

}