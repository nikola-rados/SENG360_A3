import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import javax.crypto.*;


public class Client {


    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'Z', '4', 'e', 't', 'e', '_', 't', 'S', '-', '!', '2', '%', 't', 'K', 'e', '9'};


    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }


    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        String msg, server_str;
        Scanner scan_client = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 7802);
        Scanner scan_server = new Scanner(socket.getInputStream());

        int cia = getCIA();
        System.out.println(cia);

        // send server message containing triad number to check security protocols
        PrintStream cia_check = new PrintStream(socket.getOutputStream());
        cia_check.println(cia);
        System.out.println("--------------------------------------------------------");

        // listen for a response from the server
        while(true) {
            try {
                server_str = scan_server.nextLine();
                // Confidentiality check
                if(cia >= 4) {
                    try {
                        server_str = decrypt(server_str);
                    } catch (Exception e) {
                        System.out.println("Error: Unable to decrypt message");
                    }
                }
                System.out.println("Server: " + server_str);
            }
            catch(NoSuchElementException e) {
                System.out.println("--- No message sent ---");
                System.out.println("Exiting Program...");
                break;
            }

            // first iteration needs to check for the confirmation message
            if(server_str.contains("Selected security properties were accepted")) {
                continue;
            } else if(server_str.contains("Selected security properties were denied")){
                System.out.println("Exiting Program...");
                break;
            // this checks for the password retry and goes to next loop
            } else if(server_str.contains("Username/Password is not vaild please try again")) {
                continue;
            }

            // After Server responds, user can respond
            System.out.print("Client: ");
            msg = scan_client.nextLine();

            // check if client wishes to break from the conversation
            if (msg.contains("!quit")) {
                break;
            }

            // Confidentiality check
            if(cia >= 4) {
                try {
                    msg = encrypt(msg);
                } catch (Exception e) {
                    System.out.println("Error: Unable to encrypt message");
                }
            }

            // send messag to Server
            PrintStream client_out = new PrintStream(socket.getOutputStream());
            client_out.println(msg);
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
            } else if(i == 1) {
                System.out.println("Would you like Integrity? (y/n)");
                val = 2;
            // Authentication
            } else if(i == 2) {
                System.out.println("Would you like Authentication? (y/n)");
                val = 1;
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
