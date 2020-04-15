package nw.ExchangePlatform.data;

import java.lang.reflect.Array;
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
