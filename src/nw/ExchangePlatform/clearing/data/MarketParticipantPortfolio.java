package nw.ExchangePlatform.clearing.data;

import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    public HashMap<String, SecurityCertificate> securities;
    public double cash;

    //HashMap<tickerSymbol, SecurityCertificate>
    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> securities){
        this.securities = securities;
    }
}
