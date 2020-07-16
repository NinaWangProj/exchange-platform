package nw.ExchangePlatform.trading.data;

import nw.ExchangePlatform.trading.data.PendingOrder;
import nw.ExchangePlatform.trading.data.Transaction;
import nw.ExchangePlatform.trading.data.UnfilledOrder;

import java.util.ArrayList;

public class TradingOutput {

    public ArrayList<Transaction> Transactions;
    public ArrayList<UnfilledOrder> UnfilledOrders;
    public ArrayList<PendingOrder> PendingOrders;

    public TradingOutput(ArrayList<Transaction> Transactions, ArrayList<UnfilledOrder> UnfilledOrders, ArrayList<PendingOrder> PendingOrders) {
        this.Transactions = Transactions;
        this.UnfilledOrders = UnfilledOrders;
        this.PendingOrders = PendingOrders;
    }

    public TradingOutput() {
    }


}
