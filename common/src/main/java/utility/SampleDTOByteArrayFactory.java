package utility;

import commonData.DTO.Transferable;

import java.io.ByteArrayOutputStream;

public class SampleDTOByteArrayFactory {

    public static byte[] produceSampleDTOByteArray(ByteArrayType type) throws Exception{
        byte[] byteArray = null;
        switch (type) {
            case MarketData_L1:
                byteArray = produceMarketDataDTOByteArray();
                break;
            case MarketData_L3_Msg_PartialFillLimitBuyOrder:
                byteArray = ProduceMarketData_MessageDTOByteArray();
                break;
            case BookChanges_InsertOperations:
                byteArray = ProduceBookChanges_2Ops_DTOByteArray();
                break;
            case BookChanges_MixedOperations:
                break;
            case OpenAcctRequest:
                byteArray = ProduceOpenAcctRequestDTO_user1();
                break;
            case LoginRequest_user1:
                byteArray = ProduceLoginRequestDTO_user1();
                break;
            case LoginRequest_user2:
                byteArray = ProduceLoginRequestDTO_user2();
                break;
            case PortfolioRequest:
                byteArray = ProducePortfolioRequestDTO();
                break;
            case MarketDataRequest_L1:
                byteArray = ProduceMarketDataRequestDTO_AAPL();
                break;
            case MarketDataRequest_L3:
                byteArray = ProduceMarketDataRequest_L3_AAPL();
                break;
            case MarketDataRequest_ContL3:
                break;
            case OrderDTO_Limit_Buy:
                byteArray = ProduceLimitOrder_Buy_DTOByteArray();
                break;
            case OrderDTO_Market:
                break;
        }
        return byteArray;
    }

    private static byte[] produceMarketDataDTOByteArray() throws Exception {
        Transferable marketDataDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketData_Level1);
        byte[] DTOByteArray = marketDataDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(marketDataDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceMarketData_MessageDTOByteArray() throws Exception {
        //market data
        Transferable marketDataDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketData_Level3);
        byte[] marketDataDTOByteArray = marketDataDTO.Serialize();

        //message
        Transferable msgDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.Message_PartiallyFilled_LimitBuyOrder);
        byte[] msgDTOByteArray = msgDTO.Serialize();

        //write market data to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(marketDataDTO.getDtoType().getByteValue());
        outputStream.write((byte)marketDataDTOByteArray.length);
        outputStream.write(marketDataDTOByteArray);

        //write msg to output stream
        outputStream.write(msgDTO.getDtoType().getByteValue());
        outputStream.write((byte)msgDTOByteArray.length);
        outputStream.write(msgDTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceBookChanges_2Ops_DTOByteArray() throws Exception {
        //Book Changes
        Transferable bookChangesDTO0 = SampleDTOFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo0);
        byte[] bookChangesDTO0ByteArray = bookChangesDTO0.Serialize();
        Transferable bookChangesDTO1 = SampleDTOFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo1);
        byte[] bookChangesDTO1ByteArray = bookChangesDTO1.Serialize();

        //write market data to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(bookChangesDTO0.getDtoType().getByteValue());
        outputStream.write((byte)bookChangesDTO0ByteArray.length);
        outputStream.write(bookChangesDTO0ByteArray);

        //write msg to output stream
        outputStream.write(bookChangesDTO1.getDtoType().getByteValue());
        outputStream.write((byte)bookChangesDTO1ByteArray.length);
        outputStream.write(bookChangesDTO1ByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceLimitOrder_Buy_DTOByteArray() throws Exception {
        //Limit order
        Transferable limitOrderDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.Order_LimitOrder_Buy);
        byte[] limitOrderDTOByteArray = limitOrderDTO.Serialize();

        //write order to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(limitOrderDTO.getDtoType().getByteValue());
        outputStream.write((byte)limitOrderDTOByteArray.length);
        outputStream.write(limitOrderDTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceOpenAcctRequestDTO_user1() throws Exception {
        Transferable openAcctRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.OpenAcctRequest_user1);
        byte[] DTOByteArray = openAcctRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(openAcctRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceLoginRequestDTO_user1() throws Exception {
        Transferable loginRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.LoginRequest_user1);
        byte[] DTOByteArray = loginRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(loginRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceLoginRequestDTO_user2() throws Exception {
        Transferable loginRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.LoginRequest_user2);
        byte[] DTOByteArray = loginRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(loginRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }


    private static byte[] ProducePortfolioRequestDTO() throws Exception {
        Transferable portfolioRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.PortfolioRequest);
        byte[] DTOByteArray = portfolioRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(portfolioRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceMarketDataRequestDTO_AAPL() throws Exception {
        Transferable marketDataRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketDataReq_Level1);
        byte[] DTOByteArray = marketDataRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(marketDataRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceMarketDataRequest_L3_AAPL() throws Exception {
        Transferable marketDataRequestDTO = SampleDTOFactory.ProduceSampleDTO(DTOTestType.MarketDataReq_Level3);
        byte[] DTOByteArray = marketDataRequestDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(marketDataRequestDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

}
