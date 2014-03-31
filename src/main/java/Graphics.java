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

    protected static final int INACTIVATE_DELAY_11_SECONDS = 11000;
    protected static final int INACTIVATE_DELAY_ONE_SECOND = 1000;
    private int secondsToStartGame = 10;
    public JPanel mainPanel;
    private JPanel panelForCBForTickets;
    public JButton startTheGameButton;
    private JTextPane textPaneForShowingGeneratedNumber;
    private JTextPane textPaneForShowingDetailsOfTicket;
    private JTextPane textPaneForDrawingHistory;
    private Boolean flagForSelectedTicket = false;
    private LotteryClient lotteryClient = new LotteryClient();
    private MessageInterface rmiServer;
    private LinkedHashMap<Integer, List<Integer>> collectionOfTickets = new LinkedHashMap<Integer, List<Integer>>();
    private List<JCheckBox> listCheckBoxes = new ArrayList<JCheckBox>();
    private List<JButton> listButtons = new ArrayList<JButton>();

    public static void likeMain() {
        JFrame frame = new JFrame("MaxBet Lotto");
        frame.setContentPane(new Graphics().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public Graphics() {
        setStyleToTextPaneForShowingGeneratedNumber();
        setStyleToTextPaneForDrawingHistory();
        setStyleToTextPaneForShowingDetailsOfTicket();
        textPaneForShowingDetailsOfTicket.setText("Za pocetak kola kliknite na dugme Pocetak Igre");
        textPaneForDrawingHistory.setText("Mladjan Cvijanovic");
        textPaneForShowingGeneratedNumber.setText("MaxBet Lotto");

        /*Osluskivac za dugme koje pokrece igru*/
        startTheGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTheGameButton.setEnabled(false);
                textPaneForShowingDetailsOfTicket.setText("");
                appendTextToTextPaneForDrawingHistory(1);
                textPaneForDrawingHistory.setText("");
                rmiServer = lotteryClient.initializeRMI();
                try {
                    collectionOfTickets = lotteryClient.generateTickets(rmiServer);
                    panelForCBForTickets.removeAll();
                    panelForCBForTickets.updateUI();
                    panelForCBForTickets.setLayout(new GridLayout(collectionOfTickets.size(), 2));
                    for (Integer key : collectionOfTickets.keySet()) {
                        updatePanelForCBForTickets(key);
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
                final Timer timerForWriteEverySecondOnScreen = setTimerForCountDownTeenSecondsBeforeStartGame();
                final Timer timer1 = setTimerForDrawingTheNumbers();
                setTimerForMakeDelayOfTeenSecondForPickingTheTickets(timerForWriteEverySecondOnScreen, timer1);
            }
        });
    }

    private void setTimerForMakeDelayOfTeenSecondForPickingTheTickets(final Timer timerForWriteEverySecondOnScreen, final Timer timer1) {
        Timer timerForGenerateDelayFor10Seconds = new Timer(INACTIVATE_DELAY_11_SECONDS, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timerForWriteEverySecondOnScreen.stop();
                secondsToStartGame = 10;
                textPaneForShowingGeneratedNumber.setText("");
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
                    textPaneForShowingDetailsOfTicket.setText("Cekanje na dobitni listic!");
                    setStyleToTextPaneForDrawingHistory();
                    textPaneForDrawingHistory.setText("Izvuceni brojevi su:\n");
                    timer1.start();
                } else {
                    textPaneForShowingDetailsOfTicket.setText("Niste izabrali ni jedan listic!");
                    listCheckBoxes.clear();
                    listButtons.clear();
                    lotteryClient.winnerTickets.clear();
                    startTheGameButton.setEnabled(true);
                    try {
                        rmiServer.clearList();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        });
        timerForGenerateDelayFor10Seconds.setRepeats(false);
        timerForGenerateDelayFor10Seconds.start();
    }

    private Timer setTimerForDrawingTheNumbers() {
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
                    textPaneForShowingGeneratedNumber.setText("Izvuceni broj je: " + "\n\n" + String.valueOf(drawnNumber));
                    appendTextToTextPaneForDrawingHistory(drawnNumber);
                } else {
                    lotteryClient.FLAG = false;
                    ((Timer) e.getSource()).stop();
                    appendTextToTextPaneForShowingDetailsOfTicket();
                    textPaneForShowingGeneratedNumber.setText("WINNER\nZa novo kolo kliknite na dugme!");
                    flagForSelectedTicket = false;
                    listCheckBoxes.clear();
                    listButtons.clear();
                    lotteryClient.winnerTickets.clear();
                    startTheGameButton.setEnabled(true);
                    try {
                        rmiServer.clearList();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        });
        timer1.setRepeats(true);
        return timer1;
    }

    private Timer setTimerForCountDownTeenSecondsBeforeStartGame() {
        final Timer timerForWriteEverySecondOnScreen = new Timer(INACTIVATE_DELAY_ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textPaneForShowingGeneratedNumber.setText("Igra pocinje za: " + "\n" + Integer.toString(secondsToStartGame) + "\n" + "sekundi");
                secondsToStartGame--;
            }
        });
        timerForWriteEverySecondOnScreen.setRepeats(true);
        timerForWriteEverySecondOnScreen.start();
        return timerForWriteEverySecondOnScreen;
    }

    private void setStyleToTextPaneForShowingGeneratedNumber() {
        StyledDocument doc = textPaneForShowingGeneratedNumber.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs = textPaneForShowingGeneratedNumber.getInputAttributes();
        int size = StyleConstants.getFontSize(attrs);
        StyleConstants.setFontSize(attrs, size * 5);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }

    private void setStyleToTextPaneForDrawingHistory() {
        StyledDocument doc2 = textPaneForDrawingHistory.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs2 = textPaneForDrawingHistory.getInputAttributes();
        int size2 = StyleConstants.getFontSize(attrs2);
        StyleConstants.setFontSize(attrs2, size2 * 2);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc2.setParagraphAttributes(0, doc2.getLength(), center, false);
    }

    private void setStyleToTextPaneForShowingDetailsOfTicket() {
        StyledDocument doc3 = textPaneForShowingDetailsOfTicket.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        MutableAttributeSet attrs3 = textPaneForShowingDetailsOfTicket.getInputAttributes();
        int size3 = StyleConstants.getFontSize(attrs3);
        StyleConstants.setFontSize(attrs3, size3 * 3);
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc3.setParagraphAttributes(0, doc3.getLength(), center, false);
    }

    private void appendTextToTextPaneForDrawingHistory(Integer drawnNumber) {
        try {
            Document doc = textPaneForDrawingHistory.getDocument();
            String formatedString = Integer.toString(drawnNumber);
            doc.insertString(doc.getLength(), formatedString + ", ", null);
        } catch(BadLocationException exc) {
            exc.printStackTrace();
        }
    }

    public void appendTextToTextPaneForShowingDetailsOfTicket() {
        String name = "Dobitna kombinacija je:";
        String formatedString;
        String finalString = "";
        for (List<Integer> list : lotteryClient.winnerTickets) {
            formatedString = list.toString().replace("[", "").replace("]", "");
            finalString += name + "\n" + formatedString + "\n";
        }
        textPaneForShowingDetailsOfTicket.setText(finalString);
    }

    private void showTextInTextPaneForShowingDetailsOfTicket(int index) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(index-1);
        Integer id = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        String formatedString = "Kombinacija za tiket ID #" + Integer.toString(id) + "\n\n" + list.toString().replace("[", "").replace("]", "");
        textPaneForShowingDetailsOfTicket.setText(formatedString);
    }

    private void updatePanelForCBForTickets(int key)
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
        panelForCBForTickets.add(buttonLikeLabel);
        listButtons.add(buttonLikeLabel);
        listCheckBoxes.add(box);
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
    private void insertToDb(int index) {
        List<Integer> list = (new ArrayList<List<Integer>>(collectionOfTickets.values())).get(index - 1);
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index-1);
        String formatedString = list.toString().replace("[", "").replace("]", "");
        try {
            rmiServer.insertIntoDB(value, formatedString);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void deleteFromDB(int index) {
        Integer value = (new ArrayList<Integer>(collectionOfTickets.keySet())).get(index - 1);
        try {
            rmiServer.deleteFromDB(value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (indexBox > 0 && indexBox < 31) {
                insertToDb(indexBox);
                showTextInTextPaneForShowingDetailsOfTicket(indexBox);
            }
        }
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            if (indexBox > 0 && indexBox < 31) {
                deleteFromDB(indexBox);
                textPaneForShowingDetailsOfTicket.setText("Tiket je izbrisan iz baze!");
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int indexButton = 0;
        for (JButton button : listButtons) {
            indexButton++;
            if(source == button) {
                showTextInTextPaneForShowingDetailsOfTicket(indexButton);
            }
        }
    }
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                likeMain();
            }
        });
    }
}
