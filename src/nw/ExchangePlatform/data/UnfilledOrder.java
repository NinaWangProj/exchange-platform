package nw.ExchangePlatform.data;

import java.util.Date;

public class UnfilledOrder extends MarketParticipantOrder {
    //fields
    public final String reason;

    //constructor
    public UnfilledOrder(MarketParticipantOrder order, String reason) {
        super(order.userID,order.name,order.orderID,order.time,order.direction,order.tickerSymbol,order.size,order.price,order.orderType,order.orderDuration);
        this.reason = reason;
    }

    public UnfilledOrder(int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                         double price, OrderType orderType, OrderDuration duration, String reason)
    {
        super(userID,name,orderID,time,direction,tickerSymbol,size,price,orderType,duration);
        this.reason = reason;
    }
}
