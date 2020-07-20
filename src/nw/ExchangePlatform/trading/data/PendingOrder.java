package nw.ExchangePlatform.trading.data;

import nw.ExchangePlatform.commonData.Order.Direction;
import nw.ExchangePlatform.commonData.Order.OrderDuration;
import nw.ExchangePlatform.commonData.Order.OrderType;

import java.util.Date;

public class PendingOrder extends MarketParticipantOrder {
    public String pendingMessage;

    public PendingOrder(MarketParticipantOrder order, String pendingMessage) {
        super(order.getSessionID(),order.getUserID(), order.getName(), order.getOrderID(), order.getTime(), order.getDirection(), order.getTickerSymbol(), order.getSize(), order.getPrice(),order.orderType,order.orderDuration);
        this.pendingMessage = pendingMessage;
    }

    public PendingOrder(int sessionID, int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                        double price, OrderType orderType, OrderDuration duration, String pendingMessage)
    {
        super(sessionID,userID,name,orderID,time,direction,tickerSymbol,size,price,orderType,duration);
        this.pendingMessage = pendingMessage;
    }

    public String getReason() {
        return pendingMessage;
    }
}
