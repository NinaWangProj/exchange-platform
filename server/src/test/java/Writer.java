import clearing.data.DTCCWarehouse;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

public class Writer {

    @Test
    public void DTCCWareHouseWriterTest() throws IOException {
        DTCCWarehouse dtccWarehouse = new DTCCWarehouse();

        HashMap<Integer, SecurityCertificate> userCertificates = new HashMap<>();
        userCertificates.put(101, new SecurityCertificate("user1","AAPL",
                100, new Date()));

        HashMap<String, SecurityCertificate> tickerCertificateMap = new HashMap<>();
        tickerCertificateMap.put("AAPL", new SecurityCertificate("user1","AAPL",
                100, new Date()));
        MarketParticipantPortfolio portfolio = new MarketParticipantPortfolio(tickerCertificateMap,1000);

        dtccWarehouse.certificatesMap.put("AAPL", userCertificates);
        dtccWarehouse.portfoliosMap.put(101,portfolio);

        String outputPath = "C:\\Users\\nwang\\Desktop\\Nina_Project\\Test\\Test.json";
        OutputStream outputStream = new FileOutputStream(outputPath);
        dtccWarehouse.WriteToJSONString(outputStream);
        outputStream.flush();
        outputStream.close();
    }


}
