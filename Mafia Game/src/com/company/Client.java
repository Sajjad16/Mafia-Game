package com.company;

import java.net.*;
import java.io.*;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This is the client program.
 */
public class Client {

    private String userName;
    private Player clientPlayer;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private ObjectInputStream objectInputStream;
    private Thread thread;
    //    private ShareData shareData;
    private ArrayList<String> aliveUsers;

    public Client() {
        aliveUsers = new ArrayList<>();
    }

    public void execute() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter the port and server:");
//            while (true) {
//                try {
//                    System.out.println("Server:");
//                    String serverHost = scanner.next();
//                    System.out.println("Port:");
//                    int port = scanner.nextInt();
//                    socket = new Socket(serverHost, port);
//                    break;
//                } catch (IOException | InputMismatchException e) {
//                    e.printStackTrace();
//                    System.out.println("Try again:");
//                }
//            }
            socket = new Socket("localhost", 6969);
            System.out.println("Connected to the  server");
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            thread = Thread.currentThread();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Enter your username:");
            userName = scanner.next();
            writer.println(userName);
            while (true) {
                clientPlayer = (Player) objectInputStream.readObject();
                if (clientPlayer != null)
                    break;
                System.out.println("Username is duplicate Enter your username:");
                userName = scanner.next();
                writer.println(userName);
            }
            System.out.println("Are you ready?\n1.yes\n2.No");
            while (true) {
                try {
                    int readyDecision = scanner.nextInt();
                    if (readyDecision == 1) {
                        writer.println("ready");
                        break;
                    } else {
                        System.out.println("The game will not start until you enter the 1");
                    }
                } catch (InputMismatchException e) {
                    System.err.println("Invalid input");
                    System.out.println("Please try a gain");
                    scanner.nextLine();
                }
            }

            Thread.sleep(500);
            System.out.println("Waiting to anther join...");
            Thread.sleep(500);
            String msg = reader.readLine();
            if (msg.equals("Start game")) {
                System.out.println(msg);
                startGame();
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.execute();
    }


