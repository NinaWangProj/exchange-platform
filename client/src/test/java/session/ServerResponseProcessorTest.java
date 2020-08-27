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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ServerResponseProcessorTest {

    @Test
    void readMessageFromServerTest() throws Exception{

        MessageDTO messageDTO = new MessageDTO(113, OrderStatusType.PartiallyFilled,
                "200 share of AAPL has been filled");
        byte[] messageDTOByteArray = messageDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(messageDTO.getDtoType().getByteValue());
        outputStream.write((byte)messageDTOByteArray.length);
        outputStream.write(messageDTOByteArray);
        byte[] dtoByteArray = outputStream.toByteArray();

        //read DTO from input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(dtoByteArray);

        ServerResponseProcessor DTOreader = new ServerResponseProcessor(inputStream);
        Transferable messageDTOT = DTOreader.ReadMessageFromServer();

        Assertions.assertThat(messageDTOT).usingRecursiveComparison().isEqualTo(messageDTO);
    }
}