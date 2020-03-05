package nw.ExchangePlatform.data;

import java.util.Date;

public class MarketParticipantOrder {

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
    public final OrderDuration orderDuration;


    //constructor
    public MarketParticipantOrder(String userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                                  double price, OrderType orderType, OrderDuration orderDuration)
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
        this.orderDuration = orderDuration;
    }
}
