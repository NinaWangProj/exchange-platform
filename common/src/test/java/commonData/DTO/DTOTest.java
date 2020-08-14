package commonData.DTO;

import commonData.DataType.MarketDataType;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.clearing.SecurityCertificate;
import commonData.marketData.MarketDataItem;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.uri.ShouldHaveHost;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class DTOTest {

    @Test
    public void marketOrderDTOTest() {
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 101;
        Direction direction = Direction.BUY;
        OrderType type = OrderType.MARKETORDER;
        String tickerSymbol = "AAPL";
        int size = 1000;
        double price = -1;
        OrderDuration duration = OrderDuration.DAY;

        OrderDTO orderDTO = new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);

        byte[] orderDTOByteArray = orderDTO.Serialize();

        OrderDTO deserializedDTO = OrderDTO.Deserialize(orderDTOByteArray);

        assertThat(deserializedDTO).isEqualToComparingFieldByField(orderDTO);

    }

    @Test
    public void limitOrderDTOTest() {
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 102;
        Direction direction = Direction.SELL;
        OrderType type = OrderType.LIMITORDER;
        String tickerSymbol = "GOOGL";
        int size = 200;
        double price = 500.38;
        OrderDuration duration = OrderDuration.GTC;

        OrderDTO orderDTO = new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);

        byte[] orderDTOByteArray = orderDTO.Serialize();

        OrderDTO deserializedDTO = OrderDTO.Deserialize(orderDTOByteArray);

        assertThat(deserializedDTO).isEqualToComparingFieldByField(orderDTO);

    }

    @Test
    public void portfolioDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 103;
        SecurityCertificate googCertificate = new SecurityCertificate("Nina","GOOG",
                10000, new Date());
        SecurityCertificate aaplCertificate = new SecurityCertificate("Nina","AAPL",
                1, new Date());

        HashMap<String,SecurityCertificate> portfolio = new HashMap<String,SecurityCertificate>();
        portfolio.put("GOOG",googCertificate);
        portfolio.put("AAPL",aaplCertificate);

        PortfolioDTO portfolioDTO = new PortfolioDTO(clientRequestID,portfolio,500.23);

        byte[] portfolioDTOByteArray = portfolioDTO.Serialize();

        PortfolioDTO deserializedDTO = PortfolioDTO.Deserialize(portfolioDTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(portfolioDTO);
    }

    @Test
    public void messageDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 104;
        OrderStatusType type = OrderStatusType.PartiallyFilled;

        String message = "Congradulation!  " + "Nina" + ", Your order with orderID: " + "1002"
                + " has been filled with: " + "100" + ", shares, @$" + "500.3" + " per share.";

        MessageDTO messageDTO = new MessageDTO(clientRequestID,type,message);

        byte[] messageDTOByteArray = messageDTO.Serialize();

        MessageDTO deserializedDTO = MessageDTO.Deserialize(messageDTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(messageDTO);
    }

    @Test
    public void marketDataItemDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 105;
        String tickerSymbol = "TSLA";
        int size = 5000;
        double price = 1620.3;

        MarketDataItemDTO dataItemDTO = new MarketDataItemDTO(tickerSymbol,size,price);

        byte[] dataItemByteArray = dataItemDTO.Serialize();

        MarketDataItemDTO deserializedDTO = MarketDataItemDTO.Deserialize(dataItemByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(dataItemDTO);
    }

    @Test
    public void marketDataDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 106;
        String tickerSymbol = "MSFT";
        MarketDataItem bidDataItem1 = new MarketDataItem(tickerSymbol,1, 207);
        MarketDataItem bidDataItem2 = new MarketDataItem(tickerSymbol,100, 206.89486);
        MarketDataItem askDataItem1 = new MarketDataItem(tickerSymbol,500, 208);
        MarketDataItem askDataItem2 = new MarketDataItem(tickerSymbol,500, 209.032);

        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
        bids.add(bidDataItem1);
        bids.add(bidDataItem2);

        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();
        asks.add(askDataItem1);
        asks.add(askDataItem2);

        MarketDataDTO dto = new MarketDataDTO(clientRequestID,tickerSymbol,bids,asks);

        byte[] dTOByteArray = dto.Serialize();

        MarketDataDTO deserializedDTO = MarketDataDTO.Deserialize(dTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(dto);
    }

    @Test
    public void marketDataRequestDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 107;
        String tickerSymbol = "NFLX";
        MarketDataType type = MarketDataType.Level1;

        MarketDataRequestDTO dto = new MarketDataRequestDTO(clientRequestID,tickerSymbol,type);

        byte[] dTOByteArray = dto.Serialize();

        MarketDataRequestDTO deserializedDTO = MarketDataRequestDTO.Deserialize(dTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(dto);
    }

/*    @Test
    public void portfolioRequestDTOTest() throws Exception{
        //serialize an order then deserialize to see if we will get the same object back
        long clientRequestID = 104;

        PortfolioRequestDTO portRequestDTO = new PortfolioRequestDTO(clientRequestID);


        PortfolioDTO portfolioDTO = new PortfolioDTO(clientRequestID,portfolio,500.23);

        byte[] portfolioDTOByteArray = portfolioDTO.Serialize();

        PortfolioDTO deserializedDTO = PortfolioDTO.Deserialize(portfolioDTOByteArray);

        Assertions.assertThat(deserializedDTO).usingRecursiveComparison().isEqualTo(portfolioDTO);
    }*/
}