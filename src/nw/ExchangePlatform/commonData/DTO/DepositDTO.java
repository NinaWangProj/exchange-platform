package nw.ExchangePlatform.commonData.DTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DepositDTO implements Transferable{
    private final DTOType dtoType;
    private final double cashAmount;

    public DepositDTO(double cashAmount) {
        dtoType = DTOType.DepositRequest;
        this.cashAmount = cashAmount;
    }

    public byte[] Serialize() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] cashAmountByte = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putDouble(cashAmount).array();
        outputStream.write(cashAmountByte);

        return outputStream.toByteArray();
    }

    public static Transferable Deserialize(byte[] depositRequestByteArray) throws Exception{
        ByteArrayInputStream inputStream = new ByteArrayInputStream(depositRequestByteArray);

        byte[] cashAmtBuffer = new byte[8];
        inputStream.read(cashAmtBuffer,0,8);
        double cashAmt = ByteBuffer.wrap(cashAmtBuffer).getDouble();
        DepositDTO depositDTO = new DepositDTO(cashAmt);

        return depositDTO;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public double getCashAmount() {
        return cashAmount;
    }
}
