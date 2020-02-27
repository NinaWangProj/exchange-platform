package nw.ExchangePlatform.data;

        import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    HashMap<String, SecurityCertificate> portfolio = new HashMap<>();

    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> portfolio){
        this.portfolio = portfolio;
    }
}
