import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

    public static void main(String[] args) throws UnknownHostException, IOException {
        String msg, temp;

        // ask for client triad
        int CIA = getCIA();
        //System.out.println(CIA);

        Scanner scan = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 7802);
        Scanner scan1 = new Scanner(socket.getInputStream());

        // send server message containing triad number to check security protocols
        PrintStream cia_check = new PrintStream(socket.getOutputStream());
        cia_check.println(CIA);

        //there is a change to be made here

        // instant message
        System.out.println("Enter a message:\n");
        msg = scan.nextLine();
        PrintStream p = new PrintStream(socket.getOutputStream());
        p.println(msg);

        try {
            temp = scan1.nextLine();
            System.out.println(temp);
        }
        catch(NoSuchElementException e) {
            System.out.println("--- No message found ---");
        }
    }

    // prompts user for with security options
    private static int getCIA() {
        char selection; // user input
        int triad = 0;  // security triad selection
        int val = 0;
        boolean check = true;
        Scanner scan = new Scanner(System.in);

        for(int i = 0; i < 3; i++) {
            check = true;
            // Confidentiality
            if(i == 0) {
                System.out.println("Would you like Confidentiality? (y/n)");
                val = 4;
            // Integrity
            } else if (i == 1) {
                System.out.println("Would you like Integrity? (y/n)");
                val = 2;
            // Authentication
            } else if (i == 2) {
                System.out.println("Would you like Authentication? (y/n)");
                val = 1;
            } else {
                //we should never get here
            }

            // loop to prompt user
            while(check) {
                selection = scan.next().charAt(0);
                if(selection == 'y') {
                    triad += val;
                    check = false;
                } else if (selection == 'n') {
                    //do nothing
                    check = false;
                } else {
                    System.out.println("Please enter a valid option");
                }
            }
        }
        return triad;
    }
}
