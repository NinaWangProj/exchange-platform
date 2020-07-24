package nw.ExchangePlatform.clearing.data;

import java.util.Date;

public class SecurityCertificate {

    //fields
    public String shareHolderName;
    public String tickerSymbol;
    public int quantity;
    public Date issuedDate;

    //constructor
    public SecurityCertificate(String shareHolderName, String tickerSymbol, int quantity, Date issuedDate)
    {
        this.shareHolderName = shareHolderName;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.issuedDate = issuedDate;
    }
}
