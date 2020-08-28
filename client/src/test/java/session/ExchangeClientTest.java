package session;

import commonData.DTO.OrderDTO;
import commonData.DTO.Transferable;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import utility.DTOTestType;
import utility.MockOrderStatusEventHandler;
import utility.MockServer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExchangeClientTest {

    private ExchangeClient exchangeClient;
    private MockServer server;
    private OrderStatusEventHandler statusEventHandler;
    private Socket clientSocket;

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

        //server does work
        server.Initialize();
        Transferable dto = server.ReadRequestFromClient();
        server.RespondToClient();
        when(clientSocket.getInputStream()).thenReturn(new ByteArrayInputStream(server.getOutputStream().toByteArray()));

        exchangeClient.SetupClient(statusEventHandler);

        //compare
        MockOrderStatusEventHandler eventHandler = (MockOrderStatusEventHandler)statusEventHandler;
        Assertions.assertThat(dto).
              usingRecursiveComparison().isEqualTo(marketOrderDTO);
        Assertions.assertThat((long)1).
                isEqualTo(eventHandler.getRequestID());
        Assertions.assertThat(OrderStatusType.PartiallyFilled).
                usingRecursiveComparison().isEqualTo(eventHandler.getMsgType());
        Assertions.assertThat("Your market Order has been filled with 100 shares @ $300").
                isEqualTo(eventHandler.getMsg());
    }

    private void PrepareUnitTest() throws Exception{

        exchangeClient = new ExchangeClient();

        //assign exchangeClient with mock client socket using reflection
        clientSocket = mock(Socket.class);
        Class<?> metaExchangeClient = exchangeClient.getClass();
        Field field = metaExchangeClient.getDeclaredField("clientSocket");
        field.setAccessible(true);
        field.set(exchangeClient,clientSocket);

        //mock: return byteArrayOutputStream instead of TCP stream
        ByteArrayOutputStream clientOutputStream = new ByteArrayOutputStream();
        when(clientSocket.getOutputStream()).thenReturn(clientOutputStream);

        //instantiate mock status event handler
        statusEventHandler = new MockOrderStatusEventHandler();

        //set up mock server:
        server = new MockServer(clientOutputStream);
    }
}