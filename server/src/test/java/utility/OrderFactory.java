package utility;

import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderFactory {

    public static MarketParticipantOrder ProduceOrder(int testOrderID, String tickerSymbol, Direction direction) throws ParseException {
        MarketParticipantOrder order = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        switch (testOrderID) {
            case 1:
                order = new MarketParticipantOrder(1,101,"user1",
                        1, sdf.parse("21/01/2020"), direction, tickerSymbol, 100, 131.40,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 2:
                order = new MarketParticipantOrder(2,102,"user2",
                        2,sdf.parse("21/02/2020"), direction, tickerSymbol, 1000, 132.0,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 3:
                order = new MarketParticipantOrder(3,103,"user3",
                        3,sdf.parse("12/2/2020"), direction, tickerSymbol, 2, 135.08888854523,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 4:
                order = new MarketParticipantOrder(4,104,"user4",
                        4,sdf.parse("12/5/2020"), direction, tickerSymbol, 20, 137.08888854523,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 5:
                order = new MarketParticipantOrder(5,105,"user5",
                        5,sdf.parse("12/2/2020"), direction, tickerSymbol, 1, 145.23,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 6:
                //similar to case 1, with a later date
                order = new MarketParticipantOrder(1,101,"user1",
                        1, sdf.parse("22/01/2020"), direction, tickerSymbol, 100, 131.40,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;
            case 7:
                order = new MarketParticipantOrder(2,102,"user2",
                        2,sdf.parse("21/02/2020"), direction, tickerSymbol, 500, 132.0,
                        OrderType.LIMITORDER, OrderDuration.GTC);
                break;

        }

        return order;
    }
}
