package trading.workflow;


import common.TradingOutput;
import common.Transaction;
import commonData.Order.MarketParticipantOrder;
import javafx.util.Pair;
import commonData.Order.Direction;
import common.SortedOrderList;
import trading.data.*;


import java.util.ArrayList;
import java.util.Date;

public class TradingEngine{
    //fields
    public final String tickerSymbol;
    private SortedOrderList bids;
    private SortedOrderList asks;


    //constructor
    public TradingEngine(String tickerSymbol, Pair<SortedOrderList, SortedOrderList> limitOrderBook) {
        this.tickerSymbol = tickerSymbol;
        this.bids = limitOrderBook.getKey();
        this.asks = limitOrderBook.getValue();
    }

    //public methods
    public TradingOutput Process(MarketParticipantOrder order) throws Exception {
        TradingOutput finalTradingOutput = new TradingOutput(order.getOrderID());

        TradingOutput output = MatchOrder(order);
        finalTradingOutput.Transactions.addAll(output.Transactions);
        finalTradingOutput.UnfilledOrders.addAll(output.UnfilledOrders);
        finalTradingOutput.PendingOrders.addAll(output.PendingOrders);

        return finalTradingOutput;
    }

    // TradingOutput can no longer be result of a batch of orders, only a single order
    @Deprecated
    public TradingOutput ProcessBatch(ArrayList<MarketParticipantOrder> orders) throws Exception{
        TradingOutput finalTradingOutput = new TradingOutput(-1);

        for (int i = 0; i< orders.size(); i++) {
            TradingOutput output = MatchOrder(orders.get(i));
            finalTradingOutput.Transactions.addAll(output.Transactions);
            finalTradingOutput.UnfilledOrders.addAll(output.UnfilledOrders);
            finalTradingOutput.PendingOrders.addAll(output.PendingOrders);
        }

        /*for (MarketParticipantOrder order : orders) {
            TradingOutput output = MatchOrder(order);
            finalTradingOutput.Transactions.addAll(output.Transactions);
            finalTradingOutput.UnfilledOrders.addAll(output.UnfilledOrders);
            finalTradingOutput.PendingOrders.addAll(output.PendingOrders);
        }*/
        return finalTradingOutput;
    }

    //private methods
    private TradingOutput MatchOrder(MarketParticipantOrder order) throws Exception {
        boolean valid = true;
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        ArrayList<UnfilledOrder> unfilledOrders = new ArrayList<UnfilledOrder>();
        ArrayList<PendingOrder> pendingOrders = new ArrayList<PendingOrder>();
        SortedOrderList counterPartyLimitOrderBook;
        SortedOrderList currentLimitOrderBook ;

        switch (order.getDirection()) {
            case BUY:
                currentLimitOrderBook = bids;
                counterPartyLimitOrderBook = asks;
                break;
            case SELL:
                currentLimitOrderBook = asks;
                counterPartyLimitOrderBook = bids;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + order.getDirection());
        }

        while(valid) {
            switch (order.getOrderType()) {
                case MARKETORDER:
                    valid = FillMarketOrder(order, counterPartyLimitOrderBook, transactions, unfilledOrders);
                    break;
                case LIMITORDER:
                    valid = FillLimitOrder(order, currentLimitOrderBook, counterPartyLimitOrderBook, transactions, unfilledOrders, pendingOrders);
                    break;
            }
        }
        return new TradingOutput(order.getOrderID(), transactions, unfilledOrders,pendingOrders);
    }

    private boolean FillMarketOrder(MarketParticipantOrder order, SortedOrderList counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                    ArrayList<UnfilledOrder> unfilledOrders) throws Exception {
        boolean active;

        if(counterPartyLimitOrderBook == null  || counterPartyLimitOrderBook.size() == 0) {
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
                topCounterLimitOrder.setSize(topCounterLimitOrder.getSize() - transactionSize);
                active = false;
            } else {
                transactionSize = topCounterLimitOrder.getSize();
                order.setSize(order.getSize() - transactionSize);
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterPartyTransaction = new Transaction(topCounterLimitOrder.getSessionID(),topCounterLimitOrder.getUserID(),
                    topCounterLimitOrder.getName(), TradingEngineManager.getNewTransactionID(), topCounterLimitOrder.getOrderID(), new Date(),
                    topCounterLimitOrder.getDirection(), topCounterLimitOrder.getTickerSymbol(), transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.getSessionID(),order.getUserID(), order.getName(), TradingEngineManager.getTransactionID(), order.getOrderID(), new Date(),
                    order.getDirection(), order.getTickerSymbol(), transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterPartyTransaction);
        }
        return active;
    }

    private boolean FillLimitOrder(MarketParticipantOrder order, SortedOrderList currentLimitOrderBook, SortedOrderList counterPartyLimitOrderBook, ArrayList<Transaction> transactions,
                                   ArrayList<UnfilledOrder> unfilledOrders, ArrayList<PendingOrder> pendingOrders) throws Exception{
        boolean active;

        //If no match is found, add order to limit order book, return pending message to participant
        if(counterPartyLimitOrderBook == null  || counterPartyLimitOrderBook.size() == 0 ||
                !CheckTradeViability(order, counterPartyLimitOrderBook.get(0))) {
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
            int counterOrderSize = topCounterLimitOrder.getSize();
            int transactionSize;

            if(order.getSize() == topCounterLimitOrder.getSize()) {
                transactionSize = order.getSize();
                counterPartyLimitOrderBook.remove(0);
                active = false;
            } else if (order.getSize() < counterOrderSize) {
                transactionSize = order.getSize();
                int updatedCounterOrderSize = counterOrderSize-transactionSize;
                counterPartyLimitOrderBook.modify(0,"size",updatedCounterOrderSize);
                active = false;
            } else {
                transactionSize = counterOrderSize;
                order.setSize(order.getSize() - transactionSize);
                counterPartyLimitOrderBook.remove(0);
                active = true;
            }

            Transaction counterSideTransaction = new Transaction(topCounterLimitOrder.getSessionID(),topCounterLimitOrder.getUserID(), topCounterLimitOrder.getName(), TradingEngineManager.getNewTransactionID(), topCounterLimitOrder.getOrderID(), new Date(),
                    topCounterLimitOrder.getDirection(), topCounterLimitOrder.getTickerSymbol(), transactionSize, transactionPrice);
            Transaction currentOrderTransaction = new Transaction(order.getSessionID(),order.getUserID(), order.getName(), TradingEngineManager.getTransactionID(), order.getOrderID(), new Date(),
                    order.getDirection(), order.getTickerSymbol(), transactionSize, transactionPrice);

            transactions.add(currentOrderTransaction);
            transactions.add(counterSideTransaction);
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
