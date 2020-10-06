package integration;

import commonData.DataType.MarketDataType;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.marketData.MarketDataItem;
import marketData.MarketData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import serverEngine.ServerConfig;
import serverEngine.ServerEngine;
import session.ExchangeClient;
import session.OrderStatusEventHandler;
import trading.limitOrderBook.OrderComparatorType;

import java.util.ArrayList;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicLong;

public class IntegrationTest {
    private ExchangeClient client1;
    private ExchangeClient client2;
    private ExchangeClient client3;

    @Test
    public void ServerTestIT() throws Exception {

        ServerConfig config = new ServerConfig(5, 5,
                58673, 0, OrderComparatorType.PriceTimePriority,
                new AtomicLong(0));
        ServerEngine server = new ServerEngine(config);
        server.Start();

        Thread.sleep(60000);
    }

    @Test
    public void SingleClientTestIT() throws Exception {
        //connect clients with server
        ExchangeClient client1 = new ExchangeClient();
        boolean connectedClient1 = client1.ConnectWithServer();
        ExchangeClient client2 = new ExchangeClient();
        boolean connectedClient2 = client2.ConnectWithServer();
        ExchangeClient client3 = new ExchangeClient();
        boolean connectedClient3 = client3.ConnectWithServer();

        Object monitor = new Object();
        TestEventHandler eventHandler = new TestEventHandler(monitor);

        //set up clients
        client1.SetupClient(eventHandler);
        client2.SetupClient(eventHandler);
        client3.SetupClient(eventHandler);

        //Create account for clients
        Boolean acctCreatedClient1 = client1.SubmitOpenAcctRequest("User1", "user1Password$1");
        System.out.print(acctCreatedClient1);
        Boolean acctCreatedClient2 = client2.SubmitOpenAcctRequest("User2", "user2Password$2");
        System.out.print(acctCreatedClient2);
        Boolean acctCreatedClient3 = client3.SubmitOpenAcctRequest("User3", "user3Password$3");
        System.out.print(acctCreatedClient3);

        //clients log in
        Boolean loginClient1 = client1.SubmitLoginRequest("User1", "user1Password$1");
        System.out.print(loginClient1);
        Boolean loginClient2 = client2.SubmitLoginRequest("User2", "user2Password$2");
        System.out.print(loginClient2);
        Boolean loginClient3 = client3.SubmitLoginRequest("User3", "user3Password$3");
        System.out.print(loginClient3);

        //submit orders from diff clients in serial
        client1.SubmitLimitOrder(Direction.BUY,"AAPL",100, 116.01, OrderDuration.DAY);
        client1.SubmitLimitOrder(Direction.BUY,"TSLA",1, 425.23, OrderDuration.DAY);
        client2.SubmitLimitOrder(Direction.BUY,"AAPL",102, 115.23, OrderDuration.DAY);
        client2.SubmitMarketOrder(Direction.BUY,"AAPL",102, OrderDuration.DAY);
        client1.SubmitLimitOrder(Direction.BUY,"AAPL",50, 116.50, OrderDuration.DAY);
        client2.SubmitLimitOrder(Direction.BUY,"AAPL",2, 115.50, OrderDuration.DAY);
        client3.SubmitLimitOrder(Direction.SELL,"AAPL",200, 117.17, OrderDuration.DAY);
        client3.SubmitLimitOrder(Direction.SELL,"AAPL",102, 116.97, OrderDuration.DAY);
        client3.SubmitMarketOrder(Direction.SELL,"AAPL",102,OrderDuration.DAY);
        client3.SubmitLimitOrder(Direction.SELL,"AAPL", 50, 115.50, OrderDuration.DAY);
        client2.SubmitMarketOrder(Direction.BUY, "AAPL", 50,OrderDuration.DAY);
        long requestID = client1.SubmitLimitOrder(Direction.BUY, "AAPL", 400,118.23,OrderDuration.DAY);
        long requestID2 = client2.SubmitMarketOrder(Direction.BUY,"AAPL",102, OrderDuration.DAY);

        synchronized (monitor) {
            monitor.wait();
        }
        MarketData marketData  = client1.SubmitMarketDataRequest(MarketDataType.Level3,"AAPL");

        //expected
        int expectedNumOfAsks = 0;
        int expectedNumOfBids = 2;
        MarketDataItem expectedItem1 = new MarketDataItem("AAPL",148,118.23);
        MarketDataItem expectedItem2 = new MarketDataItem("AAPL",102,115.23);
        ArrayList<MarketDataItem> expectedBids = new ArrayList<>();
        expectedBids.add(expectedItem1);
        expectedBids.add(expectedItem2);

        //compare results
        Assertions.assertThat(expectedNumOfAsks).isEqualTo(marketData.getAsks().size());
        Assertions.assertThat(expectedNumOfBids).isEqualTo(marketData.getBids().size());
        Assertions.assertThat(expectedBids).usingRecursiveComparison().isEqualTo(marketData.getBids());

        Thread.sleep(40000);
    }

    private MarketData SubmitMarketDataRequest() throws Exception {
        //for testing purpose; will let client1 submit market data request
        MarketData marketData = client1.SubmitMarketDataRequest(MarketDataType.Level3, "AAPL");
        return marketData;
    }
}

class TestEventHandler implements OrderStatusEventHandler {
    private Object monitor;
    public TestEventHandler(Object monitor) {
        this.monitor = monitor;
    }

    @Override
    public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg) throws Exception {
        if(requestID == (long)7) {
            monitor.notifyAll();
        }
    }
}