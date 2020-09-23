package session;

import commonData.DTO.*;
import commonData.DataType.MarketDataType;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.clearing.MarketParticipantPortfolio;
import marketData.MarketData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import utility.MockOrderStatusEventHandler;
import utility.MockServer;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExchangeClientTest {
    private ExchangeClient exchangeClient;
    private MockServer server;
    private OrderStatusEventHandler statusEventHandler;

    @Test
    public void API_Test_SubmitMarketOrder() throws Exception{

        //set up exchange client and mock server
        PrepareUnitTest();

        //create order
        OrderDTO marketOrderDTO = new OrderDTO(1,Direction.BUY, OrderType.MARKETORDER,
              "TSLA",250,-1,OrderDuration.GTC);

        //call api: to submit market order
        exchangeClient.SubmitMarketOrder(marketOrderDTO.getDirection(), marketOrderDTO.getTickerSymbol(),
              marketOrderDTO.getSize(),marketOrderDTO.getOrderDuration());

        Thread.sleep(2000);

        //compare
        MockOrderStatusEventHandler eventHandler = (MockOrderStatusEventHandler)statusEventHandler;
        Assertions.assertThat(server.getReceivedDTO()).
              usingRecursiveComparison().isEqualTo(marketOrderDTO);
        Assertions.assertThat((long)1).
                isEqualTo(eventHandler.getRequestID());
        Assertions.assertThat(OrderStatusType.PartiallyFilled).
                usingRecursiveComparison().isEqualTo(eventHandler.getMsgType());
        Assertions.assertThat("Your market Order has been filled with 100 shares @ $300").
                isEqualTo(eventHandler.getMsg());
    }

    @Test
    public void API_Test_SubmitLimitOrder() throws Exception{

        //set up exchange client and mock server
        PrepareUnitTest();

        //create order
        OrderDTO limitOrderDTO = new OrderDTO(1,Direction.BUY, OrderType.LIMITORDER,
                "TSLA",250,500.356,OrderDuration.GTC);

        //call api: to submit limit order
        exchangeClient.SubmitLimitOrder(limitOrderDTO.getDirection(), limitOrderDTO.getTickerSymbol(),
                limitOrderDTO.getSize(),limitOrderDTO.getPrice(), limitOrderDTO.getOrderDuration());

        Thread.sleep(2000);

        //compare
        MockOrderStatusEventHandler eventHandler = (MockOrderStatusEventHandler)statusEventHandler;
        Assertions.assertThat(server.getReceivedDTO()).
                usingRecursiveComparison().isEqualTo(limitOrderDTO);
        Assertions.assertThat((long)1).
                isEqualTo(eventHandler.getRequestID());
        Assertions.assertThat(OrderStatusType.PartiallyFilled).
                usingRecursiveComparison().isEqualTo(eventHandler.getMsgType());
        Assertions.assertThat("Your market Order has been filled with 100 shares @ $300").
                isEqualTo(eventHandler.getMsg());
    }

    @Test
    public void API_SubmitMarketDataRequest() throws Exception{
        PrepareUnitTest();

        //create order
        MarketDataRequestDTO marketDataRequestDTO = new MarketDataRequestDTO((long)1,"AAPL",
                MarketDataType.Level1);

        //call api: to submit request
        MarketData marketData = exchangeClient.SubmitMarketDataRequest(MarketDataType.Level1, "AAPL");

        //compare
        Assertions.assertThat((MarketDataRequestDTO)server.getReceivedDTO()).
                usingRecursiveComparison().isEqualTo(marketDataRequestDTO);
        MarketDataDTO marketDataDTO = (MarketDataDTO) server.getResponseDTO();
        Assertions.assertThat(new MarketData("AAPL", marketDataDTO.getBids(), marketDataDTO.getAsks())).
                usingRecursiveComparison().isEqualTo(marketData);
    }

    @Test
    public void API_SubmitContinuousMarketDataRequest() throws Exception{
        PrepareUnitTest();

        MarketDataRequestDTO marketDataRequestDTO = new MarketDataRequestDTO((long)1,"AAPL",
                MarketDataType.ContinuousLevel3);

        //call api: to submit request
        String returnMsg = exchangeClient.SubmitContinuousMarketDataRequest("AAPL");

        Thread.sleep(2000);
        //compare
        Assertions.assertThat((MarketDataRequestDTO)server.getReceivedDTO()).
                usingRecursiveComparison().isEqualTo(marketDataRequestDTO);
    }

    @Test
    public void API_SubmitPortfolioDataRequest() throws Exception{
        PrepareUnitTest();

        //call api: to submit request
        MarketParticipantPortfolio portfolio = exchangeClient.SubmitPortfolioDataRequest();

        //compare
        PortfolioDTO expectedDTO = (PortfolioDTO)server.getResponseDTO();
        MarketParticipantPortfolio expectedPortfolio = new MarketParticipantPortfolio(expectedDTO.getSecurities(),
                expectedDTO.getCash());
        Assertions.assertThat(expectedPortfolio).
                usingRecursiveComparison().isEqualTo(portfolio);
    }

    private void PrepareUnitTest() throws Exception{
        exchangeClient = new ExchangeClient();

        //assign exchangeClient with mock client socket using reflection
        Socket clientSocket = mock(Socket.class);
        Class<?> metaExchangeClient = exchangeClient.getClass();
        Field field = metaExchangeClient.getDeclaredField("clientSocket");
        field.setAccessible(true);
        field.set(exchangeClient,clientSocket);

        //mock: return byteArrayOutputStream instead of TCP stream
        PipedOutputStream clientOutputStream = new PipedOutputStream();
        PipedInputStream serverInputStream = new PipedInputStream(clientOutputStream);
        when(clientSocket.getOutputStream()).thenReturn(clientOutputStream);

        PipedOutputStream serverOutputStream = new PipedOutputStream();
        PipedInputStream clientInputStream = new PipedInputStream(serverOutputStream);
        //serverOutputStream.connect(clientInputStream);
        when(clientSocket.getInputStream()).thenReturn(clientInputStream);

        //instantiate mock status event handler
        statusEventHandler = new MockOrderStatusEventHandler();

        exchangeClient.SetupClient(statusEventHandler);

        //set up mock server:
        server = new MockServer(serverInputStream, serverOutputStream);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}