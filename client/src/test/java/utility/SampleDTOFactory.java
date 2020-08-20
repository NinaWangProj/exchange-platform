package utility;

import commonData.DTO.*;
import commonData.DataType.MarketDataType;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import commonData.clearing.SecurityCertificate;
import commonData.marketData.MarketDataItem;
import org.graalvm.compiler.core.common.type.ArithmeticOpTable;
import sun.util.cldr.CLDRBaseLocaleDataMetaInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class SampleDTOFactory {

    public Transferable ProduceSampleDTO(DTOTestType type) {
        Transferable sampleDTO = null;
        switch (type) {
            case Message_Unfilled_MarketBuyOrder:
                sampleDTO = ProduceSampleMessage_UnfilledDTO();
                break;
            case Message_PartiallyFilled_LimitBuyOrder:
                sampleDTO = ProduceSampleMessage_PartiallyFilled_1DTO();
                break;
            case Message_PartiallyFilled_LimitSellOrder:
                sampleDTO = ProduceSampleMessage_PartiallyFilled_2DTO();
                break;
            case Message_PartiallyFilled_MarketSellOrder:
                sampleDTO = ProduceSampleMessage_PartiallyFilled_3DTO();
                break;
            case MarketData_Level1:
                sampleDTO = ProduceSampleMarketData_Level1DTO();
                break;
            case MarketData_Level3:
                sampleDTO = ProduceSampleMarketData_Level3DTO();
                break;
            case MarketDataReq_Level1:
                sampleDTO = ProduceSampleMareketDataRequest_Level1DTO();
                break;
            case MarketDataReq_Level3:
                sampleDTO = ProduceSampleMareketDataRequest_Level3DTO();
                break;
            case Portfolio:
                sampleDTO = ProduceSamplePortfolioDTO();
                break;
            case PortfolioRequest:
                sampleDTO = ProduceSamplePortfolioRequestDTO();
                break;
/*            case BookChanges_1:
                sampleDTO = ProduceSampleBookChanges_1DTO();
                break;
            case BookChanges_2:
                sampleDTO = ProduceSampleBookChanges_2DTO();
                break;*/
            case Order_LimitOrder_Buy:
                sampleDTO = ProduceSampleOrder_LimitOrder_BuyDTO();
                break;
            case Order_LimitOrder_Sell:
                sampleDTO = ProduceSampleOrder_LimitOrder_SellDTO();
                break;
            case Order_MarketOrder_Buy:
                sampleDTO = ProduceSampleOrder_MarketOrder_BuyDTO();
                break;
            case Order_MarketOrder_Sell:
                sampleDTO = ProduceSampleOrder_MarketOrder_SellDTO();
                break;
            case DepositRequest:
                sampleDTO = ProduceSampleDepositRequestDTO();
                break;
        }
        return sampleDTO;
    }

    private MessageDTO ProduceSampleMessage_UnfilledDTO() {
        MessageDTO dto = new MessageDTO(101, OrderStatusType.Unfilled,
                "The order is not being filled");
        return dto;
    }

    private MessageDTO ProduceSampleMessage_PartiallyFilled_1DTO() {
        MessageDTO dto = new MessageDTO(108, OrderStatusType.PartiallyFilled,
                "200 share of GOOG has being filled at $1576.25.");
        return dto;
    }

    private MessageDTO ProduceSampleMessage_PartiallyFilled_2DTO() {
        MessageDTO dto = new MessageDTO(102, OrderStatusType.PartiallyFilled,
                "200 share of AAPL has being filled at $267.34.");
        return dto;
    }

    private MessageDTO ProduceSampleMessage_PartiallyFilled_3DTO() {
        MessageDTO dto = new MessageDTO(1029, OrderStatusType.PartiallyFilled,
                "200 share of TSLA has being filled at $2000.23");
        return dto;
    }

    private MarketDataDTO ProduceSampleMarketData_Level1DTO() {
        long clientRequestID = 103;
        String tickerSymbol = "AAPL";
        ArrayList<MarketDataItem> bids = new ArrayList<>();
        ArrayList<MarketDataItem> asks = new ArrayList<>();
        bids.add(new MarketDataItem(tickerSymbol,309,263.89));
        asks.add(new MarketDataItem(tickerSymbol,200,264.23));

        MarketDataDTO dto = new MarketDataDTO(clientRequestID,tickerSymbol,bids,asks);
        return dto;
    }

    private MarketDataDTO ProduceSampleMarketData_Level3DTO() {
        long clientRequestID = 104;
        String tickerSymbol = "AAPL";

        ArrayList<MarketDataItem> bids = new ArrayList<>();
        ArrayList<MarketDataItem> asks = new ArrayList<>();

        bids.add(new MarketDataItem(tickerSymbol,309,263.89));
        asks.add(new MarketDataItem(tickerSymbol,200,264.23));

        bids.add(new MarketDataItem(tickerSymbol,1,262));
        asks.add(new MarketDataItem(tickerSymbol,23,265));

        bids.add(new MarketDataItem(tickerSymbol,309,261.89));
        asks.add(new MarketDataItem(tickerSymbol,200,266.32));

        return new MarketDataDTO(clientRequestID,tickerSymbol,bids,asks);
    }

    private MarketDataRequestDTO ProduceSampleMareketDataRequest_Level1DTO() {
        long clientRequestID = 103;
        String tickerSymbol = "AAPL";
        MarketDataType type = MarketDataType.Level1;

        return new MarketDataRequestDTO(clientRequestID,tickerSymbol,type);
    }

    private MarketDataRequestDTO ProduceSampleMareketDataRequest_Level3DTO() {
        long clientRequestID = 104;
        String tickerSymbol = "AAPL";
        MarketDataType type = MarketDataType.Level3;

        return new MarketDataRequestDTO(clientRequestID,tickerSymbol,type);
    }

    private PortfolioDTO ProduceSamplePortfolioDTO() {
        long clientRequestID = 105;
        String shareHolderName = "user1";
        double cashAmt = 89000.254;
        HashMap<String, SecurityCertificate> certificates = new HashMap<String, SecurityCertificate>();
        certificates.put("FB", new SecurityCertificate(shareHolderName,"FB",230,new Date()));
        certificates.put("TSLA", new SecurityCertificate(shareHolderName,"TSLA",100,new Date()));
        certificates.put("AMZN", new SecurityCertificate(shareHolderName,"AMZN",1,new Date()));

        return new PortfolioDTO(clientRequestID,certificates,cashAmt);
    }

    private PortfolioRequestDTO ProduceSamplePortfolioRequestDTO() {
        long clientRequestID = 105;

        return new PortfolioRequestDTO(clientRequestID);
    }

/*    private MarketDataRequestDTO ProduceSampleBookChanges_1DTO() {

    }

    private MarketDataRequestDTO ProduceSampleBookChanges_2DTO() {

    }*/

    private OrderDTO ProduceSampleOrder_LimitOrder_BuyDTO() {
        long clientRequestID = 108;
        Direction direction = Direction.BUY;
        OrderType type = OrderType.LIMITORDER;
        String tickerSymbol = "GOOG";
        int size = 533;
        double price = 1576.25;
        OrderDuration duration = OrderDuration.GTC;

        return new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);
    }

    private OrderDTO ProduceSampleOrder_LimitOrder_SellDTO() {
        long clientRequestID = 102;
        Direction direction = Direction.SELL;
        OrderType type = OrderType.LIMITORDER;
        String tickerSymbol = "AAPL";
        int size = 500;
        double price = 267.34;
        OrderDuration duration = OrderDuration.GTC;

        return new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);
    }

    private OrderDTO ProduceSampleOrder_MarketOrder_BuyDTO() {
        long clientRequestID = 101;
        Direction direction = Direction.BUY;
        OrderType type = OrderType.MARKETORDER;
        String tickerSymbol = "TSLA";
        int size = 1200;
        double price = -1;
        OrderDuration duration = OrderDuration.GTC;

        return new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);
    }

    private OrderDTO ProduceSampleOrder_MarketOrder_SellDTO() {
        long clientRequestID = 109;
        Direction direction = Direction.SELL;
        OrderType type = OrderType.MARKETORDER;
        String tickerSymbol = "TSLA";
        int size = 3;
        double price = -1;
        OrderDuration duration = OrderDuration.GTC;

        return new OrderDTO(clientRequestID,direction,type,tickerSymbol,size,price,duration);
    }

    private DepositDTO ProduceSampleDepositRequestDTO() {
        long clientRequestID = 110;
        double cashAmt = 12550.36;

        return new DepositDTO(clientRequestID,cashAmt);
    }
}
