import java.net.*;
import java.io.*;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.Scanner;

public class Server implements Runnable{

    private Socket clientSock;
    private BufferedReader fromUserReader;
    private PrintWriter toSockWriter;
    private static PrintWriter buzzerHit;
    private static boolean first = true;
    private static JFrame frame;
    private static JLabel firstBuzzLabel;
    private static List<PrintWriter> clientList = new ArrayList<PrintWriter>();

    public Server(Socket sock, BufferedReader reader, PrintWriter writer) {
        this.clientSock = sock;
        this.fromUserReader = reader;
        this.toSockWriter = writer;
        firstBuzzLabel = new JLabel();
        frame = new JFrame();
        frame.setSize(650, 335);
        frame.setTitle("Server");
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(new Color(194,194,194));
    }

    public static synchronized boolean addClient(PrintWriter toClientWriter) {
        return(clientList.add(toClientWriter));
    }

    public static synchronized boolean removeClient(PrintWriter toClientWriter) {
        return(clientList.remove(toClientWriter));
    }

    public static synchronized void relayFirstMessage(PrintWriter fromClientWriter, String teamName) {
        fromClientWriter.println("Congratulations! You hit the buzzer first");
        
        firstBuzzLabel.setText(teamName + " hit the buzzer first!");
        firstBuzzLabel.setFont(new Font("sans-serif", Font.BOLD, 30));

        JPanel firstBuzzPanel = new JPanel();
        firstBuzzPanel.setBounds(0, 50, 650, 50);

        firstBuzzPanel.add(firstBuzzLabel);
        frame.add(firstBuzzPanel);
        firstBuzzPanel.setVisible(true);
        frame.setVisible(true);
        
        for (PrintWriter user: clientList) {
            if (!(user.equals(fromClientWriter))){
                user.println(teamName + " hit the buzzer first");
            }
        }
    }

    public static synchronized void relayNotFirstMessage(PrintWriter fromClientWriter) {
        if (fromClientWriter == buzzerHit) {
            fromClientWriter.println("You hit the buzzer first, please answer");
        } else {
            fromClientWriter.println("Sorry, the buzzer was already hit");
        }
    }

    public static synchronized void relayResetMessage() {
        for (PrintWriter user: clientList) {
            user.println("The buzzer is now active, you may buzz in again");
        }
    } 

    public void run(){
        try {
            BufferedReader readFromSocket = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            String name = readFromSocket.readLine();
            System.out.println(name + " is in the game");
            PrintWriter buzzerWriter = new PrintWriter(clientSock.getOutputStream(),true);
            addClient(buzzerWriter);

            JLabel title = new JLabel();
            title.setText("Game Master");
            title.setFont(new Font("serif", Font.BOLD, 36));

            JPanel titlePanel = new JPanel();
            titlePanel.setBackground(new Color(194,194,194));
            titlePanel.setBounds(0,0,650,50);

            JPanel resetPanel = new JPanel();
            resetPanel.setBackground(new Color(194, 194, 194));
            resetPanel.setBounds(0,240,650,70);

            JButton reset = new JButton("Reset");
            reset.setFocusable(false);
            reset.setPreferredSize(new Dimension(100,50));

            JPanel scoreTextPanel = new JPanel();
            scoreTextPanel.setBounds(0,155,680,30);

            JLabel scoreTextLabel = new JLabel();
            scoreTextLabel.setFont(new Font("sans-serif", Font.PLAIN, 20));
            scoreTextLabel.setText("Enter score from previous question below and hit enter");

            JPanel scorePanel = new JPanel();
            scorePanel.setBounds(0,190,650,50);

            JTextField scoreField = new JTextField(5);
            scoreField.setFont(new Font("sans-serif", Font.BOLD, 30));

            titlePanel.add(title);
            resetPanel.add(reset); 
            scoreTextPanel.add(scoreTextLabel);
            scorePanel.add(scoreField);
            frame.add(titlePanel);
            frame.add(resetPanel);
            frame.add(scoreTextPanel);
            frame.add(scorePanel);
            titlePanel.setVisible(true);
            resetPanel.setVisible(true);
            scorePanel.setVisible(true);
            scoreTextPanel.setVisible(true);
            frame.setVisible(true);

            while(true) {
                reset.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        first = true;
                        buzzerHit = null;
                        firstBuzzLabel.setText("");
                        relayResetMessage();
                    }
                });

                String clientMesg = readFromSocket.readLine();
                if (clientMesg == null) {
                    break;
                }
                if (first == true) {
                    relayFirstMessage(buzzerWriter, name);
                    buzzerHit = buzzerWriter;
                    first = false;
                } else {
                    relayNotFirstMessage(buzzerWriter);
                }

                scoreField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        buzzerHit.println(scoreField.getText());
                        scoreField.setText("");
                    }
                });

            }
            removeClient(buzzerWriter);
        }
        catch (Exception e){
            System.out.println(e);
            System.exit(1);
        }
    }
    public static void main(String [] args) {

        Socket clientSock = null;
        try {
            ServerSocket serverSocket = new ServerSocket(50000);
            while (true) {
                clientSock = serverSocket.accept();
                BufferedReader fromUserReader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter toSockWriter = new PrintWriter(clientSock.getOutputStream(), true);
                Thread child = new Thread(new Server(clientSock, fromUserReader, toSockWriter));
                child.start();
            }
        } 
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        } 
    }
}
