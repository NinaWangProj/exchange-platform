package commonData.DTO;

public class PortfolioRequestDTO {
    private final DTOType dtoType;
    private final Long clientRequestID;

    public PortfolioRequestDTO(Long clientRequestID) {
        dtoType = DTOType.DepositRequest;
        this.clientRequestID = clientRequestID;
    }

    public byte[] Serialize() throws Exception{
        return new byte[0];
    }

    public DTOType getDtoType() {
        return dtoType;
    }

    public Long getClientRequestID() {
        return clientRequestID;
    }
}
