package commonData.DTO;

public interface Transferable {
    public DTOType getDtoType();
    public byte[] Serialize() throws Exception;
}
