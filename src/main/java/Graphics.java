import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

/**
 * Created by Mladjan on 29.3.2014.
 */
public class Graphics extends javax.swing.JFrame implements ItemListener {

    public JPanel panel1;
    public JButton pokreniIgruButton;
    private JTextPane textPane1;
    private JTextPane textPane3;
    private JPanel p2;

    Boolean flagForSelectedTicket = false;
    protected static final int INACTIVATE_DELAY_11_SECONDS = 11000;
    protected static final int INACTIVATE_DELAY_ONE_SECOND = 1000;
    private int secondsToStartGame = 10;
    LotteryClient lotteryClient = new LotteryClient();
    MessageInterface rmiServer;
    LinkedHashMap<Integer, List<Integer>> collectionOfTickets = new LinkedHashMap<Integer, List<Integer>>();
    List<JCheckBox> check = new ArrayList<JCheckBox>();

    public static void likeMain() {
        JFrame frame = new JFrame("Graphics");
        frame.setContentPane(new Graphics().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public Graphics() {
        StyledDocument doc = textPane1.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs = textPane1.getInputAttributes();
        int size = StyleConstants.getFontSize(attrs);
        StyleConstants.setFontSize(attrs, size * 3);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        pokreniIgruButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pokreniIgruButton.setEnabled(false);
                textPane3.setText("");
                rmiServer = lotteryClient.InicializeRMI();
                try {
                    collectionOfTickets = lotteryClient.generateTickets(rmiServer);
                    p2.removeAll();
                    p2.updateUI();
                    p2.setLayout( new GridLayout( collectionOfTickets.size(),2) );
                    for (Integer key : collectionOfTickets.keySet()) {
                        updateP2(collectionOfTickets.size(), key);
                        String name = key.toString();
                        System.out.println(key);

                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                final Timer timerForWriteEverySecondOnScreen = new Timer(INACTIVATE_DELAY_ONE_SECOND, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            textPane1.setText("Igra pocinje za: " + "\n" + Integer.toString(secondsToStartGame) + "\n" + "sekundi");
                            secondsToStartGame--;
                    }
                });
                // don't allow repeats
                timerForWriteEverySecondOnScreen.setRepeats(true);
                // tell timer to start
                timerForWriteEverySecondOnScreen.start();


                final Timer timer1 = new Timer(INACTIVATE_DELAY_ONE_SECOND, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                            if(!lotteryClient.FLAG){
                                Integer drawnNumber = null;
                                try {
                                    drawnNumber = lotteryClient.drawingNumbers(rmiServer);
                                } catch (RemoteException e1) {
                                    e1.printStackTrace();
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                                textPane1.setText("Izvuceni broj je: " + "\n" + String.valueOf(drawnNumber));
                            }
                        else {
                                lotteryClient.FLAG = false;
                                ((Timer)e.getSource()).stop();
                                for(List<Integer> list : lotteryClient.winnerTickets){
                                    appendPanel3(list.toString());
                                }
                                textPane1.setText("WINNER");
                                flagForSelectedTicket = false;
                                check.clear();

                                lotteryClient.winnerTickets.clear();
                                pokreniIgruButton.setEnabled(true);
                                try {
                                    rmiServer.clearList();
                                } catch (RemoteException e1) {
                                    e1.printStackTrace();
                                }
                            }
                    }

                });
                // don't allow repeats
                timer1.setRepeats(true);
                // tell timer to start


                Timer timerForGenerateDelayFor10Seconds = new Timer(INACTIVATE_DELAY_11_SECONDS, new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        timerForWriteEverySecondOnScreen.stop();
                        secondsToStartGame = 10;

                        textPane1.setText("");
                        for(JCheckBox box : check) {
                            box.setEnabled(false);
                        }

                        if (flagForSelectedTicket) {
                            textPane3.setText("Ocekujemo dobitni listic!");
                            timer1.start();
                        }
                        else{
                            textPane3.setText("Niste izabrali ni jedan listic!");
                            check.clear();
                            lotteryClient.winnerTickets.clear();
                            pokreniIgruButton.setEnabled(true);
                            try {
                                rmiServer.clearList();
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }

                        }
                    }
                });
                // don't allow repeats
                timerForGenerateDelayFor10Seconds.setRepeats(false);
                // tell timer to start
                timerForGenerateDelayFor10Seconds.start();
            }
        });
    }

    private void updateP2( int noOfCheckBoxes, int key )
    {
        JCheckBox box;
        // JFrame frame = new JFrame();
        // frame.setSize(new Dimension(50, 50));
        box = new JCheckBox("This is " + key );
        //JCheckBox button = new JCheckBox("This is 1");
        final JPanel buttonWrapper = new JPanel();
        //buttonWrapper.add(new JLabel("Pregled tiketa"));
        // buttonWrapper.add(box);
        // buttonWrapper.setBorder(BorderFactory.createRaisedBevelBorder());
        box.addItemListener(this);
        // p2.add(buttonWrapper);
        p2.add(box);
        check.add(box);
    }

    public void appendPanel3(String s) {
        try {
            Document doc = textPane3.getDocument();
            doc.insertString(doc.getLength(), s + "\n", null);
        } catch(BadLocationException exc) {
            exc.printStackTrace();
        }
    }
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                likeMain();
            }
        });
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        int index = 0;
        for (JCheckBox box : check) {
            index++;
            if (source == box)
            {
                break;
            }
        }
        //Now that we know which button was pushed, find out
        //whether it was selected or deselected.
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            flagForSelectedTicket = false;
            if (index > 0 && index < 31) {
                deleteFromDB(index);
                textPane3.setText("");
            }
        }
        //Apply the change to the string.
        if (e.getStateChange() == ItemEvent.SELECTED) {
            flagForSelectedTicket = true;
            if (index > 0 && index < 31) {
                showInTextPane3(index);
                insertToDb(index);
            }
        }
    }
    private void insertToDb(int index) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(index-1);
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        String formatedString = list.toString()
                .replace("[", "")   //remove the right bracket
                .replace("]", "");
        try {
            rmiServer.insertIntoDB(value, formatedString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void deleteFromDB(int index) {
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        System.out.println("Radi nesto " + Integer.toString(value));
        try {
            rmiServer.deleteFromDB(value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void showInTextPane3(int i) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(i-1);
        textPane3.setText(list.toString());
    }
}
