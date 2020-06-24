package nw.ExchangePlatform.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class LoginDTO implements Transferable {
    private final String userName;
    private final String password;

    public LoginDTO(String userName,String password) {
        this.userName = userName;
        this.password = password;
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

        LoginDTO DTO = new LoginDTO(userNameT,passwordT);
        return DTO;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
