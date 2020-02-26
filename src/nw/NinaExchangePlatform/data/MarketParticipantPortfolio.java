package nw.NinaExchangePlatform.data;

import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    HashMap<String, SecurityCerfiticate> portfolio = new HashMap<String, SecurityCerfiticate>();

    public MarketParticipantPortfolio(HashMap<String, SecurityCerfiticate> portfolio){
        this.portfolio = portfolio;
    }
}
