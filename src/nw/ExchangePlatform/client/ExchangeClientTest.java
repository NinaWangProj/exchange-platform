package nw.ExchangePlatform.client;

import nw.ExchangePlatform.data.Direction;
import nw.ExchangePlatform.data.OrderDuration;
import org.junit.jupiter.api.Test;

public class ExchangeClientTest {

    @Test
    public void ClientTest(){
        ExchangeClient client = new ExchangeClient();
        boolean connected = client.ConnectWithServer();

        Direction direction = Direction.BUY;
        String tickerSymbol = "AAPL";
        int size = 100;
        OrderDuration orderDuration = OrderDuration.DAY;

        client.SubmitMarketOrder(direction,tickerSymbol,size,orderDuration);
    }
}