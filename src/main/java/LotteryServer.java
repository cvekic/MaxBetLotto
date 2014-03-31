import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.*;

public class LotteryServer extends java.rmi.server.UnicastRemoteObject
        implements MessageInterface
{
    public static final double MIN = 1;
    public static final double MAX = 90;
    Integer maxId = 0;
    List<Integer> differentNumbers = new ArrayList<Integer>();
    List<List<Integer>> mainList=new ArrayList<List<Integer>>();
    LinkedHashMap<Integer, List<Integer>> chosenCollection = new LinkedHashMap<Integer, List<Integer>>();
    Map<Boolean, List<Integer>> winerTickets = new TreeMap<Boolean, List<Integer>>();
    int thisPort;
    String thisAddress;
    Registry registry;
    ConnectionToDB connectionToDB = new ConnectionToDB();
    // rmi registry for lookup the remote objects.
    // This method is called from the remote client by the RMI.
    // This is the implementation of the “MessageInterface”.
    @Override
    public List<List<Integer>> generateTickets() throws RemoteException
    {
        mainList.clear();
        for(int m=0; m < 30; m++){
            mainList.add(generateNos());
        }
        System.out.println("Process Finished!");
        return mainList;
    }
    /* Generate and add numbers b/w 1-90 in vector */
    private List<Integer> generateNos(){
        int randomNumber = 0;
        List<Integer> oneTicket=new ArrayList<Integer>();
        while(oneTicket.size() != 15) {
            randomNumber = generateNumbersInRangeFrom1To90();
            if(!oneTicket.contains(randomNumber)) {
                oneTicket.add(randomNumber);
            }
        }
        return oneTicket;
    }

    @Override
    public Integer generateLottoCombination() throws RemoteException {
        int winningNumber = generateNumbersInRangeFrom1To90();
        while(differentNumbers.contains(winningNumber)) {
            winningNumber = generateNumbersInRangeFrom1To90();
        }
        differentNumbers.add(winningNumber);
        return winningNumber;
    }

    @Override
    public void clearList() throws RemoteException {
        winerTickets.clear();
        maxId = 0;
    }

    @Override
    public Integer returnMaxid() throws RemoteException {

        try {
//            List<Integer> proba = connectionToDB.getAllTickets();
//            System.out.println(proba.toString());
            for (Integer i : connectionToDB.getAllTickets()) {
                if (i > maxId) {
                    maxId = i;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    @Override
    public void insertIntoDB(Integer id, String combination) throws RemoteException {
        try {
            connectionToDB.insertIntoDB(id, combination);
            List<Integer> tepmList = new ArrayList<Integer>();
            combination = combination.replace(" ", "");
            List<String> tempArray = Arrays.asList(combination.split(","));
            for(String s : tempArray) {
                Integer tempInt = Integer.parseInt(s);
                tepmList.add(tempInt);
            }
            chosenCollection.put(id, tepmList);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFromDB(Integer id) throws RemoteException {
        try {
            chosenCollection.remove(id);
            connectionToDB.deleteFromDB(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Boolean, List<Integer>> checkForWinner() throws RemoteException {
        boolean flag = false;

        if (differentNumbers.size() >= 15) {
            for (List<Integer> list : chosenCollection.values()) {
                if(differentNumbers.containsAll(list)) {
                   System.out.println(list);
                   flag = true;
                   winerTickets.put(flag, list);
                }
            }
            if (flag) {
                differentNumbers.clear();
            }
        }
        return winerTickets;
    }

    private int generateNumbersInRangeFrom1To90() {
        return (int) (MIN + (int)(Math.random() * ((MAX - MIN) + 1)));
    }

    public LotteryServer() throws RemoteException
    {
        try{
            thisAddress= "127.0.0.1"; // to run on localhost
        }
        catch(Exception e){
            throw new RemoteException("can't get inet address.");
        }
        thisPort=3232; // this port(registry's port)
        System.out.println("Lottery Server running at address="+thisAddress+",port="+thisPort);
        try{
        // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry( thisPort );
            registry.rebind("rmiServer", this);
        }
        catch(RemoteException e){
            throw e;
        }
    }
    static public void main(String args[])
    {
        try{
            LotteryServer server = new LotteryServer();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
