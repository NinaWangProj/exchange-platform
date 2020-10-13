package serverEngine;

import clearing.data.CredentialWareHouse;
import com.fasterxml.jackson.databind.ObjectMapper;
import trading.limitOrderBook.OrderComparatorType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicLong;

public class ServerConfig {
    private final int numOfOrderQueues;
    private final int numOfEngineResultQueues;
    private final int serverPortID;
    private final int baseOrderID;
    private final OrderComparatorType comparatorType;
    private final AtomicLong previousTransactionID;
    private final String snapShotFolderPath;

    public ServerConfig(int numOfOrderQueues, int numOfEngineResultQueues, int serverPortID, int baseOrderID,
                        OrderComparatorType comparatorType, AtomicLong previousTransactionID,
                        String snapShotFolderPath) {
        this.numOfOrderQueues = numOfOrderQueues;
        this.numOfEngineResultQueues = numOfEngineResultQueues;
        this.serverPortID = serverPortID;
        this.baseOrderID = baseOrderID;
        this.comparatorType = comparatorType;
        this.previousTransactionID = previousTransactionID;
        this.snapShotFolderPath = snapShotFolderPath;
    }

    public int getNumOfOrderQueues() {
        return numOfOrderQueues;
    }

    public int getNumOfEngineResultQueues() {
        return numOfEngineResultQueues;
    }

    public int getServerPortID() {
        return serverPortID;
    }

    public int getBaseOrderID() {
        return baseOrderID;
    }

    public OrderComparatorType getComparatorType() {
        return comparatorType;
    }

    public AtomicLong getPreviousTransactionID() {
        return previousTransactionID;
    }

    public String getSnapShotFolderPath() {
        return snapShotFolderPath;
    }

    public void WriteToJSONString(OutputStream outputStream) {
        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        try {
            String jsonStr = JSONObjectMapper.writeValueAsString(this);
            outputWriter.write(jsonStr);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServerConfig BuildFromJSON(InputStream inputStream) {
        ObjectMapper JSONObjectMapper = new ObjectMapper();
        ServerConfig config = null;
        try {
            config = JSONObjectMapper.readValue(inputStream, ServerConfig.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void UpdateConfigs(int baseOrderID, int previousTransactionID) {

    }

}
