package nw.ExchangePlatform.trading;

import nw.ExchangePlatform.data.*;

import java.util.ArrayList;
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

    public TradingOutput Process(ArrayList<MarketParticipantOrder> orders) {
        TradingOutput finalTradingOutput = new TradingOutput();

        for (MarketParticipantOrder order : orders) {
            TradingOutput output = new TradingOutput();
            switch (order.orderType) {
                case MARKETORDER:
                    output = MatchMarketOrder(order);
                    break;
                case LIMITORDER:
                    output = MatchLimitOrder(order);
            }
            finalTradingOutput.Transaction.addAll(output.Transaction);
        }
        return finalTradingOutput;
    }

    private TradingOutput MatchMarketOrder(MarketParticipantOrder order) {
        boolean valid = true;
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<UnfilledOrder> unfilledOrders = new ArrayList<>();
        ArrayList<MarketParticipantOrder> counterPartyLimitOrderBook = new ArrayList<>();

        switch (order.direction) {
            case BUY:
                counterPartyLimitOrderBook = asks;
                break;
            case SELL:
                counterPartyLimitOrderBook = bids;
        }

        while(valid) {
            valid = FillOrder(order, counterPartyLimitOrderBook, transactions, unfilledOrders);
        }

        return new TradingOutput(transactions, unfilledOrders);
    }

    private TradingOutput MatchLimitOrder(MarketParticipantOrder order) {
        return new TradingOutput();
    }

    private boolean FillOrder(MarketParticipantOrder order, ArrayList<MarketParticipantOrder> counterPartyLimitOrderBook, ArrayList<Transaction> transactions, ArrayList<UnfilledOrder> unfilledOrders) {
        boolean active = false;

        if(counterPartyLimitOrderBook == null  || !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
            UnfilledOrder unfilled = new UnfilledOrder(order, "Could not match market order price");
            unfilledOrders.add(unfilled);
            return active;
        }

        MarketParticipantOrder topCounterLimitOrder = counterPartyLimitOrderBook.get(0);
        boolean foundCounterParticipant = CheckTradeViability(order, counterPartyLimitOrderBook.get(0));

        if(foundCounterParticipant) {
            double transactionPrice = topCounterLimitOrder.price;
            int transactionSize;

            if(order.size == topCounterLimitOrder.size) {
                transactionSize = order.size;
                counterPartyLimitOrderBook.remove(0);
            } else if (order.size < topCounterLimitOrder.size) {
                transactionSize = order.size;
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

    private TransactionType CheckTransactionType(MarketParticipantOrder order, int originalOrderSize) {
        TransactionType transactionType;
        if(order.size < originalOrderSize)
            transactionType = TransactionType.PARTIALFILLED;
        else if(order.size == 0)
            transactionType = TransactionType.ALLFILLED;
        else
            transactionType = TransactionType.NONEFILLED;

        return transactionType;
    }

    private void ReturnOrderToMarketParticipant() {

    }

    private boolean CheckTradeViability(MarketParticipantOrder order, MarketParticipantOrder counterLimitOrder) {
        boolean viable = false;

        if((order.direction == Direction.BUY && order.price >= counterLimitOrder.price)
            || (order.direction == Direction.SELL && order.price <= counterLimitOrder.price)) {
            viable = true;
        }
        return viable;
    }

}
