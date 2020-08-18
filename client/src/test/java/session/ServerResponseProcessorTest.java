package session;

import commonData.DTO.MarketDataItemDTO;
import commonData.DTO.MessageDTO;
import commonData.DTO.OrderDTO;
import commonData.DTO.Transferable;
import commonData.DataType.OrderStatusType;
import commonData.Order.Direction;
import commonData.Order.OrderDuration;
import commonData.Order.OrderType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ServerResponseProcessorTest {

    @Test
    void readMessageFromServer() throws Exception{
/*        OrderDTO orderDTO = new OrderDTO(113, Direction.SELL, OrderType.LIMITORDER,"AAPL",
                3,150.235, OrderDuration.GTC);
        byte[] orderDTOByteArray = orderDTO.Serialize();*/

        MessageDTO messageDTO = new MessageDTO(113, OrderStatusType.PartiallyFilled,
                "200 share of AAPL has been filled");
        byte[] messageDTOByteArray = messageDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(messageDTO.getDtoType().getByteValue());
        outputStream.write((byte)messageDTOByteArray.length);
        outputStream.write(messageDTOByteArray);

        //read DTO from input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        ServerResponseProcessor DTOreader = new ServerResponseProcessor(inputStream);
        Transferable marketDataItemDTOT = DTOreader.ReadMessageFromServer();

        Assertions.assertThat(marketDataItemDTOT).usingRecursiveComparison().isEqualTo(messageDTO);
    }
}