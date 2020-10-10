package clearing.data;

import com.opencsv.bean.CsvBindByName;
import org.graalvm.compiler.nodes.StructuredGraph;

public class CredentialRow {
    @CsvBindByName
    public final String UserName;
    @CsvBindByName
    public final String Password;
    @CsvBindByName
    public final int UserID;

    public CredentialRow() {
        this.UserName = "DefaultUser";
        this.Password = "DefaultPassword";
        this.UserID = -1;
    }

    public CredentialRow(String UserName, String Password, int UserID) {
        this.UserName = UserName;
        this.Password = Password;
        this.UserID = UserID;
    }

}
