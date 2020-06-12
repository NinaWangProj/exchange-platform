package nw.ExchangePlatform.wrapper;

import nw.ExchangePlatform.data.DTOType;
import nw.ExchangePlatform.data.MarketParticipantOrder;
import nw.ExchangePlatform.data.OrderDTO;

import java.io.InputStream;

public class ClientReader implements Runnable{
    private InputStream inputStream;
    private OrderQueue orderQueue;

    public ClientReader(InputStream inputStream,OrderQueue orderQueue) {
        this.inputStream = inputStream;
        this.orderQueue = orderQueue;
    }

    public void ReadRequestFromClient() throws Exception{
        int nextByte= inputStream.read();

        while (nextByte != -1) {
            DTOType dtoType = DTOType.valueOf(nextByte);

            switch (dtoType) {
                case ORDER:
                    int byteSizeOfDTO = inputStream.read();
                    byte[] orderDTOByteArray = new byte[byteSizeOfDTO];
                    inputStream.read(orderDTOByteArray,0,byteSizeOfDTO);
                    OrderDTO orderDTO = OrderDTO.Deserialize(orderDTOByteArray);

                    //convert OrderDTO to MarketParticipantOrder
                    //MarketParticipantOrder mporder = new MarketParticipantOrder();
                    //place order in Queue
                    //orderQueue.AddOrderToQueue(mporder);

                case CONFIG:
                    //need to implement later

            }
            nextByte = inputStream.read();
        }
    }

    public void run() {
        try {
            ReadRequestFromClient();
        } catch (Exception e) {

        }
    }
}
