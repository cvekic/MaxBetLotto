import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface MessageInterface extends Remote
{
    List<List<Integer>> generateTickets() throws RemoteException;
    Integer generateLottoCombination() throws RemoteException;
    Map<Boolean, List<Integer>> checkForWinner() throws RemoteException;
    void clearList() throws RemoteException;
    Integer returnMaxid() throws RemoteException;
    void insertIntoDB(Integer id, String combination) throws RemoteException;
    void deleteFromDB(Integer id) throws RemoteException;
}
