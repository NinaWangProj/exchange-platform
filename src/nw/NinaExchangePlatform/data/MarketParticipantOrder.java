package nw.NinaExchangePlatform.data;

public class MarketParticipantOrder {

    //fields
    String userID;
    String name;
    OrderDirection direction;
    String tickerSymbol;
    int size;
    double parValue;
    OrderType orderType;
    OrderDuration orderDuration;

    //constructor
    public MarketParticipantOrder(String userID, String name, OrderDirection direction, String tickerSymbol, int size,
                                     double parValue, OrderType orderType, OrderDuration orderDuration)
    {
        this.userID = userID;
        this.name = name;
        this.tickerSymbol = tickerSymbol;
        this.size = size;
        this.parValue = parValue;
        this.orderType = orderType;
        this.orderDuration = orderDuration;
    }
}
