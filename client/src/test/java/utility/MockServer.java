package utility;

import commonData.DTO.*;
import commonData.DataType.MessageType;
import commonData.DataType.OrderStatusType;
import commonData.clearing.SecurityCertificate;
import commonData.marketData.MarketDataItem;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MockServer implements Runnable{
    private PipedInputStream serverInputStream;
    private PipedOutputStream serverOutputStream;
    private Transferable ReceivedDTO;
    private Transferable ResponseDTO;
    private HashMap<String,String> MockCredentialWarehouse;

    public MockServer(PipedInputStream serverInputStream, PipedOutputStream serverOutputStream) {
        this.serverInputStream = serverInputStream;
        this.serverOutputStream = serverOutputStream;
        this.MockCredentialWarehouse = new HashMap<>();
    }

    public void ReadRequestFromClient() throws Exception {
        DTOType dtoType = DTOType.valueOf(serverInputStream.read());
        int byteSizeOfDTO = serverInputStream.read();
        byte[] DTOByteArray = new byte[byteSizeOfDTO];
        serverInputStream.read(DTOByteArray, 0, byteSizeOfDTO);

        switch (dtoType) {
            case Order:
                ReceivedDTO = OrderDTO.Deserialize(DTOByteArray);
                break;
            case LoginRequest:
                ReceivedDTO = LoginDTO.Deserialize(DTOByteArray);
                break;
            case MarketDataRequest:
                ReceivedDTO = MarketDataRequestDTO.Deserialize(DTOByteArray);
                break;
            case PortfolioRequest:
                ReceivedDTO = PortfolioRequestDTO.Deserialize(DTOByteArray);
                break;
            case OpenAcctRequest:
                ReceivedDTO = OpenAcctDTO.Deserialize(DTOByteArray);
                break;
        }
    }

    public void RespondToClient() throws Exception {
        DTOType type = ReceivedDTO.getDtoType();

        switch (type) {
            case Order:
                OrderDTO orderDTO = (OrderDTO) ReceivedDTO;
                ResponseDTO = new OrderStatusDTO(orderDTO.getClientRequestID(), OrderStatusType.PartiallyFilled,
                        "Your market Order has been filled with 100 shares @ $300");
                break;
            case MarketDataRequest:
                MarketDataRequestDTO marketDataRequestDTO = (MarketDataRequestDTO) ReceivedDTO;
                switch (marketDataRequestDTO.getDataType()) {
                    case Level1:
                        ArrayList<MarketDataItem> bids = new ArrayList<MarketDataItem>();
                        bids.add(new MarketDataItem("AAPL",5000,129.01));
                        ArrayList<MarketDataItem> asks = new ArrayList<MarketDataItem>();
                        asks.add(new MarketDataItem("AAPL",2000,129.35));
                        ResponseDTO = new MarketDataDTO(marketDataRequestDTO.getClientRequestID(),"AAPL",bids,asks);
                        break;
                    case ContinuousLevel3:
                        break;
                }
                break;
            case PortfolioRequest:
                PortfolioRequestDTO portfolioRequestDTO = (PortfolioRequestDTO) ReceivedDTO;
                double cash = 12000000.879065;
                HashMap<String, SecurityCertificate> securities = new HashMap<>();
                securities.put("TSLA", new SecurityCertificate("user1", "TSLA",400, new Date()));
                securities.put("AAPL", new SecurityCertificate("user1", "AAPL",300, new Date()));
                ResponseDTO = new PortfolioDTO(portfolioRequestDTO.getClientRequestID(), securities, cash);
                break;
            case OpenAcctRequest:
                OpenAcctDTO openAcctDTO = (OpenAcctDTO) ReceivedDTO;
                MockCredentialWarehouse.put(openAcctDTO.getUserName(),openAcctDTO.getPassword());
                MessageType msgType = MessageType.SuccessMessage;
                String statusMessage =  openAcctDTO.getDtoType() + " completed successfully.";
                MessageDTO msgDTO = new MessageDTO(openAcctDTO.getClientRequestID(), msgType,statusMessage);
                ResponseDTO = msgDTO;
                break;
            case LoginRequest:
                LoginDTO loginDTO = (LoginDTO) ReceivedDTO;
                String userName = loginDTO.getUserName();
                String password = loginDTO.getPassword();
                Boolean loginSuccessful = false;
                String loginStatus = "";
                MessageType loginMessageType = MessageType.ErrorMessage;
                if(MockCredentialWarehouse.containsKey(userName)
                && MockCredentialWarehouse.get(userName).equals(password)) {
                    loginSuccessful = true;
                    loginMessageType = MessageType.SuccessMessage;
                }

                if(loginSuccessful) {
                    loginStatus = loginDTO.getDtoType() + " completed successfully.";
                } else {
                    loginStatus = loginDTO.getDtoType() + " failed.";
                }

                MessageDTO loginStatusDTO = new MessageDTO(loginDTO.getClientRequestID(), loginMessageType,loginStatus);
                ResponseDTO = loginStatusDTO;
        }

        byte[] DTOByteArray = ResponseDTO.Serialize();
        serverOutputStream.write(ResponseDTO.getDtoType().getByteValue());
        serverOutputStream.write((byte)DTOByteArray.length);
        serverOutputStream.write(DTOByteArray);
    }

    public Transferable getReceivedDTO() {
        return ReceivedDTO;
    }

    public Transferable getResponseDTO() {
        return ResponseDTO;
    }

    public void run() {
        try{
            while(true) {
                ReadRequestFromClient();
                RespondToClient();
            }
        } catch(Exception e) {
        }
    }
}
