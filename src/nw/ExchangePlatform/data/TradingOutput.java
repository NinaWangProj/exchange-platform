package nw.ExchangePlatform.data;

import java.util.ArrayList;

public class TradingOutput {
    public ArrayList<Transaction> Transaction;
    public ArrayList<UnfilledOrder> UnfilledOrders;

    public TradingOutput(ArrayList<Transaction> Transaction, ArrayList<UnfilledOrder> UnfilledOrders) {
        this.Transaction = Transaction;
        this.UnfilledOrders = UnfilledOrders;
    }

    public TradingOutput() {
    }


}
