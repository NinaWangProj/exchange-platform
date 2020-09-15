package session;

import clearing.data.CredentialWareHouse;
import common.LimitOrderBookWareHouse;
import common.ServerQueue;
import common.SortedOrderList;
import common.utility.MessageGenerator;
import commonData.DTO.*;
import commonData.DataType.MessageType;
import commonData.Order.Direction;
import commonData.Order.MarketParticipantOrder;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;
import commonData.marketData.MarketDataItem;
import javafx.util.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import trading.limitOrderBook.OrderComparatorType;
import utility.ByteArrayType;
import utility.DTOTestType;
import utility.SampleDTOByteArrayFactory;
import utility.SampleDTOFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SessionTest {
    private ServerQueue queue;
    private CredentialWareHouse credentialWareHouse;
    private ByteArrayOutputStream outputStream;

    @Test
    public void On_ReceivingOpenAcctRequest_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.OpenAcctRequest);
        session.RunCurrentSession();

        //Baseline
        SampleDTOFactory dtoFactory = new SampleDTOFactory();
        OpenAcctDTO expectedOpenAcctDTO = (OpenAcctDTO)dtoFactory.ProduceSampleDTO(DTOTestType.OpenAcctRequest_user1);
        Integer expectedUserID = 1;
        MarketParticipantPortfolio expectedPortfolio = new MarketParticipantPortfolio();
        MessageDTO expectedMsgDTO = new MessageDTO((long)1, MessageType.SuccessMessage,
                MessageGenerator.GenerateStatusMessage(MessageType.SuccessMessage, DTOType.OpenAcctRequest));

        Thread.sleep(1000);

        //retrieve response MessageDTO
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] DTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(DTOByteArray, 0, byteSizeOfDTO);
        MessageDTO msgDTO = MessageDTO.Deserialize(DTOByteArray);

        //compare username
        String userName = credentialWareHouse.getLoginCredentialMap().keySet().iterator().next();
        Integer userID = credentialWareHouse.GetUserID(userName);
        Assertions.assertThat(expectedOpenAcctDTO.getUserName()).isEqualTo(userName);
        //compare password
        Assertions.assertThat(expectedUserID).isEqualTo(userID);
        Assertions.assertThat(expectedPortfolio).
                usingRecursiveComparison().isEqualTo(session.getPortfolioHashMap().get(1));
        Assertions.assertThat(expectedMsgDTO).
                usingRecursiveComparison().isEqualTo(msgDTO);
    }

    @Test
    public void On_ReceivingLoginRequest_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.LoginRequest_user1);
        session.RunCurrentSession();

        //Baseline
        SampleDTOFactory dtoFactory = new SampleDTOFactory();
        LoginDTO expectedLoginRequest = (LoginDTO)dtoFactory.ProduceSampleDTO(DTOTestType.LoginRequest_user1);
        MessageDTO expectedMsgDTO = new MessageDTO((long)101, MessageType.SuccessMessage,
                MessageGenerator.GenerateStatusMessage(MessageType.SuccessMessage, DTOType.LoginRequest));

        Thread.sleep(1000);

        //retrieve response MessageDTO
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] DTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(DTOByteArray, 0, byteSizeOfDTO);
        MessageDTO msgDTO = MessageDTO.Deserialize(DTOByteArray);

        Assertions.assertThat(expectedLoginRequest.getUserName()).isEqualTo(session.getClientUserName());
        Assertions.assertThat(1).isEqualTo(session.getClientUserID());
        Assertions.assertThat(expectedMsgDTO).
                usingRecursiveComparison().isEqualTo(msgDTO);
    }

    @Test
    public void On_ReceivingLoginRequest_WrongLogin_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.LoginRequest_user2);
        session.RunCurrentSession();

        //Baseline
        MessageDTO expectedMsgDTO = new MessageDTO((long)101, MessageType.ErrorMessage,
                MessageGenerator.GenerateStatusMessage(MessageType.ErrorMessage, DTOType.LoginRequest));

        Thread.sleep(2000);

        //retrieve response MessageDTO
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] DTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(DTOByteArray, 0, byteSizeOfDTO);
        MessageDTO msgDTO = MessageDTO.Deserialize(DTOByteArray);

        Assertions.assertThat(expectedMsgDTO).
                usingRecursiveComparison().isEqualTo(msgDTO);
    }

    @Test
    public void On_ReceivingOrderDTO_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.OrderDTO_Limit_Buy);
        session.RunCurrentSession();

        //Baseline
        SampleDTOFactory dtoFactory = new SampleDTOFactory();
        OrderDTO expectedOrderDTO = (OrderDTO)dtoFactory.ProduceSampleDTO(DTOTestType.Order_LimitOrder_Buy);
        MarketParticipantOrder expectedOrder = new MarketParticipantOrder(0,1,"user1",0,new Date(),
                expectedOrderDTO.getDirection(),expectedOrderDTO.getTickerSymbol(),expectedOrderDTO.getSize(),
                expectedOrderDTO.getPrice(),expectedOrderDTO.getOrderType(),expectedOrderDTO.getOrderDuration());

        Thread.sleep(1000);

        //compare
        Assertions.assertThat(expectedOrder).
                usingRecursiveComparison().ignoringFields("Date").isEqualTo(queue.getOrders()[0].take());
    }

    @Test
    public void On_ReceivingPortfolioRequest_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.PortfolioRequest);
        session.RunCurrentSession();

        //Baseline
        MarketParticipantPortfolio portfolio = GenerateSamplePortfolios().get(1);
        PortfolioDTO expectedPortfolioDTO = new PortfolioDTO((long)105, portfolio.getSecurities(), portfolio.getCashAmt());

        Thread.sleep(2000);

        //retrieve PortfoliDTO
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] msgDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(msgDTOByteArray, 0, byteSizeOfDTO);
        dtoType = DTOType.valueOf(inputStream.read());
        byteSizeOfDTO = inputStream.read();
        byte[] portfolioDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(portfolioDTOByteArray, 0, byteSizeOfDTO);

        PortfolioDTO DTO = PortfolioDTO.Deserialize(portfolioDTOByteArray);

        //Compare
        Assertions.assertThat(expectedPortfolioDTO).
                usingRecursiveComparison().isEqualTo(DTO);
    }

    @Test
    public void On_ReceivingMarketDataRequest_L1_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.MarketDataRequest_L1);
        session.RunCurrentSession();

        //Baseline
        ArrayList<MarketDataItem> expectedBids = new ArrayList<>();
        ArrayList<MarketDataItem> expectedAsks = new ArrayList<>();
        expectedBids.add(new MarketDataItem("AAPL",1000,117.32));
        expectedAsks.add(new MarketDataItem("AAPL",1000,118.32));

        MarketDataDTO expectedDTO = new MarketDataDTO((long)103,"AAPL", expectedBids,expectedAsks);

        Thread.sleep(1000);

        //retrieve market data
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] msgDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(msgDTOByteArray, 0, byteSizeOfDTO);
        dtoType = DTOType.valueOf(inputStream.read());
        byteSizeOfDTO = inputStream.read();
        byte[] marketDataDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(marketDataDTOByteArray, 0, byteSizeOfDTO);
        MarketDataDTO DTO = MarketDataDTO.Deserialize(marketDataDTOByteArray);

        //Compare
        Assertions.assertThat(expectedDTO).
                usingRecursiveComparison().isEqualTo(DTO);
    }

    @Test
    public void On_ReceivingMarketDataRequest_L3_Test() throws Exception {
        //prepare unit test: set up session and returns session object
        Session session = Prepare(ByteArrayType.MarketDataRequest_L3);
        session.RunCurrentSession();

        //Baseline
        ArrayList<MarketDataItem> expectedBids = new ArrayList<>();
        ArrayList<MarketDataItem> expectedAsks = new ArrayList<>();
        expectedBids.add(new MarketDataItem("AAPL",1000,117.32));
        expectedBids.add(new MarketDataItem("AAPL",23,116.3286954));
        expectedBids.add(new MarketDataItem("AAPL",2,115.3286954));
        expectedBids.add(new MarketDataItem("AAPL",15,113.0));
        expectedAsks.add(new MarketDataItem("AAPL",1000,118.32));
        expectedAsks.add(new MarketDataItem("AAPL",23,118.3286954));
        expectedAsks.add(new MarketDataItem("AAPL",2,119.3286954));
        expectedAsks.add(new MarketDataItem("AAPL",15,121.0));
        expectedAsks.add(new MarketDataItem("AAPL",15,131.1524));

        MarketDataDTO expectedDTO = new MarketDataDTO((long)104,"AAPL", expectedBids,expectedAsks);

        Thread.sleep(1000);

        //retrieve market data
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] msgDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(msgDTOByteArray, 0, byteSizeOfDTO);
        dtoType = DTOType.valueOf(inputStream.read());
        byteSizeOfDTO = inputStream.read();
        byte[] marketDataDTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(marketDataDTOByteArray, 0, byteSizeOfDTO);
        MarketDataDTO DTO = MarketDataDTO.Deserialize(marketDataDTOByteArray);

        //Compare
        Assertions.assertThat(expectedDTO).
                usingRecursiveComparison().isEqualTo(DTO);
    }

    private Session Prepare(ByteArrayType type) throws Exception {
        //mock clientSocket
        Socket clientSocket = mock(Socket.class);

        ByteArrayInputStream inputStream = GenerateInputStream(type);
        outputStream = new ByteArrayOutputStream();
        when(clientSocket.getInputStream()).thenReturn(inputStream);
        when(clientSocket.getOutputStream()).thenReturn(outputStream);

        queue = new ServerQueue(3,3);

        credentialWareHouse = new CredentialWareHouse(1);
        if(type != ByteArrayType.OpenAcctRequest) {
            credentialWareHouse.CreateAccount("user1","user1Password$1");
        }

        //pre-populate locks
        ConcurrentHashMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();
        locks.put("AAPL", new ReentrantReadWriteLock());
        locks.put("TSLA", new ReentrantReadWriteLock());
        locks.put("FB", new ReentrantReadWriteLock());
        locks.put("GOOG", new ReentrantReadWriteLock());

        LimitOrderBookWareHouse orderBookWareHouse = GenerateLimitOrderBookWareHouse(locks.get("AAPL"));

        //pre-populate porfolio <userID, MarketParticipantPortfolio>
        HashMap<Integer, MarketParticipantPortfolio> userIDPortfolioMap = GenerateSamplePortfolios();

        Session session = new Session(clientSocket,0,queue,0,credentialWareHouse,
                orderBookWareHouse,locks,userIDPortfolioMap);

        return session;
    }

    private HashMap<Integer, MarketParticipantPortfolio> GenerateSamplePortfolios() throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        HashMap<Integer, MarketParticipantPortfolio> userIDPortfolioMap = new HashMap<>();

        //create portfilio for user1
        HashMap<String, SecurityCertificate> user1_securities = new HashMap<>();
        SecurityCertificate certificate1 = new SecurityCertificate("user1","AAPL",
                200, dateFormat.parse("20/01/2020"));
        SecurityCertificate certificate2 = new SecurityCertificate("user1","FB",
                308, dateFormat.parse("22/01/2020"));
        SecurityCertificate certificate3 = new SecurityCertificate("user1","TSLA",
                2, dateFormat.parse("22/05/2020"));
        user1_securities.put("AAPL", certificate1);
        user1_securities.put("FB", certificate2);
        user1_securities.put("TSLA", certificate3);
        userIDPortfolioMap.put(1, new MarketParticipantPortfolio(user1_securities,120000));

        //create portfilio for user2
        HashMap<String, SecurityCertificate> user2_securities = new HashMap<>();
        SecurityCertificate certificate1_user2 = new SecurityCertificate("user1","AAPL",
                200, dateFormat.parse("31/01/2020"));
        SecurityCertificate certificate2_user2 = new SecurityCertificate("user1","FB",
                308, dateFormat.parse("12/07/2020"));
        user1_securities.put("AAPL", certificate1_user2);
        user1_securities.put("FB", certificate2_user2);
        userIDPortfolioMap.put(1, new MarketParticipantPortfolio(user1_securities,120000));

        return userIDPortfolioMap;
    }

    private ByteArrayInputStream GenerateInputStream(ByteArrayType type) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //create login request accordingly

        if(type != ByteArrayType.OpenAcctRequest &&
                type != ByteArrayType.LoginRequest_user1 && type != ByteArrayType.LoginRequest_user2)
        {
            byte[] loginByteArray = SampleDTOByteArrayFactory.produceSampleDTOByteArray
                    (ByteArrayType.LoginRequest_user1);
            outputStream.write(loginByteArray);
        }

        byte[] byteArray = SampleDTOByteArrayFactory.produceSampleDTOByteArray(type);
        outputStream.write(byteArray);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return inputStream;
    }

    private LimitOrderBookWareHouse GenerateLimitOrderBookWareHouse(ReadWriteLock lock) throws Exception {
        LimitOrderBookWareHouse orderBookWareHouse = new LimitOrderBookWareHouse(OrderComparatorType.PriceTimePriority);
        orderBookWareHouse.AddNewLimitOrderBook("AAPL", lock);
        Pair<SortedOrderList, SortedOrderList> limitOrderBooks = orderBookWareHouse.GetLimitOrderBook("AAPL");
        SortedOrderList bids = limitOrderBooks.getKey();
        SortedOrderList asks = limitOrderBooks.getValue();

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        MarketParticipantOrder bid1 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.BUY, "AAPL",1000,117.32, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder bid2 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.BUY, "AAPL",23,116.3286954, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder bid3 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.BUY, "AAPL",2,115.3286954, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder bid4 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.BUY, "AAPL",15,113.0, OrderType.LIMITORDER, OrderDuration.GTC);

        bids.add(bid1);
        bids.add(bid2);
        bids.add(bid3);
        bids.add(bid4);

        MarketParticipantOrder ask1 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.SELL, "AAPL",1000,118.32, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder ask2 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.SELL, "AAPL",23,118.3286954, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder ask3 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.SELL, "AAPL",2,119.3286954, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder ask4 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.SELL, "AAPL",15,121.0, OrderType.LIMITORDER, OrderDuration.GTC);
        MarketParticipantOrder ask5 = new MarketParticipantOrder(0,1,"user1",0,simpleDateFormat.parse("2020-10-09"),
                Direction.SELL, "AAPL",15,131.1524, OrderType.LIMITORDER, OrderDuration.GTC);

        asks.add(ask1);
        asks.add(ask2);
        asks.add(ask3);
        asks.add(ask4);
        asks.add(ask5);

        return orderBookWareHouse;
    }
}