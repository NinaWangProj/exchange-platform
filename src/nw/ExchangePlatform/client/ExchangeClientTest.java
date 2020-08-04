package nw.ExchangePlatform.client;

import nw.ExchangePlatform.commonData.Order.Direction;
import nw.ExchangePlatform.commonData.Order.OrderDuration;
import org.junit.jupiter.api.Test;

public class ExchangeClientTest {

    @Test
    public void ClientSubmitOrderTest() throws Exception {
        ExchangeClient client = new ExchangeClient();
        boolean connected = client.ConnectWithServer();

        Direction direction = Direction.BUY;
        String tickerSymbol = "AAPL";
        int size = 100;
        OrderDuration orderDuration = OrderDuration.DAY;

        client.SubmitMarketOrder(direction,tickerSymbol,size,orderDuration);
    }
}