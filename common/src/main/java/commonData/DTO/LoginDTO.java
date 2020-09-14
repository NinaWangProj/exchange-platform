package commonData.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class LoginDTO implements Transferable {
    private final DTOType dtoType;
    private final String userName;
    private final String password;
    private final Long clientRequestID;

    public LoginDTO(Long clientRequestID, String userName,String password) {
        this.userName = userName;
        this.password = password;
        dtoType = DTOType.LoginRequest;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
        outputStream.write(requestIDByteArray);

        byte[] userNameBytes = userName.getBytes();
        byte userNameSize = (byte)userNameBytes.length;
        outputStream.write(userNameSize);
        outputStream.write(userNameBytes);

        byte[] passwordBytes = password.getBytes();
        byte passwordSize = (byte)passwordBytes.length;
        outputStream.write(passwordSize);
        outputStream.write(passwordBytes);

        return outputStream.toByteArray();
    }

    public static LoginDTO Deserialize(byte[] LoginDTOBytes) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(LoginDTOBytes);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        int userNameLength = inputStream.read();
        byte[] userNameBuffer = new byte[userNameLength];
        inputStream.read(userNameBuffer, 0, userNameLength);
        String userNameT = new String(userNameBuffer);

        int passwordLength = inputStream.read();
        byte[] passwordBuffer = new byte[passwordLength];
        inputStream.read(passwordBuffer, 0, passwordLength);
        String passwordT = new String(passwordBuffer);

        LoginDTO DTO = new LoginDTO(requestIDT,userNameT,passwordT);
        return DTO;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
