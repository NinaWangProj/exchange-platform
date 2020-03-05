package nw.ExchangePlatform.data;

import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    HashMap<String, SecurityCertificate> securities;
    double cash;


    //HashMap<tickerSymbol, SecurityCertificate>
    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> securities){
        this.securities = securities;
    }
}
