package commonData.DTO;

import commonData.clearing.SecurityCertificate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PortfolioDTO implements Transferable{
    private final Long clientRequestID;
    private final DTOType dtoType;
    //HashMap<tickerSymbol,SecurityCertificate>
    private final HashMap<String, SecurityCertificate> securities;
    private final double cash;

    public PortfolioDTO(Long clientRequestID, HashMap<String, SecurityCertificate> securities, double cash) {
        this.securities = securities;
        this.cash = cash;
        this.dtoType = DTOType.Portfolio;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] requestIDByteArray = ByteBuffer.allocate(8).putLong(clientRequestID).array();
        outputStream.write(requestIDByteArray);

        byte[] cashAmtByte = ByteBuffer.allocate(8).putDouble(cash).array();
        outputStream.write(cashAmtByte);

        int numOfSecurities = securities.size();
        byte[] numOfSecuritiesByte = ByteBuffer.allocate(4).putInt(numOfSecurities).array();
        outputStream.write(numOfSecuritiesByte);

        for(Map.Entry<String, SecurityCertificate> entry : securities.entrySet()) {
            //share holder name
            byte[] shareHolderNameByte = entry.getValue().shareHolderName.getBytes();
            byte shareHolderNameByteSize = (byte) shareHolderNameByte.length;
            outputStream.write(shareHolderNameByteSize);
            outputStream.write(shareHolderNameByte);

            //ticker symbol
            byte[] tickerSymbolByte = entry.getKey().getBytes();
            byte tickerSize = (byte) tickerSymbolByte.length;
            outputStream.write(tickerSize);
            outputStream.write(tickerSymbolByte);

            //quantity
            byte[] quantityByteArray = ByteBuffer.allocate(4).putInt(entry.getValue().quantity).array();
            outputStream.write(quantityByteArray);

            //issued Date
            DateFormat formater = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSZ");
            String formattedIssuedDate = formater.format(entry.getValue().issuedDate);
            byte[] issuedDateStringByteArray = formattedIssuedDate.getBytes();
            byte issuedDateStringByteSize = (byte) issuedDateStringByteArray.length;
            outputStream.write(issuedDateStringByteSize);
            outputStream.write(issuedDateStringByteArray);
        }

        byte[] portfolioDTOByteArray = outputStream.toByteArray();

        return portfolioDTOByteArray;
    }

    public static PortfolioDTO Deserialize(byte[] DTOByteArray) throws Exception{
        HashMap<String, SecurityCertificate> securities = new HashMap<String, SecurityCertificate>();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(DTOByteArray);

        byte[] requestIDBuffer = new byte[8];
        inputStream.read(requestIDBuffer, 0, 8);
        Long requestIDT = ByteBuffer.wrap(requestIDBuffer).getLong();

        byte[] cashAmtBuffer = new byte[8];
        inputStream.read(cashAmtBuffer, 0, 8);
        double cashAmtT = ByteBuffer.wrap(cashAmtBuffer).getDouble();

        byte[] numOfSecuritiesBuffer = new byte[4];
        inputStream.read(numOfSecuritiesBuffer, 0, 4);
        double numOfSecuritiesBufferT = ByteBuffer.wrap(numOfSecuritiesBuffer).getInt();

        for(int i = 0; i < numOfSecuritiesBufferT; i++) {

            //share holder name
            int shareHolderNameSize = inputStream.read();
            byte[] shareHolderNameBuffer = new byte[shareHolderNameSize];
            inputStream.read(shareHolderNameBuffer, 0, shareHolderNameSize);
            String shareHolderNameT = new String(shareHolderNameBuffer);

            //ticker symbol
            int tickerSymbolSize = inputStream.read();
            byte[] tickerSymbolBuffer = new byte[tickerSymbolSize];
            inputStream.read(tickerSymbolBuffer, 0, tickerSymbolSize);
            String tickerSymbolT = new String(tickerSymbolBuffer);

            //quantity
            byte[] quantityBuffer = new byte[4];
            inputStream.read(quantityBuffer,0,4);
            int quantityT = ByteBuffer.wrap(quantityBuffer).getInt();

            //issued date
            int issuedDateStringSize = inputStream.read();
            byte[] issuedDateStringBuffer = new byte[issuedDateStringSize];
            inputStream.read(issuedDateStringBuffer, 0, issuedDateStringSize);
            String issuedDateStringT = new String(issuedDateStringBuffer);
            Date issuedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSZ").parse(issuedDateStringT);

            securities.put(tickerSymbolT, new SecurityCertificate(shareHolderNameT,tickerSymbolT,quantityT,issuedDate));
        }
        PortfolioDTO portfolioDTO = new PortfolioDTO(requestIDT,securities,cashAmtT);
        return portfolioDTO;
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public HashMap<String, SecurityCertificate> getSecurities() {
        return securities;
    }

    public double getCash() {
        return cash;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
