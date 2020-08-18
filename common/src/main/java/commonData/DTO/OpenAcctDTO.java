package commonData.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class OpenAcctDTO implements Transferable {
    private final DTOType dtoType;
    private final String userName;
    private final String password;

    public OpenAcctDTO(String userName,String password) {
        this.userName = userName;
        this.password = password;
        dtoType = DTOType.OpenAcctRequest;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

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

    public static Transferable Deserialize(byte[] LoginDTOBytes) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(LoginDTOBytes);

        int userNameLength = inputStream.read();
        byte[] userNameBuffer = new byte[userNameLength];
        inputStream.read(userNameBuffer, 0, userNameLength);
        String userNameT = new String(userNameBuffer);

        int passwordLength = inputStream.read();
        byte[] passwordBuffer = new byte[passwordLength];
        inputStream.read(passwordBuffer, 0, passwordLength);
        String passwordT = new String(passwordBuffer);

        OpenAcctDTO DTO = new OpenAcctDTO(userNameT,passwordT);
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
}
