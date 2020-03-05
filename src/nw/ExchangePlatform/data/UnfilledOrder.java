package nw.ExchangePlatform.data;

import java.util.Date;

public class UnfilledOrder {
    //fields
    public final String userID;
    public final String name;
    public final int orderID;
    public final Date time;
    public final Direction direction;
    public final String tickerSymbol;
    public int size;
    public final double price;
    public final OrderType orderType;
    public final String reason;


    //constructor
    public UnfilledOrder(String userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                         double price, OrderType orderType, String reason)
    {
        this.userID = userID;
        this.name = name;
        this.orderID = orderID;
        this.time = time;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.price = price;
        this.orderType = orderType;
        this.direction = direction;
        this.reason = reason;
    }

    public UnfilledOrder(MarketParticipantOrder order, String reason) {
        this.userID = order.userID;
        this.name = order.name;
        this.orderID = order.orderID;
        this.time = order.time;
        this.tickerSymbol = order.tickerSymbol;
        this.size = order.size;
        this.price = order.price;
        this.orderType = order.orderType;
        this.direction = order.direction;
        this.reason = reason;
    }
}
