
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MyClient {
    public static void main(String[] args) {
        try{
            Socket s=new Socket("localhost",6666);

            Scanner scan = new Scanner(System.in);

            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout=new DataOutputStream(s.getOutputStream());

            System.out.println("Connection Complete");

            Thread sendMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        String message = scan.nextLine();
                        try{
                            dout.writeUTF(message);
                        }
                        catch(IOException e){
                            e.getMessage();
                        }
                    }
                }
            });

            Thread readMessage = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try{
                            String message = din.readUTF();
                            System.out.println(message);
                        }
                        catch(IOException e){
                            e.getMessage();
                        }
                    }
                }
            });

            sendMessage.start();
            readMessage.start();

            try{
                sendMessage.join();
                readMessage.join();
            }
            catch(InterruptedException e){
                System.out.println("Thread Interrupted.");
            }

            dout.flush();
            dout.close();
            s.close();

            sendMessage.interrupt();
            readMessage.interrupt();

            System.out.println(sendMessage.isAlive());
            System.out.println(readMessage.isAlive());
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
}
