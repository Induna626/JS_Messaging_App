package com.theclient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

// **NEW CLASS**
// Client Class
public class Client extends Thread{

    final JTextPane FillChatbox = new JTextPane();
    final JTextPane ListClients = new JTextPane();
    final JTextField ChatInput = new JTextField();
    private Thread read;
    private String serverName;
    private int PORT;
    private String name;
    BufferedReader input;
    PrintWriter output;
    Socket server;

    public Client() {
        this.serverName = "localhost";
        this.PORT = 1235;
        this.name = "Username";

        // Java form builder / form pane
        final JFrame jfr = new JFrame("CMPG315 Chat App");
        jfr.getContentPane().setLayout(null);
        jfr.setSize(700, 500);
        jfr.setResizable(false);


// Load the background image
// Load the background image from a relative path
// Define a variable to represent the relative path to the resources directory
        String resourcesPath = "resources/";

// Load the background image from a relative path in the "resources" folder
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(resourcesPath + "image.jpg"));
        Image image = imageIcon.getImage();

// Resize the image to fit the JFrame
        Image resizedImage = image.getScaledInstance(jfr.getWidth(), jfr.getHeight(), Image.SCALE_SMOOTH);

// Create a new JLabel with the resized image as the icon
        JLabel backgroundLabel = new JLabel(new ImageIcon(resizedImage));

// Set the layout of the JFrame to BorderLayout
        jfr.setLayout(new BorderLayout());

// Add the JLabel to the background of the JFrame
        jfr.setContentPane(backgroundLabel);

// Repaint the JFrame to update the changes
        jfr.repaint();




        // jfr.getContentPane().setBackground( Color.BLACK );
        jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Chat window
        FillChatbox.setBounds(200, 25, 450, 350);
        FillChatbox.setMargin(new Insets(6, 6, 6, 6));
        FillChatbox.setEditable(false);
        JScrollPane ChatScrollPlane = new JScrollPane(FillChatbox);
        ChatScrollPlane.setBounds(200, 25, 450, 350);
        FillChatbox.setContentType("text/html");
        FillChatbox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        // Client list window
        //ListClients.setBounds(25, 100, 135, 300);
        //ListClients.setEditable(true);
        //ListClients.setMargin(new Insets(6, 6, 6, 6));
        //ListClients.setEditable(false);
        //JScrollPane jsplistuser = new JScrollPane(ListClients);
        //jsplistuser.setBounds(25, 100, 135, 300);

