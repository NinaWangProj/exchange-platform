package utility;

import commonData.DTO.Transferable;

import java.io.ByteArrayOutputStream;

public class SampleDTOByteArrayFactory {

    public static byte[] produceSampleDTOByteArray(ByteArrayType type) throws Exception{
        byte[] byteArray = null;
        switch (type) {
            case MarketData_L1:
                byteArray = produceMarketOrderDTOByteArray();
                break;
            case MarketData_L3_Msg_PartialFillLimitBuyOrder:
                byteArray = ProduceMarketData_MessageDTOByteArray();
                break;
            case BookChanges_InsertOperations:
                byteArray = ProduceBookChanges_2Ops_DTOByteArray();
                break;
            case BookChanges_MixedOperations:
                break;
        }
        return byteArray;
    }

    private static byte[] produceMarketOrderDTOByteArray() throws Exception {
        SampleDTOFactory dtoFactory = new SampleDTOFactory();
        Transferable marketDataDTO = dtoFactory.ProduceSampleDTO(DTOTestType.MarketData_Level1);
        byte[] DTOByteArray = marketDataDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(marketDataDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);

        return outputStream.toByteArray();
    }

    private static byte[] ProduceMarketData_MessageDTOByteArray() throws Exception {
        SampleDTOFactory dtoFactory = new SampleDTOFactory();

        //market data
        Transferable marketDataDTO = dtoFactory.ProduceSampleDTO(DTOTestType.MarketData_Level3);
        byte[] marketDataDTOByteArray = marketDataDTO.Serialize();

        //message
        Transferable msgDTO = dtoFactory.ProduceSampleDTO(DTOTestType.Message_PartiallyFilled_LimitBuyOrder);
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
        SampleDTOFactory dtoFactory = new SampleDTOFactory();

        //Book Changes
        Transferable bookChangesDTO0 = dtoFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo0);
        byte[] bookChangesDTO0ByteArray = bookChangesDTO0.Serialize();
        Transferable bookChangesDTO1 = dtoFactory.ProduceSampleDTO(DTOTestType.BookChanges_InsertTo1);
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


}
