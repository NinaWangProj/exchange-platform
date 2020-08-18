package server.common;


import commonData.Order.Direction;
import commonData.Order.Info;

import java.util.Date;

public class Transaction implements Info {
    //fields
    private final int sessionID;
    private final int userID;
    private final String name;
    public final long transactionID;
    private final int orderID;
    private final Date time;
    private final Direction direction;
    private final String tickerSymbol;
    private final int size;
    private final double price;


    //constructor
    public Transaction(int sessionID, int userID, String name, long transactionID, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                       double price)
    {
        this.sessionID = sessionID;
        this.userID = userID;
        this.name = name;
        this.transactionID = transactionID;
        this.orderID = orderID;
        this.time = time;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.direction = direction;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public int getOrderID() {
        return orderID;
    }

    public Date getTime() {
        return time;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public int getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public String getReason() {
        return "";
    }

    public int getSessionID() {
        return sessionID;
    }
}
