import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LotteryClient
{
    public boolean FLAG = false;
    public List<List<Integer>> winnerTickets = new ArrayList<List<Integer>>();

    public MessageInterface initializeRMI() {
        MessageInterface rmiServer = null;
        Registry registry;
        String serverAddress = "127.0.0.1";
        String serverPort = "3232";
        System.out.println("sending request to " + serverAddress + ":" + serverPort);
        try {
            registry = LocateRegistry.getRegistry(serverAddress, (new Integer(serverPort)).intValue());
            rmiServer = (MessageInterface) (registry.lookup("rmiServer"));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return rmiServer;
    }
    /*Metoda koja komunicira sa serverom za dohvatanje izvucenog broja*/
    public Integer drawingNumbers(MessageInterface rmiServer) throws RemoteException, InterruptedException {
            int winnerNumber = rmiServer.generateLottoCombination();
            if (rmiServer.checkForWinner().get(true) != null){
                winnerTickets.add(rmiServer.checkForWinner().get(true));
                FLAG = true;
            }
        return winnerNumber;
    }
    /*Metoda koja komunicira sa serverom za dohvatanje generisanih 30 tiketa*/
    public LinkedHashMap<Integer, List<Integer>> generateTickets(MessageInterface rmiServer) throws RemoteException {
        List<List<Integer>> noArr = rmiServer.generateTickets();
        LinkedHashMap<Integer, List<Integer>> collectionOfTickets = new LinkedHashMap<Integer, List<Integer>>();
        int idOfTicket = rmiServer.returnMaxId() + 1;
        for (List<Integer> list : noArr) {
            collectionOfTickets.put(idOfTicket, list);
            idOfTicket++;
        }
        return collectionOfTickets;
    }
}
