package trading.data;

import java.util.ArrayList;

public class TradingOutput {

    public ArrayList<Transaction> Transactions;
    public ArrayList<UnfilledOrder> UnfilledOrders;
    public ArrayList<PendingOrder> PendingOrders;
    public int OrderID;

    public TradingOutput(int OrderID, ArrayList<Transaction> Transactions, ArrayList<UnfilledOrder> UnfilledOrders, ArrayList<PendingOrder> PendingOrders) {
        this.Transactions = Transactions;
        this.UnfilledOrders = UnfilledOrders;
        this.PendingOrders = PendingOrders;
        this.OrderID = OrderID;
    }

    public TradingOutput() {
    }


}
