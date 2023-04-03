import java.net.*;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Client implements Runnable {

    private BufferedReader fromUserReader;
    private PrintWriter toSockWriter;
    private String name;
    private static JFrame frame;

    public Client(BufferedReader reader, PrintWriter writer, String name) {
        this.fromUserReader = reader;
        this.toSockWriter = writer;
        this.name = name;
        frame = new JFrame();
        frame.setSize(650,335);
        frame.setTitle("Buzzer");
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(new Color(194,194,194));
    }

    public void run() {
        try {
            toSockWriter.println(name);

            JButton buzzer = new JButton("Buzz In");
            buzzer.setFocusable(false);
            buzzer.setPreferredSize(new Dimension(100,50));

            JPanel buzzerPanel = new JPanel();
            buzzerPanel.setBounds(0,150,650,100);
            
            buzzerPanel.add(buzzer);
            frame.add(buzzerPanel);
            
            while(true) {
                buzzer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        toSockWriter.println("buzz");
                    }
                });

                String line;
                line = this.fromUserReader.readLine();
                if (line == "end") {
                    toSockWriter.println(line);
                    System.out.println("Game is over");
                    break;
                }
            }
            toSockWriter.close();
        }

        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        
        System.exit(0);
    }
    public static void main(String [] args) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter your team name: ");
        String name = input.nextLine();
        int score = 0;
        
        Socket sock = null;
        
        try{
            sock = new Socket("10.2.1.95", 50000);
            System.out.println("You are in the game");
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            PrintWriter toSockWriter = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader fromUserReader = new BufferedReader(new InputStreamReader(System.in));

            Thread child = new Thread(new Client(fromUserReader, toSockWriter, name));
            child.start();
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            BufferedReader fromSockReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            JLabel teamName = new JLabel(name, JLabel.CENTER);
            teamName.setForeground(Color.BLACK);
            teamName.setFont(new Font("Serif", Font.BOLD, 36));

            JLabel label1 = new JLabel("", JLabel.CENTER);
            label1.setForeground(Color.BLACK);
            label1.setFont(new Font("Sans-serif", Font.PLAIN, 24));

            JPanel namePanel = new JPanel();
            namePanel.setBackground(new Color(194,194,194));
            namePanel.setBounds(0, 0, 650, 50);
            namePanel.add(teamName);

            JPanel answerPanel = new JPanel();
            answerPanel.setBackground(new Color(194,194,194));
            answerPanel.setBounds(0,250,650,50);
            answerPanel.add(label1);

            JLabel scoreLabel = new JLabel();
            scoreLabel.setText("Score: " + score);
            scoreLabel.setFont(new Font("Sans-serif", Font.PLAIN, 24));

            JPanel scorePanel = new JPanel();
            scorePanel.setBounds(0,75,650,50);
            scorePanel.add(scoreLabel);

            frame.add(answerPanel);
            frame.add(namePanel);
            frame.add(scorePanel);
            namePanel.setVisible(true);
            answerPanel.setVisible(true);
            scorePanel.setVisible(true);
            frame.setVisible(true);
            
            while(true){
                String line = fromSockReader.readLine();
                if (line == null) {
                    System.out.println("The game is over.");
                    break;
                }
                try {
                    score += Integer.parseInt(line);
                    scoreLabel.setText("Score: " + score);
                } catch (NumberFormatException e) {
                    label1.setText(line);
                }
                frame.setVisible(true);
            }
        }
        catch(Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        input.close();
        System.exit(0);
    }
}
