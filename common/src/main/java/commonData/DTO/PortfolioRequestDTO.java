package commonData.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class PortfolioRequestDTO implements Transferable{
    private final DTOType dtoType;
    private final Long clientRequestID;

    public PortfolioRequestDTO(Long clientRequestID) {
        dtoType = DTOType.DepositRequest;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
        outputStream.write(requestIDByteArray);

        return outputStream.toByteArray();
    }

    public static PortfolioRequestDTO Deserialize(byte[] portRequestDTOByteArray) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(portRequestDTOByteArray);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        return new PortfolioRequestDTO(requestIDT);
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
