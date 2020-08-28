package utility;

import commonData.DTO.*;
import commonData.DataType.OrderStatusType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MockServer {
    private ByteArrayOutputStream clientOutputStream;
    private ByteArrayInputStream inputStream;
    private Transferable DTO;
    private ByteArrayOutputStream outputStream;
    private Transferable ResponseDTO;

    public MockServer(ByteArrayOutputStream clientOutputStream) {
        this.clientOutputStream = clientOutputStream;
    }

    public void Initialize() {
        inputStream = new ByteArrayInputStream(clientOutputStream.toByteArray());
        this.outputStream = new ByteArrayOutputStream();
    }

    public Transferable ReadRequestFromClient() throws Exception {

        DTOType dtoType = DTOType.valueOf(inputStream.read());
        int byteSizeOfDTO = inputStream.read();
        byte[] DTOByteArray = new byte[byteSizeOfDTO];
        inputStream.read(DTOByteArray, 0, byteSizeOfDTO);

        switch (dtoType) {
            case Order:
                DTO = OrderDTO.Deserialize(DTOByteArray);
                break;
            case LoginRequest:
                DTO = LoginDTO.Deserialize(DTOByteArray);
                break;
            case MareketDataRequest:
                DTO = MarketDataDTO.Deserialize(DTOByteArray);
                break;
        }
        return DTO;
    }

    public void RespondToClient() throws Exception {
        DTOType type = DTO.getDtoType();

        switch (type) {
            case Order:
                OrderDTO orderDTO = (OrderDTO)DTO;
                ResponseDTO = new MessageDTO(orderDTO.getClientRequestID(), OrderStatusType.PartiallyFilled,
                        "Your market Order has been filled with 100 shares @ $300");
                break;
            case MareketDataRequest:

                break;
            case PortfolioRequest:
                break;
        }

        byte[] DTOByteArray = ResponseDTO.Serialize();
        outputStream.write(ResponseDTO.getDtoType().getByteValue());
        outputStream.write((byte)DTOByteArray.length);
        outputStream.write(DTOByteArray);
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }
}
