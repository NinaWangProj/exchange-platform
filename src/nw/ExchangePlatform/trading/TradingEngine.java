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

        switch (order.direction) {
            case BUY:
                while(valid) {
                    valid = FillBidOrder(order, transactions, unfilledOrders);
                }
                break;
            case SELL:
                while(valid) {
                    valid = FillAskOrder(order, transactions, unfilledOrders);
                }
        }
        return new TradingOutput(transactions, unfilledOrders);
    }

    private TradingOutput MatchLimitOrder(MarketParticipantOrder order) {
        return new TradingOutput();
    }

    private boolean FillBidOrder(MarketParticipantOrder order, ArrayList<Transaction> transactions, ArrayList<UnfilledOrder> unfilledOrders) {
        boolean active = false;

        if(asks == null  || (order.price <= asks.get(0).price)) {
            UnfilledOrder unfilled = new UnfilledOrder(order, "Could not match market order price");
            unfilledOrders.add(unfilled);
            return active;
        }

        MarketParticipantOrder topLimitSellOrder = asks.get(0);

        if(order.price >= topLimitSellOrder.price) {
            double transactionPrice = topLimitSellOrder.price;
            int transactionSize = 0;

            if(order.size == topLimitSellOrder.size) {
                transactionSize = order.size;
                asks.remove(0);
            } else if (order.size < topLimitSellOrder.size) {
                transactionSize = order.size;
            } else if (order.size > topLimitSellOrder.size) {
                transactionSize = topLimitSellOrder.size;
                order.size -= transactionSize;
                asks.remove(0);
                active = true;
            }

            Transaction buyerTransaction = new Transaction(order.userID, order.name, previousTransactionID +1, order.orderID, new Date(),
                    order.direction, order.tickerSymbol, transactionSize, transactionPrice);
            Transaction sellerTransaction = new Transaction(topLimitSellOrder.userID, topLimitSellOrder.name, previousTransactionID +2, topLimitSellOrder.orderID, new Date(),
                    topLimitSellOrder.direction, topLimitSellOrder.tickerSymbol, transactionSize, transactionPrice);

            transactions.add(buyerTransaction);
            transactions.add(sellerTransaction);
            previousTransactionID += 2;
        }
        return active;
    }

    private boolean FillAskOrder(MarketParticipantOrder order, ArrayList<Transaction> transactions, ArrayList<UnfilledOrder> unfilledOrders) {
        boolean active = false;

        if(bids == null  || (order.price >= bids.get(0).price)) {
            UnfilledOrder unfilled = new UnfilledOrder(order, "Could not match market order price");
            unfilledOrders.add(unfilled);
            return active;
        }

        MarketParticipantOrder topLimitBuyOrder = bids.get(0);

        if(order.price <= topLimitBuyOrder.price) {
            double transactionPrice = topLimitBuyOrder.price;
            int transactionSize = 0;

            if(order.size == topLimitBuyOrder.size) {
                transactionSize = order.size;
                bids.remove(0);
            } else if (order.size < topLimitBuyOrder.size) {
                transactionSize = order.size;
            } else if (order.size > topLimitBuyOrder.size) {
                transactionSize = topLimitBuyOrder.size;
                order.size -= transactionSize;
                bids.remove(0);
                active = true;
            }

            Transaction buyerTransaction = new Transaction(topLimitBuyOrder.userID, topLimitBuyOrder.name, previousTransactionID +1, topLimitBuyOrder.orderID, new Date(),
                    topLimitBuyOrder.direction, topLimitBuyOrder.tickerSymbol, transactionSize, transactionPrice);
            Transaction sellerTransaction = new Transaction(order.userID, order.name, previousTransactionID +2, order.orderID, new Date(),
                    order.direction, order.tickerSymbol, transactionSize, transactionPrice);

            transactions.add(buyerTransaction);
            transactions.add(sellerTransaction);
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

}
