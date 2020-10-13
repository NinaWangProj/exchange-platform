package clearing.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import commonData.clearing.MarketParticipantPortfolio;
import commonData.clearing.SecurityCertificate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;


public class DTCCWarehouse {

    //<ticker,HashMap<userID,certificate>>
    public HashMap<String, HashMap<Integer, SecurityCertificate>> certificatesMap;

    public HashMap<Integer, MarketParticipantPortfolio> portfoliosMap;

    public DTCCWarehouse() {
        certificatesMap = new HashMap<String, HashMap<Integer, SecurityCertificate>>();
        portfoliosMap = new HashMap<Integer, MarketParticipantPortfolio>();
    }

    public void WriteToJSONString(OutputStream outputStream) throws IOException {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        try {
            String jsonStr = JSONObjectMapper.writeValueAsString(this);
            outputWriter.write(jsonStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputWriter.flush();
            outputWriter.close();
        }
    }

    public static DTCCWarehouse ReadFromJSON(InputStream inputStream) {
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        DTCCWarehouse dtccWarehouse = null;
        try {
            dtccWarehouse = JSONObjectMapper.readValue(inputStream,DTCCWarehouse.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return dtccWarehouse;
    }
}
