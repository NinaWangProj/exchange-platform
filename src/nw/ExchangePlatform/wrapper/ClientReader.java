package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDTO;
import nw.ExchangePlatform.data.Transferable;

import java.io.InputStream;

public class ClientReader {
    private InputStream inputStream;

    public ClientReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Transferable ReadRequestFromClient() throws Exception {
        int nextByte = inputStream.read();
        Transferable DTO = null;

        if(nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);

            switch (dtoType) {
                case ORDER:
                    int byteSizeOfDTO = inputStream.read();
                    byte[] orderDTOByteArray = new byte[byteSizeOfDTO];
                    inputStream.read(orderDTOByteArray, 0, byteSizeOfDTO);
                    OrderDTO orderDTO = OrderDTO.Deserialize(orderDTOByteArray);
                    DTO = orderDTO;

                    //case CONFIG:
                    //need to implement later
            }
        }

        return DTO;
    }
}
