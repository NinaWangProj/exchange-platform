package nw.ExchangePlatform.data;

import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    HashMap<String, SecurityCertificate> portfolio;

    //HashMap<tickerSymbol, SecurityCertificate>
    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> portfolio){
        this.portfolio = portfolio;
    }
}