    public void introductionNight() {
        try {
            System.out.println(readMessage());
            Thread.sleep(500);
            getTheLatestAliveUserList();
            System.out.println("ALive users:");
            printUserNames();
            Thread.sleep(500);
            System.out.println(readMessage());
            Thread.sleep(500);
            if (clientPlayer instanceof MafiaPlayer) {
                for (int i = 0; i < (aliveUsers.size() / 3) - 1; i++) {
                    System.out.println(reader.readLine());
                }
            }
            if (clientPlayer instanceof Mayor) {
                System.out.println(reader.readLine());
            }
            Thread.sleep(500);
            System.out.println(reader.readLine());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void printUserNames() {
        int counter = 1;
        for (String userName : aliveUsers) {
            System.out.println(counter + ")" + userName);
            counter++;
        }
    }

    public void printOthersUserNames() {
        int counter = 1;
        for (String userName : aliveUsers) {
            if (!userName.equals(this.userName)) {
                System.out.println(counter + ")" + userName);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
        }
    }

    public ArrayList<String> getOthersUserNames() {
        ArrayList<String> userNames = new ArrayList<>();
        for (String userName : aliveUsers) {
            if (!userName.equals(this.userName)) {
                userNames.add(userName);
            }
        }
        return userNames;
    }

    public void startGame() {
        introductionNight();
        try {
            while (true) {
                System.out.println(readMessage());
                if (!clientPlayer.isAlive()) {
                    System.out.println("You die");
                    exitGame();
                }
                if (socket.isClosed()){
                    break;
                }
                getTheLatestAliveUserList();
                System.out.println("Alive Users:");
                printUserNames();
                String msg = readMessage();
                while (!msg.equals("Start chat:")) {
//                    if (msg.equals("Want to see the old message?\n1.Yes\n2.No")) {
//                        if (clientPlayer.isAlive()) {
//                            System.out.println(msg);
//                            writer.println(yesOrNoQuestion());
//                        }
//                    } else {
                        System.out.println(msg);
//                    }
                    msg = readMessage();
                }
                System.out.println(msg);
                startChat();
                try {
                    synchronized (thread) {
                        thread.wait();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (socket.isClosed()) {
                    closeALl();
                    break;
                }
                System.out.println(reader.readLine());
                voting();
                if (socket.isClosed()) {
                    closeALl();
                    break;
                }
                nightGame();


            }


        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    public void nightGame() {
        System.out.println(readMessage());
        if (clientPlayer.isAlive()) {
            if (clientPlayer instanceof CitizenPlayer && !(clientPlayer instanceof Mayor)) {
                String message = readMessage();
                if (message.equals("Act")) {
                    clientPlayer.act(this);
                }
            }
            if (clientPlayer instanceof MafiaPlayer) {
                String message = readMessage();
                if (message.equals("Act")) {
                    clientPlayer.act(this);
                } else if (message.equals("Get vote")) {
                    ((MafiaPlayer) clientPlayer).getVote(this);
                } else if (message.equals("Kill")) {
                    ((MafiaPlayer) clientPlayer).killCitizen(this);
                }
                if (clientPlayer instanceof DoctorLecter) {
                    message = readMessage();
                    if (message.equals("Act")) {
                        clientPlayer.act(this);
                    }
                }
            }

        }
        System.out.println(readMessage());
        try {
            clientPlayer = (Player) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void voting() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println(readMessage());
            getTheLatestAliveUserList();
            System.out.println("Get vote:");
            printOthersUserNames();
            int vote = -1;
            int time1 = (int) System.currentTimeMillis();
            while (true) {
                if (System.in.available() > 0 && clientPlayer.isAlive()) {
                    try {
                        vote = scanner.nextInt();
                        if (vote < 1 || vote >= aliveUsers.size()) {
                            System.out.println("Enter the correct number");
                            vote = -1;
                        } else {
                            System.out.println("You can change your vote");
                        }
                    } catch (InputMismatchException e) {
                        System.err.println("Invalid input");
                        scanner.nextLine();
                    }
                }
                int time2 = (int) System.currentTimeMillis();
                int time = ((time2 - time1) / 1000);
                if (time > 20) {
                    break;
                }
            }
            System.out.println("End voting");
            if (vote != -1) {
                writer.println(getOthersUserNames().get(vote - 1));
                System.out.println("You vote " + getOthersUserNames().get(vote - 1));
            } else {
                if (clientPlayer.isAlive()) {
                    writer.println("null");
                    System.out.println("You did not vote for anyone");
                }
            }
            if (clientPlayer.isAlive()) {
                for (int i = 0; i < aliveUsers.size() - 1; i++) {
                    System.out.println(readMessage());
                }
            } else
                for (int i = 0; i < aliveUsers.size(); i++) {
                    System.out.println(readMessage());
                }

            if (clientPlayer instanceof Mayor && clientPlayer.isAlive() && ((Mayor) clientPlayer).getStateAbility()) {
                clientPlayer.act(this);
            }
            clientPlayer = (Player) objectInputStream.readObject();
            getTheLatestAliveUserList();
            System.out.println(readMessage());
            if (!clientPlayer.isAlive()) {
                exitGame();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void startChat() {
        new ClientWrite(this).start();
        new ClientRead(this).start();

    }

    public Thread getThread() {
        return thread;
    }

    public void exitGame() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to see the rest of the game?\n1.Yes\n2.No");
        int decision = scanner.nextInt();
        if (decision == 1) {
            writer.println("Show game");
            clientPlayer.kill();
        } else if (decision == 2) {
            writer.println("Dont show");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Good bye");
        }

    }

    public Player getClientPlayer() {
        return clientPlayer;
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String readMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object readObject() {
        try {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    String getUserName() {
        return this.userName;
    }

    public void closeALl() {
        try {
            writer.close();
            reader.close();
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void getTheLatestAliveUserList() {
        try {
            aliveUsers = new ArrayList<>();
            int numberOfPlayers = 0;
            numberOfPlayers = Integer.parseInt(reader.readLine());
            for (int i = 0; i < numberOfPlayers; i++) {
                String userName = readMessage();
                aliveUsers.add(userName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String selectUser(ArrayList<String> users) {
        Scanner scanner = new Scanner(System.in);
        int counter = 1;
        for (String user : users) {
            System.out.println(counter + ")" + user);
            counter++;
        }
        int decision;
        while (true) {
            try {
                decision = scanner.nextInt();
                if (decision < 1 || decision > users.size()) {
                    System.out.println("Enter a number between 1 and " + users.size());
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input");
                scanner.nextLine();
            }
        }
        return users.get(decision - 1);
    }

    public int yesOrNoQuestion() {
        Scanner scanner = new Scanner(System.in);
        int decision;
        while (true) {
            try {
                decision = scanner.nextInt();
                if (decision != 1 && decision != 2) {
                    System.out.println("Enter 1 or 2");
                    continue;
                }
                break;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input");
                scanner.nextLine();
            }
        }
        return decision;
    }
}