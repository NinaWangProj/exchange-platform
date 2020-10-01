package integration;

import commonData.DataType.MarketDataType;
import commonData.DataType.OrderStatusType;
import marketData.MarketData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import serverEngine.ServerConfig;
import serverEngine.ServerEngine;
import session.ExchangeClient;
import session.OrderStatusEventHandler;
import trading.limitOrderBook.OrderComparatorType;

import java.util.concurrent.atomic.AtomicLong;

public class IntegrationTest {

    @Test
    public void ServerTestIT() throws Exception {

        ServerConfig config = new ServerConfig(5,5,
                58673,0, OrderComparatorType.PriceTimePriority,
                new AtomicLong(0));
        ServerEngine server = new ServerEngine(config);
        server.Start();

        Thread.sleep(5000);
    }

    @Test
    public void SingleClientTestIT() throws Exception {
        ExchangeClient client = new ExchangeClient();
        boolean connected = client.ConnectWithServer();
        OrderStatusEventHandler orderStatusEventHandler = new OrderStatusEventHandler() {
            private long requestID;
            private String msg;

            @Override
            public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg) {
                this.requestID = requestID;
                this.msg = msg;
            }
        };

        client.SetupClient(orderStatusEventHandler);

        Boolean acctCreated = client.SubmitOpenAcctRequest("User1", "user1Password$1");
        System.out.print(acctCreated);

        Boolean loggedin = client.SubmitLoginRequest("User1", "user1Password$1");
        System.out.print(loggedin);

        //MarketParticipantPortfolio portfolio = client.SubmitPortfolioDataRequest();

        MarketData marketData = client.SubmitMarketDataRequest(MarketDataType.Level1, "AAPL");
        Assertions.assertThat(marketData).
                usingRecursiveComparison().isEqualTo(null);
    }
}
