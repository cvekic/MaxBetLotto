import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Mladjan on 29.3.2014.
 */
public class Graphics extends javax.swing.JFrame implements ItemListener, ActionListener{

    public JPanel panel1;
    public JButton pokreniIgruButton;
    private JTextPane textPane1;
    private JTextPane textPane3;
    private JPanel p2;
    private JTextPane textPane2;

    Boolean flagForSelectedTicket = false;
    protected static final int INACTIVATE_DELAY_11_SECONDS = 11000;
    protected static final int INACTIVATE_DELAY_ONE_SECOND = 1000;
    private int secondsToStartGame = 10;
    LotteryClient lotteryClient = new LotteryClient();
    MessageInterface rmiServer;
    LinkedHashMap<Integer, List<Integer>> collectionOfTickets = new LinkedHashMap<Integer, List<Integer>>();
    List<JCheckBox> listCheckBoxes = new ArrayList<JCheckBox>();
    List<JButton> listButtons = new ArrayList<JButton>();

    public static void likeMain() {
        JFrame frame = new JFrame("Max Bet Lotto");
        frame.setContentPane(new Graphics().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public Graphics() {
        setStyleToTextPane1();
        setStyleToTextPane2();
        setStyleToTextPane3();
        pokreniIgruButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pokreniIgruButton.setEnabled(false);
                textPane3.setText("");
                textPane2.setText("");
                rmiServer = lotteryClient.initializeRMI();
                try {
                    collectionOfTickets = lotteryClient.generateTickets(rmiServer);
                    p2.removeAll();
                    p2.updateUI();
                    p2.setLayout(new GridLayout(collectionOfTickets.size(), 2));
                    for (Integer key : collectionOfTickets.keySet()) {
                        updateP2(collectionOfTickets.size(), key);
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
                        if (!lotteryClient.FLAG) {
                            Integer drawnNumber = null;
                            try {
                                drawnNumber = lotteryClient.drawingNumbers(rmiServer);
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                            textPane1.setText("Izvuceni broj je: " + "\n" + String.valueOf(drawnNumber));
                            appendTextToTextPane2(drawnNumber);
                        } else {
                            lotteryClient.FLAG = false;
                            ((Timer) e.getSource()).stop();
                            appendTextToTextPane3();
                            textPane1.setText("WINNER");
                            flagForSelectedTicket = false;
                            listCheckBoxes.clear();
                            listButtons.clear();
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
                        for (JCheckBox box : listCheckBoxes) {
                            if (box.isSelected()) {
                                flagForSelectedTicket = true;
                            }
                            box.setEnabled(false);
                        }
                        for (JButton button : listButtons) {
                            button.setEnabled(false);
                        }

                        if (flagForSelectedTicket) {
                            textPane3.setText("Cekanje na dobitni listic!");
                            textPane2.setText("Izvuceni brojevi su:\n");
                            timer1.start();
                        } else {
                            textPane3.setText("Niste izabrali ni jedan listic!");
                            listCheckBoxes.clear();
                            listButtons.clear();
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

    private void setStyleToTextPane3() {
        StyledDocument doc3 = textPane3.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs3 = textPane3.getInputAttributes();
        int size3 = StyleConstants.getFontSize(attrs3);
        StyleConstants.setFontSize(attrs3, size3 * 3);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc3.setParagraphAttributes(0, doc3.getLength(), center, false);
    }

    private void setStyleToTextPane2() {
        StyledDocument doc2 = textPane2.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs2 = textPane2.getInputAttributes();
        int size2 = StyleConstants.getFontSize(attrs2);
        StyleConstants.setFontSize(attrs2, size2 * 2);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc2.setParagraphAttributes(0, doc2.getLength(), center, false);
    }

    private void setStyleToTextPane1() {
        StyledDocument doc = textPane1.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs = textPane1.getInputAttributes();
        int size = StyleConstants.getFontSize(attrs);
        StyleConstants.setFontSize(attrs, size * 5);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    private void updateP2( int noOfCheckBoxes, int key )
    {
        JCheckBox box;
        box = new JCheckBox();
        Color color=new Color(30, 99, 191);
        box.setBackground(color);
        JButton buttonLikeLabel = new JButton("        ID " + key);
        buttonLikeLabel.setBorderPainted(false);
        buttonLikeLabel.setContentAreaFilled(false);
        buttonLikeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        buttonLikeLabel.setForeground(Color.white);
        box.addItemListener(this);
        buttonLikeLabel.addActionListener(this);
        buttonLikeLabel.add(box);
        p2.add(buttonLikeLabel);
        listButtons.add(buttonLikeLabel);
        listCheckBoxes.add(box);
    }
    private void appendTextToTextPane2(Integer drawnNumber) {
        try {
            Document doc = textPane2.getDocument();
            String formatedString = Integer.toString(drawnNumber);
            doc.insertString(doc.getLength(), formatedString + ", ", null);
        } catch(BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    public void appendTextToTextPane3() {
        String name = "Dobitna kombinacija je:";
        String formatedString;
        String finalString = "";
        for (List<Integer> list : lotteryClient.winnerTickets) {
            formatedString = list.toString().replace("[", "").replace("]", "");
            finalString += name + "\n" + formatedString + "\n";
        }
        textPane3.setText(finalString);
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
        Object sourceBox = e.getItemSelectable();
        int indexBox = 0;
        for (JCheckBox box : listCheckBoxes) {
            indexBox++;
            if (sourceBox == box)
            {
                break;
            }
        }
        //Apply the change to the string.
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (indexBox > 0 && indexBox < 31) {
                insertToDb(indexBox);
                showInTextPane3(indexBox);
            }
        }
        //Now that we know which button was pushed, find out
        //whether it was selected or deselected.
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            if (indexBox > 0 && indexBox < 31) {
                deleteFromDB(indexBox);
                textPane3.setText("Tiket je izbrisan iz baze!");
            }
        }

    }
    private void insertToDb(int index) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(index-1);
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        String formatedString = list.toString().replace("[", "").replace("]", "");
        try {
            rmiServer.insertIntoDB(value, formatedString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void deleteFromDB(int index) {
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        try {
            rmiServer.deleteFromDB(value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    private void showInTextPane3(int i) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(i-1);
        Integer id = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(i-1);
        String formatedString = "Kombinacija za tiket ID #" + Integer.toString(id) + "\n\n" + list.toString().replace("[", "").replace("]", "");
        textPane3.setText(formatedString);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int indexButton = 0;
        for (JButton button : listButtons) {
            indexButton++;
            if(source == button) {
                showInTextPane3(indexButton);
            }
        }
    }
}
