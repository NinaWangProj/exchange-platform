package session;

import commonData.DTO.OrderStatusDTO;
import commonData.DTO.Transferable;
import commonData.DataType.OrderStatusType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class ServerResponseProcessorTest {

    @Test
    void readMessageFromServerTest() throws Exception{

        OrderStatusDTO orderStatusDTO = new OrderStatusDTO(113, OrderStatusType.PartiallyFilled,
                "200 share of AAPL has been filled");
        byte[] messageDTOByteArray = orderStatusDTO.Serialize();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(orderStatusDTO.getDtoType().getByteValue());
        outputStream.write((byte)messageDTOByteArray.length);
        outputStream.write(messageDTOByteArray);
        byte[] dtoByteArray = outputStream.toByteArray();

        //read DTO from input stream
        ByteArrayInputStream inputStream = new ByteArrayInputStream(dtoByteArray);

        ServerResponseProcessor DTOreader = new ServerResponseProcessor(inputStream);
        Transferable messageDTOT = DTOreader.ReadMessageFromServer().getKey();

        Assertions.assertThat(messageDTOT).usingRecursiveComparison().isEqualTo(orderStatusDTO);
    }
}