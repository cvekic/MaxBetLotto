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

    public MessageInterface InicializeRMI() {
        MessageInterface rmiServer = null;
        Registry registry;
        String serverAddress = "127.0.0.1";
        String serverPort = "3232";
        System.out.println("sending request to " + serverAddress + ":" + serverPort);
        try {
            // get the “registry”
            registry = LocateRegistry.getRegistry(serverAddress, (new Integer(serverPort)).intValue());
            // look up the remote object
            rmiServer = (MessageInterface) (registry.lookup("rmiServer"));
            // call the remote method
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return rmiServer;
    }
    public Integer drawingNumbers(MessageInterface rmiServer) throws RemoteException, InterruptedException {
            int winnerNumber = rmiServer.generateLottoCombination();
            System.out.println(winnerNumber);
            if (rmiServer.checkForWinner().get(true) != null){
                winnerTickets.add(rmiServer.checkForWinner().get(true));
            }
        for(Boolean key : rmiServer.checkForWinner().keySet()) {
            System.out.println(winnerTickets);
            if(key) {
                FLAG = key;
            }
            else {
                continue;
            }
        }
        return winnerNumber;
    }
    public LinkedHashMap<Integer, List<Integer>> generateTickets(MessageInterface rmiServer) throws RemoteException {
        List<List<Integer>> noArr = rmiServer.generateTickets();
        LinkedHashMap<Integer, List<Integer>> collectionOfTickets = new LinkedHashMap<Integer, List<Integer>>();
        int numberOfTicket = rmiServer.returnMaxid() + 1;
        for (List<Integer> list : noArr) {
            collectionOfTickets.put(numberOfTicket, list);
            numberOfTicket++;
        }
        return collectionOfTickets;
    }
}
