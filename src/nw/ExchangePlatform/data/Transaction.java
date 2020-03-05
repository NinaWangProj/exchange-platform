package nw.ExchangePlatform.data;

import java.util.Date;

public class Transaction {
    //fields
    public final String userID;
    public final String name;
    public final long transactionID;
    public final int orderID;
    public final Date transactionTime;
    public final Direction direction;
    public final String tickerSymbol;
    public final int size;
    public final double tradePrice;


    //constructor
    public Transaction(String userID, String name, long transactionID, int orderID, Date transactionTime, Direction direction, String tickerSymbol, int size,
                       double tradePrice)
    {
        this.userID = userID;
        this.name = name;
        this.transactionID = transactionID;
        this.orderID = orderID;
        this.transactionTime = transactionTime;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.tradePrice = tradePrice;
        this.direction = direction;
    }
}
