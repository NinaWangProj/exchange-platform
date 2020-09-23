package session;

import commonData.DTO.BookChangeDTO;
import commonData.DTO.MarketDataDTO;
import commonData.DTO.OrderStatusDTO;
import commonData.marketData.MarketDataItem;
import marketData.MarketData;
import marketData.MarketDataWareHouse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import utility.*;

import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.net.Socket;
import java.util.ArrayList;

class ClientSessionTest {

    @Test
    void ProcessesLevel1MarketDataTest() throws Exception{
        Socket clientSocket = mock(Socket.class);
        OrderStatusEventHandler eventHandler = new MockOrderStatusEventHandler();
        MarketDataWareHouse dataWareHouse = new MarketDataWareHouse();

        byte[] byteArray = SampleDTOByteArrayFactory.produceSampleDTOByteArray(ByteArrayType.MarketData_L1);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        when(clientSocket.getInputStream()).thenReturn(inputStream);

        ClientSession session = new ClientSession(clientSocket,eventHandler,dataWareHouse);
        session.Start();

        //Baseline MarketDataWareHouse
        MarketDataWareHouse expectedWareHouse = new MarketDataWareHouse();
        MarketDataDTO marketDataDTO = (MarketDataDTO)SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketData_Level1);
        expectedWareHouse.setMarketData("AAPL",marketDataDTO.getBids(),marketDataDTO.getAsks());
        Assertions.assertThat(dataWareHouse).usingRecursiveComparison().isEqualTo(expectedWareHouse);
    }

    @Test
    void ProcessesL3MarketData_MsgTest() throws Exception{
        Socket clientSocket = mock(Socket.class);
        OrderStatusEventHandler eventHandler = new MockOrderStatusEventHandler();
        MarketDataWareHouse dataWareHouse = new MarketDataWareHouse();

        byte[] byteArray = SampleDTOByteArrayFactory.produceSampleDTOByteArray(ByteArrayType.MarketData_L3_Msg_PartialFillLimitBuyOrder);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        when(clientSocket.getInputStream()).thenReturn(inputStream);

        ClientSession session = new ClientSession(clientSocket,eventHandler,dataWareHouse);
        session.Start();

        Thread.sleep(3000);

        //Baseline MarketDataWareHouse
        MarketDataWareHouse expectedWareHouse = new MarketDataWareHouse();
        MarketDataDTO marketDataDTO = (MarketDataDTO)SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketData_Level3);
        expectedWareHouse.setMarketData("AAPL",marketDataDTO.getBids(),marketDataDTO.getAsks());
        Assertions.assertThat(dataWareHouse).usingRecursiveComparison().isEqualTo(expectedWareHouse);

        //Baseline msg
        OrderStatusDTO msgDTO = (OrderStatusDTO)SampleDTOFactory.ProduceSampleDTO(DTOTestType.Message_PartiallyFilled_LimitBuyOrder);
        MockOrderStatusEventHandler mockEventHandler = (MockOrderStatusEventHandler)eventHandler;
        Assertions.assertThat(mockEventHandler.getRequestID()).usingRecursiveComparison().isEqualTo(msgDTO.getClientRequestID());
        Assertions.assertThat(mockEventHandler.getMsgType()).usingRecursiveComparison().isEqualTo(msgDTO.getStatusType());
        Assertions.assertThat(mockEventHandler.getMsg()).isEqualTo(msgDTO.getMessage());
    }

    @Test
    void ProcessesInsertBookChangesTest() throws Exception{
        Socket clientSocket = mock(Socket.class);
        OrderStatusEventHandler eventHandler = new MockOrderStatusEventHandler();
        MarketDataWareHouse dataWareHouse = new MarketDataWareHouse();

        byte[] byteArray = SampleDTOByteArrayFactory.produceSampleDTOByteArray(ByteArrayType.BookChanges_InsertOperations);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        when(clientSocket.getInputStream()).thenReturn(inputStream);

        ClientSession session = new ClientSession(clientSocket,eventHandler,dataWareHouse);
        session.Start();

        //to wait for session thread finishes executing, then we can resume current thread to compare results
        Thread.sleep(2000);

        //Baseline MarketData
        BookChangeDTO marketDataDTO0 = (BookChangeDTO)SampleDTOFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo0);
        BookChangeDTO marketDataDTO1 = (BookChangeDTO)SampleDTOFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo1);
        MarketDataItem item0 = marketDataDTO0.getBookChanges().get(0).AddedRow;
        MarketDataItem item1 = marketDataDTO1.getBookChanges().get(0).AddedRow;
        ArrayList<MarketDataItem> expectedBids = new ArrayList<>();
        expectedBids.add(new MarketDataItem("GE",item0.getSize(),item0.getPrice()));
        expectedBids.add(new MarketDataItem("GE",item1.getSize(),item1.getPrice()));

        MarketData expectedMarketData = new MarketData("GE", expectedBids,new ArrayList<>());

        Assertions.assertThat(dataWareHouse.getMarketData("GE")).
                usingRecursiveComparison().isEqualTo(expectedMarketData);
    }

}