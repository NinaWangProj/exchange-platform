package nw.ExchangePlatform.clearing.data;

import java.util.HashMap;

public class MarketParticipantPortfolio {

    //<tickerSymbol, SecurityCertificate>
    private HashMap<String, SecurityCertificate> securities;
    private double cash;

    //HashMap<tickerSymbol, SecurityCertificate>
    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> securities){
        this.securities = securities;
        cash = 0.0;
    }

    public MarketParticipantPortfolio(){
        securities = new HashMap<>();
        cash = 0.0;
    }

    public MarketParticipantPortfolio(HashMap<String, SecurityCertificate> securities, double cash){
        this.securities = securities;
        this.cash = cash;
    }

    public HashMap<String, SecurityCertificate> getSecurities() {
        return securities;
    }

    public double getCashAmt() {
        return cash;
    }

    public void DepositCash(double cash) {
        this.cash += cash;
    }
}
