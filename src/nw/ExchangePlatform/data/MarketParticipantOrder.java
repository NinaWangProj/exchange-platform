package nw.ExchangePlatform.data;

import java.util.Date;

public class MarketParticipantOrder implements Comparable<MarketParticipantOrder>, Info {

    //fields
    private final int sessionID;
    private final int userID;
    private final String name;
    private final int orderID;
    private final Date time;
    private final Direction direction;
    private final String tickerSymbol;
    private int size;
    private final double price;
    public final OrderType orderType;
    public final OrderDuration orderDuration;


    //constructor
    public MarketParticipantOrder(int sessionID, int userID, String name, int orderID, Date time, Direction direction, String tickerSymbol, int size,
                                  double price, OrderType orderType, OrderDuration orderDuration)
    {
        this.sessionID = sessionID;
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

    @Override
    public int compareTo(MarketParticipantOrder order2) {
        if(this.getPrice() > order2.getPrice()) {
            return -1;
        } else if (this.getPrice() < order2.getPrice()) {
            return 1;
        } else {
            return this.getTime().compareTo(order2.getTime());
        }
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

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public double getPrice() {
        return price;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getReason() {
        return "";
    }

    public int getSessionID() {
        return sessionID;
    }
}