        ListClients.setContentType("text/html");
        ListClients.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);


        // Field message user input
        ChatInput.setBounds(25, 410, 490, 35);
        ChatInput.setMargin(new Insets(6, 6, 6, 6));
        final JScrollPane jtextInputChatSP = new JScrollPane(ChatInput);
        jtextInputChatSP.setBounds(25, 410, 490, 35);

        // button Send
        final JButton SendButton = new JButton("Send");
        SendButton.setBounds(575, 410, 100, 35);

        // button Disconnect
        final JButton DisconnectButton = new JButton("Disconnect");
        DisconnectButton.setBounds(25, 30, 100, 40);

        ChatInput.addKeyListener(new KeyAdapter() {
            // send message on Enter keypress
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        // Send message when 'Send' is clicked
        SendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                sendMessage();
            }
        });

        // Connection view
        final JTextField jtfName = new JTextField(this.name);
        final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
        final JTextField jtfAddr = new JTextField(this.serverName);
        final JButton ConnectButton = new JButton("Connect");

        // Empty field handeling
        jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, ConnectButton));
        jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, ConnectButton));
        jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, ConnectButton));

        jtfAddr.setBounds(25, 30, 135, 40);
        jtfName.setBounds(25, 200, 135, 40);
        jtfport.setBounds(25, 115, 135, 40);
        ConnectButton.setBounds(300, 400, 200, 40);

        FillChatbox.setBackground(Color.WHITE);
        ListClients.setBackground(Color.WHITE);

        jfr.add(ConnectButton);
        jfr.add(ChatScrollPlane);
        //jfr.add(jsplistuser);
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.setVisible(true);


        // Instructions
        appendToPane(FillChatbox, "<h2>Chats will display here:</h2>"
                +"<ul>"
                +"<li><b>Run a server, then enter it's IP adress </li>"
                +"<li><b>Enter Port number (1235 default)</li>"
                +"<li><b>Enter your Username</li>"
                +"</ul><br/>");

        // On connect
        ConnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    name = jtfName.getText();
                    String port = jtfport.getText();
                    serverName = jtfAddr.getText();
                    PORT = Integer.parseInt(port);

                    appendToPane(FillChatbox, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
                    server = new Socket(serverName, PORT);

                    appendToPane(FillChatbox, "<span>Connected to " +
                            server.getRemoteSocketAddress()+"</span>");

                    input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                    output = new PrintWriter(server.getOutputStream(), true);

                    // send username to server
                    output.println(name + " joined the chatroom");

                    // create new Read Thread
                    read = new Read();
                    read.start();
                    jfr.remove(jtfName);
                    jfr.remove(jtfport);
                    jfr.remove(jtfAddr);
                    jfr.remove(ConnectButton);
                    jfr.add(SendButton);
                    jfr.add(jtextInputChatSP);
                    jfr.add(DisconnectButton);
                    jfr.revalidate();
                    jfr.repaint();
                    FillChatbox.setBackground(Color.WHITE);
                    ListClients.setBackground(Color.WHITE);
                } catch (Exception ex) {
                    appendToPane(FillChatbox, "<span>Could not connect to Server</span>");
                    JOptionPane.showMessageDialog(jfr, ex.getMessage());
                }
            }

        });

        // When a user disconnects
        DisconnectButton.addActionListener(new ActionListener()  {
            public void actionPerformed(ActionEvent ae) {
                jfr.add(jtfName);
                jfr.add(jtfport);
                jfr.add(jtfAddr);
                jfr.add(ConnectButton);
                jfr.remove(SendButton);
                jfr.remove(jtextInputChatSP);
                jfr.remove(DisconnectButton);
                jfr.revalidate();
                jfr.repaint();
                read.interrupt();
                ListClients.setText(null);
                FillChatbox.setBackground(Color.WHITE);
                ListClients.setBackground(Color.WHITE);
                appendToPane(FillChatbox, "<span>Connection closed.</span>");
                output.close();
            }
        });

    }

    // check if all field are not empty
    public class TextListener implements DocumentListener {
        JTextField jtf1;
        JTextField jtf2;
        JTextField jtf3;
        JButton ConnectButton;

        public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton ConnectButton){
            this.jtf1 = jtf1;
            this.jtf2 = jtf2;
            this.jtf3 = jtf3;
            this.ConnectButton = ConnectButton;
        }

        public void changedUpdate(DocumentEvent e) {}

        public void removeUpdate(DocumentEvent e) {
            if(jtf1.getText().trim().equals("") ||
                    jtf2.getText().trim().equals("") ||
                    jtf3.getText().trim().equals("")
            ){
                ConnectButton.setEnabled(false);
            }else{
                ConnectButton.setEnabled(true);
            }
        }
        public void insertUpdate(DocumentEvent e) {
            if(jtf1.getText().trim().equals("") ||
                    jtf2.getText().trim().equals("") ||
                    jtf3.getText().trim().equals("")
            ){
                ConnectButton.setEnabled(false);
            }else{
                ConnectButton.setEnabled(true);
            }
        }

    }

    // Sends message whe textbox is not empty
    public void sendMessage() {
        try {
            String message = ChatInput.getText().trim();
            if (message.equals("")) {
                return;
            }

            output.println(name + " said" + message);
            appendToPane(FillChatbox, "\n");
            appendToPane(FillChatbox, name + ": " + message);
            ChatInput.requestFocus();
            ChatInput.setText(null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }

    // read new incoming messages
    class Read extends Thread {
        public void run() {
            String message;
            while(!Thread.currentThread().isInterrupted()){
                try {
                    message = input.readLine();
                    if(message != null){
                        if (message.charAt(0) == '[') {
                            message = message.substring(1, message.length()-1);
                            ArrayList<String> ListUser = new ArrayList<String>(
                                    Arrays.asList(message.split(", "))
                            );
                            ListClients.setText(null);
                            for (String user : ListUser) {
                                appendToPane(ListClients, "@" + user);
                            }
                        }else{
                            appendToPane(FillChatbox, message);
                        }
                    }
                }
                catch (IOException ex) {
                    System.err.println("Failed to parse incoming message");
                }
            }
        }
    }

    // send html to pane
    private void appendToPane(JTextPane tp, String msg){
        HTMLDocument doc = (HTMLDocument)tp.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
        try {
            editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(doc.getLength());
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}

