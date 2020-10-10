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

        Thread.sleep(3000);
    }

    @Test
    public void SingleClientTestIT() throws Exception {
        //connect clients with server
        client1 = new ExchangeClient();
        boolean connectedClient1 = client1.ConnectWithServer();
        client2 = new ExchangeClient();
        boolean connectedClient2 = client2.ConnectWithServer();
        client3 = new ExchangeClient();
        boolean connectedClient3 = client3.ConnectWithServer();

        Object client1Monitor = new Object();
        TestEventHandler eventHandler1 = new TestEventHandler(client1Monitor);

        Object client2Monitor = new Object();
        TestEventHandler eventHandler2 = new TestEventHandler(client2Monitor);

        Object client3Monitor = new Object();
        TestEventHandler eventHandler3 = new TestEventHandler(client3Monitor);

        //set up clients
        client1.SetupClient(eventHandler1);
        client2.SetupClient(eventHandler2);
        client3.SetupClient(eventHandler3);

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

        SubmitSerialOrders(client1Monitor,client2Monitor,client3Monitor);
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
    }

    private void SubmitSerialOrders(Object client1Monitor, Object client2Monitor, Object client3Monitor) throws Exception {
        //Three clients submitting orders in serial
        SubmitLimitOrderInSerial(client1,client1Monitor, Direction.BUY,"AAPL",100, 116.01, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client1, client1Monitor, Direction.BUY,"TSLA",1, 425.23, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client2, client2Monitor, Direction.BUY,"AAPL",102, 115.23, OrderDuration.DAY);
        SubmitMarketOrderInSerial(client2, client2Monitor, Direction.BUY,"AAPL",102, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client1, client1Monitor,Direction.BUY,"AAPL",50, 116.50, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client2, client2Monitor,Direction.BUY,"AAPL",2, 115.50, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client3, client3Monitor,Direction.SELL,"AAPL",200, 117.17, OrderDuration.DAY);
        SubmitLimitOrderInSerial(client3, client3Monitor,Direction.SELL,"AAPL",102, 116.97, OrderDuration.DAY);
        SubmitMarketOrderInSerial(client3, client3Monitor,Direction.SELL,"AAPL",102,OrderDuration.DAY);
        SubmitLimitOrderInSerial(client3, client3Monitor,Direction.SELL,"AAPL", 50, 115.50, OrderDuration.DAY);
        SubmitMarketOrderInSerial(client2, client2Monitor,Direction.BUY, "AAPL", 50,OrderDuration.DAY);
        SubmitLimitOrderInSerial(client1,client1Monitor,Direction.BUY, "AAPL", 400,118.23,OrderDuration.DAY);
        SubmitMarketOrderInSerial(client2, client2Monitor,Direction.BUY,"AAPL",102, OrderDuration.DAY);
    }

    private void SubmitMarketOrderInSerial(ExchangeClient client, Object clientMonitor, Direction direction, String tickerSymbol, int size,
                                           OrderDuration duration) throws Exception {
        client.SubmitMarketOrder(direction,tickerSymbol,size,duration);
        synchronized (clientMonitor) {
            clientMonitor.wait();
        }
    }

    private void SubmitLimitOrderInSerial(ExchangeClient client, Object clientMonitor, Direction direction, String tickerSymbol, int size,
                                          double price, OrderDuration duration) throws Exception {
        client.SubmitLimitOrder(direction,tickerSymbol,size,price,duration);
        synchronized (clientMonitor) {
            clientMonitor.wait();
        }
    }
}

class TestEventHandler implements OrderStatusEventHandler {
    private Object monitor;
    private ArrayList<Long> requestIDs;

    public TestEventHandler(Object monitor) {
        this.monitor = monitor;
        requestIDs = new ArrayList<>();
    }

    @Override
    public void On_ReceiveOrderStatusChange(long requestID, OrderStatusType msgType, String msg) throws Exception {
        if (!requestIDs.contains(requestID)) {
            requestIDs.add(requestID);
            synchronized (monitor) {
                if (monitor != null)
                    monitor.notifyAll();
            }
        }
        System.out.print(requestID);
    }
}