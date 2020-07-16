package nw.ExchangePlatform.trading;

import javafx.util.Pair;
import nw.ExchangePlatform.commonData.limitOrderBook.sortedOrderList;
import nw.ExchangePlatform.trading.data.*;
import nw.ExchangePlatform.server.WrapperEngine;

import java.util.ArrayList;
import java.util.Date;

public class TradingEngine{
    //fields
    public final String tickerSymbol;
    sortedOrderList bids;
    sortedOrderList asks;

    //constructor
    public TradingEngine(String tickerSymbol, Pair<sortedOrderList,sortedOrderList> limitOrderBook) {
        this.tickerSymbol = tickerSymbol;
        this.bids = limitOrderBook.getKey();
        this.asks = limitOrderBook.getValue();
    }

    //public methods
    public TradingOutput Process(MarketParticipantOrder order) {
        TradingOutput finalTradingOutput = new TradingOutput();

        TradingOutput output = MatchOrder(order);
        finalTradingOutput.Transactions.addAll(output.Transactions);
        finalTradingOutput.UnfilledOrders.addAll(output.UnfilledOrders);
        finalTradingOutput.PendingOrders.addAll(output.PendingOrders);

        return finalTradingOutput;
    }

    public TradingOutput ProcessBatch(ArrayList<MarketParticipantOrder> orders) {
        TradingOutput finalTradingOutput = new TradingOutput();

        for (MarketParticipantOrder order : orders) {
            TradingOutput output = MatchOrder(order);
            finalTradingOutput.Transactions.addAll(output.Transactions);
            finalTradingOutput.UnfilledOrders.addAll(output.UnfilledOrders);
            finalTradingOutput.PendingOrders.addAll(output.PendingOrders);
        }
        return finalTradingOutput;
    }

    //private methods
    private TradingOutput MatchOrder(MarketParticipantOrder order) {
        boolean valid = true;
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<UnfilledOrder> unfilledOrders = new ArrayList<>();
        ArrayList<PendingOrder> pendingOrders = new ArrayList<>();
        sortedOrderList counterPartyLimitOrderBook = new sortedOrderList();
        sortedOrderList currentLimitOrderBook = new sortedOrderList();

        switch (order.getDirection()) {
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
        return new TradingOutput(transactions, unfilledOrders,pendingOrders);
    }

    private boolean FillMarketOrder(MarketParticipantOrder order, sortedOrderList counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                    ArrayList<UnfilledOrder> unfilledOrders) {
        boolean active;

        if(counterPartyLimitOrderBook == null  || !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
            UnfilledOrder unfilled = new UnfilledOrder(order, "Could not match market order price");
            unfilledOrders.add(unfilled);
            active = false;

        } else {
            MarketParticipantOrder topCounterLimitOrder = counterPartyLimitOrderBook.get(0);

            double transactionPrice = topCounterLimitOrder.getPrice();
            int transactionSize;

            if(order.getSize() == topCounterLimitOrder.getSize()) {
                transactionSize = order.getSize();
                counterPartyLimitOrderBook.remove(0);
                active = false;
            } else if (order.getSize() < topCounterLimitOrder.getSize()) {
                transactionSize = order.getSize();
                active = false;
            } else {
                transactionSize = topCounterLimitOrder.getSize();
                order.setSize(order.getSize() - transactionSize);
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterPartyTransaction = new Transaction(topCounterLimitOrder.getSessionID(),topCounterLimitOrder.getUserID(), topCounterLimitOrder.getName(), WrapperEngine.previousTransactionID +1, topCounterLimitOrder.getOrderID(), new Date(),
                    topCounterLimitOrder.getDirection(), topCounterLimitOrder.getTickerSymbol(), transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.getSessionID(),order.getUserID(), order.getName(), WrapperEngine.previousTransactionID +2, order.getOrderID(), new Date(),
                    order.getDirection(), order.getTickerSymbol(), transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterPartyTransaction);
            WrapperEngine.previousTransactionID += 2;
        }
        return active;
    }

    private boolean FillLimitOrder(MarketParticipantOrder order, sortedOrderList currentLimitOrderBook, sortedOrderList counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                   ArrayList<UnfilledOrder> unfilledOrders, ArrayList<PendingOrder> pendingOrders) {
        boolean active;

        //If no match is found, add order to limit order book, return pending message to participant
        if(counterPartyLimitOrderBook == null  || !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
            //add limit order to limit order book:
            currentLimitOrderBook.add(order);
            //Collections.sort(currentLimitOrderBook);

            PendingOrder pendingOrder = new PendingOrder(order, "Your order has been processed. Will notify you when we find a match");
            pendingOrders.add(pendingOrder);
            active = false;

        } else {
            //If there is a match, match order, put remaining in limit order book if possible
            MarketParticipantOrder topCounterLimitOrder = counterPartyLimitOrderBook.get(0);
            double transactionPrice = topCounterLimitOrder.getPrice();
            int transactionSize;

            if(order.getSize() == topCounterLimitOrder.getSize()) {
                transactionSize = order.getSize();
                counterPartyLimitOrderBook.remove(0);
                active = false;
            } else if (order.getSize() < topCounterLimitOrder.getSize()) {
                transactionSize = order.getSize();
                active = false;
            } else {
                transactionSize = topCounterLimitOrder.getSize();
                order.setSize(order.getSize() - transactionSize);
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterSideTransaction = new Transaction(topCounterLimitOrder.getSessionID(),topCounterLimitOrder.getUserID(), topCounterLimitOrder.getName(), WrapperEngine.previousTransactionID +1, topCounterLimitOrder.getOrderID(), new Date(),
                    topCounterLimitOrder.getDirection(), topCounterLimitOrder.getTickerSymbol(), transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.getSessionID(),order.getUserID(), order.getName(), WrapperEngine.previousTransactionID +2, order.getOrderID(), new Date(),
                    order.getDirection(), order.getTickerSymbol(), transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterSideTransaction);
            WrapperEngine.previousTransactionID += 2;
        }
        return active;
    }

    private boolean CheckTradeViability(MarketParticipantOrder order, MarketParticipantOrder counterLimitOrder) {
        boolean viable;

        if((order.getDirection() == Direction.BUY && order.getPrice() >= counterLimitOrder.getPrice())
            || (order.getDirection() == Direction.SELL && order.getPrice() <= counterLimitOrder.getPrice())) {
            viable = true;
        } else {
            viable = false;
        }
        return viable;
    }

}
