import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class MyServer {
    static int i = 0;
    static volatile boolean isWinner = false;
    public static HashMap<ClientHandler, Database> ar = new HashMap<ClientHandler, Database>();

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(6666);
            String secretCode = SecretCodeGenerator.getInstance().getNewSecretCode();
            System.out.println("Secret Code: " + secretCode);

            while (!isWinner) {
                Socket s = ss.accept();//establishes connection
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());

                //dout.writeUTF("Player " + i + " has connected!");
                System.out.println("Player " + i + " has connected!");

                ClientHandler match = new ClientHandler(s, "client " + i, dis, dout);
                Thread t = new Thread(match);
                ar.put(match, new Database(secretCode));
                t.start();
                i++;
            }

            isWinner = true;

            for (ClientHandler curr : ar.keySet()) {
                curr.dout.writeUTF("You lost! Disconnecting from game...");
                curr.stopThread();
            }
            ss.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

class ClientHandler implements Runnable{
    public String name;
    public DataInputStream dis;
    public DataOutputStream dout;
    public Socket s;
    public boolean won;
    public boolean otherWon;
    public volatile boolean stopThread = false;

    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dout) {
        this.dis = dis;
        this.dout = dout;
        this.name = name;
        this.s = s;
        this.won = false;
        this.otherWon = false;
    }
    @Override
    public void run() {
        String received;
        try {
            dout.writeUTF("\nWelcome to Mastermind. Here are the rules.\n"+
                "\nThis is a text version of the classic board game Mastermind.\n"+
                "\nThe computer will think of a secret code. The code consists of 4\n"+
                "colored pegs. The pegs MUST be one of six colors: blue, green,\n"+
                "orange, purple, red, or yellow. A color may appear more than once in\n"+
                "the code. You try to guess what colored pegs are in the code and\n"+
                "what order they are in. After you make a valid guess the result\n"+
                "(feedback) will be displayed.\n"+
                "\nThe result consists of a black peg for each peg you have guessed\n"+
                "exactly correct (color and position) in your guess. For each peg in\n"+
                "the guess that is the correct color, but is out of position, you get\n"+
                "a white peg. For each peg, which is fully incorrect, you get no\n"+
                "feedback.\n"+
                "\nOnly the first letter of the color is displayed. B for Blue, R for\n"+
                "Red, and so forth. When entering guesses you only need to enter the\n"+
                "first character of each color as a capital letter.\n"+
                "You have 12 guesses to figure out the secret code or you lose the\n"+
                "game. Are you ready to play? (Y/N): ");
            received = dis.readUTF();
            //check/////////////////////////////////////////////////////////////////////////////////////////
            while(true) {
                if (received.equals("N") || received.equals("n")) {
                    try {
                        System.out.println(this.name + " has been disconnected.");
                        this.dis.close();
                        this.dout.close();
                        this.s.close();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                } else if ((received.equals("Y") || received.equals("y"))){
                    break;
                }
            }

            dout.writeUTF("\nGenerating secret code ...");
            while(!stopThread && !MyServer.isWinner && MyServer.ar.get(this).guessNumber > 0 ){
                dout.writeUTF("\nYou have " + MyServer.ar.get(this).guessNumber + " guesses left." +
                        "\nWhat is your next guess?" +
                        "\nType in the characters for your guess and press enter." +
                        "\nEnter guess: ");
                received = dis.readUTF();
                while(!stopThread && !MyServer.isWinner && !this.Check(received)) {
                    dout.writeUTF("\n" + received + " -> INVALID GUESS");
                    dout.writeUTF(
                            "\nWhat is your next guess?" +
                                    "\nType in the characters for your guess and press enter." +
                                    "\nEnter guess: "
                    );
                    received = dis.readUTF();
                }
                if(!stopThread && !MyServer.isWinner && received.equals("HISTORY")){
                    dout.writeUTF("\n");
                    for(int i = 0; i < MyServer.ar.get(this).colorSet.size(); i++){
                        dout.writeUTF(MyServer.ar.get(this).colorSet.get(i) + "\t\t" + MyServer.ar.get(this).resultSet.get(i));
                    }
                    continue;
                }
                // Valid input => check if match with guess
                String black_white = this.GetResult(received);
                if (!stopThread && !MyServer.isWinner && MyServer.ar.get(this).ans.equals(received)) {
                    //matches with random answer
                    this.won = true;
                    MyServer.isWinner = true;
                    dout.writeUTF("\n" + MyServer.ar.get(this).ans + " -> Result: " + black_white + " - You win !!");

                    this.stopThread();//stops further messages to winner thread

                    break;
                }
                else{
                    // Does not match with random generated answer
                    if (MyServer.ar.get(this).guessNumber == 1)
                        break;
                    dout.writeUTF("\n" + received + " -> Result: " + black_white);
                    MyServer.ar.get(this).guessNumber--;
                    MyServer.ar.get(this).colorSet.add(received);
                    MyServer.ar.get(this).resultSet.add(black_white);
                }
            }
        } catch (IOException e) {
            e.getMessage();
        }

        if (MyServer.isWinner && !this.won) { this.otherWon = true;}
        //check/////////////////////////////////////////////////////////////////////////////////////////
        if (MyServer.isWinner && this.otherWon || !this.won) {
            try {
                dout.writeUTF("You lost! Disconnecting from game...");
            } catch (IOException e) {
            }
        }else {
            try {
                dout.writeUTF("Congrats you won the game!");
            } catch (IOException e) {
            }
        }

        try
        {
            //closing resources
            this.dis.close();
            this.dout.close();
            this.s.close();

        }catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println(this.name + " has been disconnected.");
    }
    public String GetResult(String in) {
        int black = 0, white = 0;
        ArrayList<Character> tmp = new ArrayList<Character>();
        ArrayList<Character> ans = new ArrayList<Character>();

        // Copy characters to temporary strings
        for (int i = 0; i < in.length(); i++) {
            tmp.add(in.charAt(i));
            ans.add(MyServer.ar.get(this).ans.charAt(i));
        }

        // Check for black pegs
        for (int i = 0; i < in.length(); i++) {
            if (in.charAt(i) == MyServer.ar.get(this).ans.charAt(i)) {
                black++;
                tmp.set(i, '-');
                ans.set(i, '-');
            }
        }

        // Check for white pegs
        for (int i = 0; i < tmp.size(); i++) {
            for (int j = 0; j < ans.size(); j++) {
                if (tmp.get(i) == '-' && ans.get(j) == '-') {
                    break;
                } else if (tmp.get(i) == ans.get(j)) {
                    white++;
                    tmp.set(i, '-');
                    ans.set(j, '-');
                    //Once you remove you have to reset
                    break;
                }
            }
        }
        return black + "B_" + white + "W";
    }
    public boolean Check(String input) {
        if (input.equals("HISTORY")) {
            return true;
        }

        //Check if length of input is the same
        if (input.length() != MyServer.ar.get(this).pegNumber)
            return false;

        String tmp = input.toUpperCase();

        //Check if string is all capital
        for (int i = 0; i < input.length(); i++)
            if (Character.isDigit(input.charAt(i)) || tmp.charAt(i) != input.charAt(i))
                return false;

        //Check if each character is in colors string
        int flag;

        for (int i = 0; i < input.length(); i++) {
            flag = 0;
            for (String color : MyServer.ar.get(this).colors) {
                if (color.equals(Character.toString(input.charAt(i)))) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                return false;
            }
        }
        return true;
    }

    public void stopThread() {
        stopThread = true;
    }
}
            //System.out.println("client connected");

