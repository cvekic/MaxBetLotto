import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.*;

public class LotteryServer extends java.rmi.server.UnicastRemoteObject
        implements MessageInterface
{
    private static final double MIN = 1;
    private static final double MAX = 90;
    public static final int NUMBER_OF_TICKETS = 30;
    public static final int NUMBER_OF_NUMBERS_ON_EACH_TICKET = 15;
    private Integer maxId = 0;
    private List<Integer> differentNumbers = new ArrayList<Integer>();
    private List<List<Integer>> mainList=new ArrayList<List<Integer>>();
    private LinkedHashMap<Integer, List<Integer>> chosenCollection = new LinkedHashMap<Integer, List<Integer>>();
    private Map<Boolean, List<Integer>> winerTickets = new TreeMap<Boolean, List<Integer>>();
    public int thisPort;
    public String thisAddress;
    public Registry registry;
    private ConnectionToDB connectionToDB = new ConnectionToDB();

    /* Generisanje liste od 30 tiketa i prosledjivanje*/
    @Override
    public List<List<Integer>> generateTickets() throws RemoteException
    {
        mainList.clear();
        for(int m=0; m < NUMBER_OF_TICKETS; m++){
            mainList.add(generateNoInEachTicket());
        }
        return mainList;
    }
    /* Generisanje i dodavanje 15 brojeva u tiket i prosledjivanje tiketa  */
    private List<Integer> generateNoInEachTicket(){
        int randomNumber = 0;
        List<Integer> oneTicket=new ArrayList<Integer>();
        while(oneTicket.size() != NUMBER_OF_NUMBERS_ON_EACH_TICKET) {
            randomNumber = generateNumbersInRangeFrom1To90();
            if(!oneTicket.contains(randomNumber)) {
                oneTicket.add(randomNumber);
            }
        }
        return oneTicket;
    }
    /*Generisanje izvucenog broja*/
    @Override
    public Integer generateLottoCombination() throws RemoteException {
        int winningNumber = generateNumbersInRangeFrom1To90();
        while(differentNumbers.contains(winningNumber)) {
            winningNumber = generateNumbersInRangeFrom1To90();
        }
        differentNumbers.add(winningNumber);
        return winningNumber;
    }
    /*Metoda koja se poziva da bi se pripremile odredjene promjenjive za novo kolo*/
    @Override
    public void clearList() throws RemoteException {
        winerTickets.clear();
        maxId = 0;
    }
    /*Dohvatanje id zadnjeg tiketa u bazi*/
    @Override
    public Integer returnMaxId() throws RemoteException {
        try {
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
    /*Metoda za unos tiketa u bazu podataka*/
    @Override
    public void insertIntoDB(Integer id, String combination) throws RemoteException {
        try {
            connectionToDB.insertIntoDB(id, combination);
            List<Integer> listForHoldingFormattedInteger = new ArrayList<Integer>();
            combination = combination.replace(" ", "");
            List<String> tempArrayForFormattingList = Arrays.asList(combination.split(","));
            for(String s : tempArrayForFormattingList) {
                Integer tempInt = Integer.parseInt(s);
                listForHoldingFormattedInteger.add(tempInt);
            }
            chosenCollection.put(id, listForHoldingFormattedInteger);
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
    /*Metoda za brisanje tiketa iz baze podataka*/
    @Override
    public void deleteFromDB(Integer id) throws RemoteException {
        try {
            chosenCollection.remove(id);
            connectionToDB.deleteFromDB(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*Provjeravanje da li su izvuceni svi brojevi na bilo kom listicu koji je u igri*/
    @Override
    public Map<Boolean, List<Integer>> checkForWinner() throws RemoteException {
        boolean flag = false;
        if (differentNumbers.size() >= NUMBER_OF_NUMBERS_ON_EACH_TICKET) {
            for (List<Integer> list : chosenCollection.values()) {
                if(differentNumbers.containsAll(list)) {
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
    /*Generisanje random broja u intervalu [1,90]*/
    private int generateNumbersInRangeFrom1To90() {
        return (int) (MIN + (int)(Math.random() * ((MAX - MIN) + 1)));
    }

    /*RMI konfiguracija i pokretanje RMI Servera*/
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
