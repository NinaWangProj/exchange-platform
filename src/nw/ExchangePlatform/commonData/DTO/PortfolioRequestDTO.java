package nw.ExchangePlatform.commonData.DTO;

public class PortfolioRequestDTO {
    private final DTOType dtoType;

    public PortfolioRequestDTO() {
        dtoType = DTOType.DepositRequest;
    }

    public byte[] Serialize() throws Exception{
        return new byte[0];
    }

    public DTOType getDtoType() {
        return dtoType;
    }

}
