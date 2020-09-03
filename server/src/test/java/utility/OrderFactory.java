package utility;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;

import java.util.Date;

public class OrderFactory {

    public static MarketParticipantOrder ProduceOrder(int testOrderID, String tickerSymbol, Direction direction) {
        MarketParticipantOrder order = null;
        switch (testOrderID) {
            case 1:
                order = new MarketParticipantOrder(1,101,"user1",
                        1,new Date(), direction, tickerSymbol, 100, 131.40,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 2:
                order = new MarketParticipantOrder(2,102,"user2",
                        2,new Date(), direction, tickerSymbol, 1000, 132.0,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 3:
                order = new MarketParticipantOrder(3,103,"user3",
                        3,new Date(), direction, tickerSymbol, 2, 135.08888854523,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 4:
                order = new MarketParticipantOrder(4,104,"user4",
                        4,new Date(), direction, tickerSymbol, 20, 137.08888854523,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 5:
                order = new MarketParticipantOrder(5,105,"user5",
                        5,new Date(), direction, tickerSymbol, 1, 145.23,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
        }

        return order;
    }
}
