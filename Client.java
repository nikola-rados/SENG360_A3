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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.lang.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;



public class Client {
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'N', '7', 'r', 's', 'v', '$', 'y', 'Q', '+', '%', '9', '@', 'g', 'F', 'p', '0'};
	private static String check_file = "check_file.txt";


	public static boolean compare_text(String to_check){

		try{
		FileReader fr = new FileReader(check_file); 
		BufferedReader br = new BufferedReader(fr); 
		String s;
		s = br.readLine();
		fr.close();  
		if (bit_shift(to_check).equals(s)){
			return true;
		}
        } catch (IOException e) {
           System.out.println("Error in comparing text");

		}

		return false;
		// check_file
	 }
	 
	public static void write_text(String write_string){
		File f = new File(check_file);
        try {
            boolean f_check = f.createNewFile();
            if(f_check) {
            } else {
            }
        } catch (IOException e) {
           System.out.println("Error creating file");
           e.printStackTrace();
		}
		
		
		try{		
			FileWriter fileWriter = new FileWriter(check_file);
			fileWriter.write(write_string);
			fileWriter.flush();
			fileWriter.close();

		}catch(IOException e){
             System.out.println("Error: write_text error...");
		}
	 }

	public static String bit_shift(String change){
		 StringBuilder msg = new StringBuilder(change);
		 for (int i = 0; i < msg.length(); i ++) {
			msg.setCharAt(i, (char) (msg.charAt(i) + 2));
		}
		return msg.toString();
	}

	
    /* MAIN */
    public static void main(String[] args) throws UnknownHostException, IOException {

        String msg, server_str, sig_str;
        String verified = "";
        Scanner scan_client = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 7802);
        Scanner scan_server = new Scanner(socket.getInputStream());
		boolean Integrity = false;
        int cia = getCIA();
		if (cia==2||cia==3||cia==6||cia==7){
				Integrity = true;	
		}
        // send server message containing triad number to check security protocols
        PrintStream cia_check = new PrintStream(socket.getOutputStream());
        cia_check.println(cia);
        System.out.println("--------------------------------------------------------");

        /*
         *  Integrity
         *  If we have selected the security option it is at this point we need
         *  to generate a key pair and exchange public keys with the Server.
         */
 
        

        // listen for a response from the server
        while(true) {
            try {
                server_str = scan_server.nextLine();
               // System.out.println(server_str);
                // Confidentiality check
                if(cia >= 4) {
                    try {
                        server_str = decrypt(server_str);
                    } catch (Exception e) {
                        System.out.println("Error: Unable to decrypt message");
                    }
                }else{
					
				}
				if (Integrity){
					if (compare_text(server_str)){
						System.out.println("Server: "+server_str+ " (Verified)");
					}else{
						System.out.println("Server: "+server_str+ " (not Verified)");
					}					
				}else{
					System.out.println("Server: "+server_str);

				}
				
            }
            catch(NoSuchElementException e) {
                System.out.println("--- No message sent ---");
                System.out.println("Exiting Program...");
                break;
            }
//            System.out.println("line 210");

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

			
			
			
			
            if(cia >= 4) {
				if(Integrity){
					String into_file;
					into_file = bit_shift(msg);
					write_text (into_file);
				}
                try {
                    msg = encrypt(msg);
                } catch (Exception e) {
                    System.out.println("Error: Unable to encrypt message");
                }
            }else{
				if(Integrity){
					String into_file;
					into_file = bit_shift(msg);
					write_text (into_file);
				}else{
					
				}
			}

            // send messag to Server
            PrintStream client_out = new PrintStream(socket.getOutputStream());
            client_out.println(msg);
            // Integrity check
      
        }
    } // end main


 



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

    /*
     *  Encrypting method for messages
     */
    public static String encrypt(String data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(data.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encVal);
        return encryptedValue;
    }


    /*
     *  Decrypting method for messages.
     */
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
}
