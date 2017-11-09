/*
 *  SENG 360
 *  Fall 2017
 *  Assignment 3
 *  Russell Snelgrove (V00)
 *  Nikola Rados (V00801209)
 *  Things to keep in mind:
 *
 *
 *  You should compare the (hashed) password to a password hash in a protected
 *  (access controlled) directory. When I talk about "protected directory", I am
 *  referring to a directory that is access controlled, so that it is not
 *  accessible to the world. Integrity just ensures that the message has not been
 *  changed while in transit between client and server. You can ensure this by
 *  using message authentication codes (with AES) (see your question 5).
 * 	Authentication also requires that users authenticate. This can be done using a
 *  password authentication on client and server, respectively.
 *
 * 	Possible CIA combinations:
 * 	0 -> 000 -> ---
 * 	1 -> 001 -> --A
 * 	2 -> 010 -> -I-
 * 	3 -> 011 -> -IA
 * 	4 -> 100 -> C--
 * 	5 -> 101 -> C-A
 * 	6 -> 110 -> CI-
 * 	7 -> 111 -> CIA
 */


/* IMPORTS */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.*;
import java.security.Provider;
import javax.crypto.Cipher;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.NoSuchElementException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.io.*;


/* MAIN SERVER CLASS */
public class Server{
    /* CLASS VARS */
    private static Socket socket;
	public static int Authentication;
	public static boolean Integrity;
	public static boolean Confidentiality;
	public static int Command_total;
	public static boolean Running = true;
	private static String filename = "secure.txt";
    private static final String DEFAULT_USER = "seng360";
    private static final String DEFAULT_PASS = "assignment3";
    private static PrivateKey privateKey;
    private static PublicKey publicKey_Server;
    private static PublicKey publicKey_Client;
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'Z', '4', 'e', 't', 'e', '_', 't', 'S', '-', '!', '2', '%', 't', 'K', 'e', '9'};
	private static Signature signature;
	private static byte[] dsig;
	private static byte[] client_sig;


    /**
     *  This is the method that confirms what was requested
     */
    public static void selected() {
    	System.out.println("\nYou have selected the following:");
    	if(Command_total > 3) {
    		System.out.println("\tConfidentiality");
			Confidentiality=true;
    	}else{
			Confidentiality=false;
		}
    	if(Command_total == 7 || Command_total == 6 || Command_total == 3 || Command_total == 2) {
    		System.out.println("\tIntegrity");
			Integrity = true;
    	}else{
			Integrity = false;
		}
        if(Command_total%2 == 1) {
    	//if (Command_total==7||Command_total==5||Command_total==3||Command_total==1){
    		System.out.println("\tAuthentication");
    	}
    }


    /**
     * This method works on finding if the user of the server wants what settings of security
     */
    private static int getCIA() {
        char selection; // user input
        int triad = 0;  // security triad selection
        int val = 0;
        boolean check = true;
        Scanner scan = new Scanner(System.in);

        for(int i = 0; i < 3; i++) {
            check = true;
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
            } else {
                //we should never get here
            }

            // loop to prompt user
            while(check) {
                selection = scan.next().charAt(0);
                if(selection == 'y') {
                    triad += val;
                    check = false;
                } else if(selection == 'n') {
                    //do nothing
                    check = false;
                } else {
                    System.out.println("Please enter a valid option");
                }
            }
        }
        return triad;
    }


    /**
    * This function searchs the txt file for the user name and password
    *
    * Reference: https://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-using-java
    */
    public static boolean check_user(String name, String pword) {
        // first lets hash the strings
        String name_hashed = hashText(name);
        String pword_hashed = hashText(pword);

        try {
            FileInputStream fs = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
    		String str_check;
    		while ((str_check = br.readLine()) != null) {
                if(str_check.contains(name_hashed) && str_check.contains(pword_hashed)) {
                    br.close();
                    return true;
                }
    		}
            br.close();
    	} catch (IOException ex) {
            System.out.println("Error: Unable to read file");
    	}
        return false;
    }


    /**
     *  Generate the default setting for password which will be:
     *  Username: seng360
     *  Password: assignment3
     *  These will be hashed using SHA-256, which is a secure hashing algorithm.
     */
    private static void generateUserPass(String username, String password) {
        MessageDigest mdUser, mdPass;
        StringBuffer sbUser = new StringBuffer();
        StringBuffer sbPass = new StringBuffer();

        // Create instances
        try {
            mdUser = MessageDigest.getInstance("SHA-256");
            mdPass = MessageDigest.getInstance("SHA-256");
            mdUser.update(username.getBytes());
            mdPass.update(password.getBytes());
            byte byteDataUser[] = mdUser.digest();
            byte byteDataPass[] = mdPass.digest();

            // convert username to hex
            for(int i = 0; i < byteDataUser.length; i++) {
                sbUser.append(Integer.toString((byteDataUser[i] & 0xff) + 0x100, 16).substring(1));
            }
            // convert password to hex
            for(int i = 0; i < byteDataPass.length; i++) {
                sbPass.append(Integer.toString((byteDataPass[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Fatal Error: Algorithm not available");
            System.exit(0);
        }
        writeToFile(sbUser.toString(), sbPass.toString());
    }


    /*
     *  Similar to the generateUserPass function, this will just hash a String
     *  and return it.
     */
     private static String hashText(String unhashed) {
         MessageDigest md;
         StringBuffer sb = new StringBuffer();
         String hashed;

         // Create instance
         try {
             md = MessageDigest.getInstance("SHA-256");
             md.update(unhashed.getBytes());
             byte byteData[] = md.digest();

             // convert to hex
             for(int i = 0; i < byteData.length; i++) {
                 sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
             }
         } catch (NoSuchAlgorithmException e) {
             System.out.println("Fatal Error: Algorithm not available");
             System.exit(0);
         }
         return hashed = sb.toString();
     }

    //private helper method
    private static void writeToFile(String username, String password) {
        // first we need to create the file
        File f = new File(filename);
        try {
            boolean f_check = f.createNewFile();
            if(f_check) {
                System.out.println("File has been created successfully");
            } else {
                System.out.println("File already present at the specified location");
            }
        } catch (IOException e) {
           System.out.println("Error creating file");
           e.printStackTrace();
        }

        // then we write the default values to it
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(username);
            writer.write(password);
            writer.close();
            System.out.println("Successfully wrote to file");
        } catch(IOException e) {
            System.out.println("Error: Could not write to file");
        }

    }


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


    /*
     *
     */
    private static void sendPublicKey() throws IOException {
        FileOutputStream f_out = new FileOutputStream("public_key.ser");
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		obj_out.writeObject(publicKey_Server);
		obj_out.close();
    }


    /*
     *
     */
    private static PublicKey recievePubicKey() {
        File pk = new File("public_key.ser");
        while (pk.length() == 0) {
            // here we just wait
        }
        System.out.println("File Found...");
        try {
            FileInputStream f_in = new FileInputStream("public_key.ser");
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            PublicKey publicKey_Client = (PublicKey) obj_in.readObject();
            obj_in.close();
            pk.delete();
            return publicKey_Client;
        } catch (IOException e) {
            System.out.println("Error: IOException...");
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Class not found exception...");
            return null;
        }
     }

     /*
      *
      */
     private static String signString(String unsigned) {
         // signature
         try {
             byte[] data = "Digital Signature".getBytes();
             Signature signature = Signature.getInstance("SHA256withRSA");
             signature.initSign(privateKey);
             signature.update(data);
             byte[] signedData = signature.sign();
             System.out.println("LENGTH OF SIGNATURE IN SERVER: " + signedData.length);
             byte[] txt = unsigned.getBytes();
             System.out.println("LENGTH OF MESSAGE IN SERVER: " + txt.length);

             // combine arrays
             ByteArrayOutputStream bstream = new ByteArrayOutputStream();
             for(int i = 0; i < (txt.length + signedData.length); i++) {
                 if(i < txt.length) {
                     bstream.write(txt[i]);
                 } else {
                     bstream.write(signedData[i - txt.length]);
                 }
             }
             byte[] final_msg = bstream.toByteArray();
             System.out.println("LENGTH OF MESSAGE BEFORE SENDING: " + final_msg.length);
             String msg_out = new String(final_msg);
             return msg_out;
         } catch (NoSuchAlgorithmException e) {
             System.out.println("Error: No such algorithm...");
         } catch (InvalidKeyException e) {
             System.out.println("Error: Invalid key exception... here");
         } catch (SignatureException e) {
             System.out.println("Error: Signature exception...");
         }
         // we should return before this, this is for errors
         return null;
     }

	 
    public static void main(String[] args) {
        try {
            int port = 7802;
            ServerSocket serverSocket = new ServerSocket(port);
			Command_total = getCIA();
			selected();
			System.out.println("\nServer Started and listening to the port 7802\nReady for a client connection.");

			while(Running){
				socket = serverSocket.accept();
				Scanner scan1 = new Scanner(socket.getInputStream());
				PrintStream p = new PrintStream(socket.getOutputStream());
				Scanner message = new Scanner(System.in);
				String recived_1;
				recived_1 = scan1.nextLine();
				int recived = Integer.parseInt(recived_1);
				String returnMessage;

                if(Integrity){
                    // Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    keyGen.initialize(1024);
                    KeyPair keypair = keyGen.genKeyPair();
                    privateKey = keypair.getPrivate();
                    publicKey_Server = keypair.getPublic();
                    publicKey_Client = recievePubicKey();
                    sendPublicKey();
					byte[] data = "Digital Signature".getBytes();
					signature = Signature.getInstance("SHA256withRSA");
					signature.initSign(privateKey);
					signature.update(data);
					dsig = signature.sign();
                }
				String tempsig = new String();

				if(recived==Command_total){
					returnMessage = "Selected security properties were accepted";
				}else{
					returnMessage = "Selected security properties were denied";
				}
				if(Confidentiality){
					returnMessage = encrypt(returnMessage);
				}
				p.println(returnMessage);
				if(Integrity){
					String signedData = new String(dsig);
					p.println(signedData);
				}

				if(Confidentiality){
					System.out.println("Sent message to client: "+decrypt(returnMessage));
				}else{
					System.out.println("Sent message to client: "+(returnMessage));
				}

				String user = "unknown";
				String password = "unknown";
                // Authentication
				if(Command_total%2 == 1){
                    generateUserPass(DEFAULT_USER, DEFAULT_PASS);
					boolean checking_authentication = true;
					while(checking_authentication) {
						if(Confidentiality){
								p.println(encrypt("Please input Username:"));
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
						} else {
                            p.println("Please input Username:");
							if(Integrity){
								String signedData = new String(dsig);
								p.println(signedData);
							}
						}
						System.out.println("Message sent to the client is: Please input Username");

						try {
							user = scan1.nextLine();
						} catch(NoSuchElementException e) {
							System.out.println("should never get here");
						}
						if (Integrity){
							try{
								tempsig = scan1.nextLine();
							}catch(NoSuchElementException e){
								System.out.println("should never get here");
							}
							client_sig = tempsig.getBytes();
							signature.initVerify(publicKey_Client);
							if(signature.verify(client_sig)){
								System.out.println("Client Signature Verified");
							}else{
								System.out.println("Something is wrong with Verifcation");
							}
						}

						if(Confidentiality){
								p.println(encrypt("Please input Password:"));
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
						}else{
								p.println("Please input Password:");
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
						}

						System.out.println("Message sent to the client is: Please input Password");
						try {
							password = scan1.nextLine();
						} catch(NoSuchElementException e) {
							System.out.println("should never get here");
						}
						if (Integrity){
							try{
								tempsig = scan1.nextLine();
							}catch(NoSuchElementException e){
								System.out.println("should never get here");
							}
							client_sig = tempsig.getBytes();
							signature.initVerify(publicKey_Client);
							if(signature.verify(client_sig)){
								System.out.println("Client Signature Verified");
							}else{
								System.out.println("Something is wrong with Verifcation");
							}
						}
						if(Confidentiality){
								user = decrypt(user);
								password = decrypt(password);
						}

						if(check_user(user,password)) {
							if(Confidentiality){
								p.println(encrypt("Message sent to the client is: Username and Password accepted.  Instant Message initiated..."));
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
							}else{
								p.println("Message sent to the client is: Username and Password accepted.  Instant Message initiated...");
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
							}
							checking_authentication=false;
						} else {
							if(Confidentiality){
								p.println(encrypt("Username/Password is not vaild please try again"));
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
							}else{
								p.println("Username/Password is not vaild please try again");
								if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
								}
							}
						}
					}//while(checking authentication)
				} else {
					System.out.println("Since Authentication was not choosen the defualt username and password are UNKNOWN");
				}
				boolean communication = true;
				String reciver="";
				String sender="";
				while(communication){
					try {
						reciver = scan1.nextLine();
					} catch(NoSuchElementException e) {
							System.out.println("should never get here");
					}
					if (Integrity){
							try{
								tempsig = scan1.nextLine();
							}catch(NoSuchElementException e){
								System.out.println("should never get here");
							}
							client_sig = tempsig.getBytes();
							signature.initVerify(publicKey_Client);
							if(signature.verify(client_sig)){
								System.out.println("Client Signature Verified");
							}else{
								System.out.println("Something is wrong with Verifcation");
							}
					}
					if(Confidentiality){
								reciver = decrypt(reciver);
					}
					System.out.println("Client: "+ reciver);
					try {
							System.out.print("Server: ");
							sender = message.nextLine();
							if (sender.contains("!quit")) {
								break;
							}
						if(Confidentiality){
							sender = encrypt(sender);
						}
					}catch(NoSuchElementException e) {
							System.out.println("should never get here");
					}
					p.println(sender);
					if(Integrity){
									String signedData = new String(dsig);
									p.println(signedData);
					}
					//System.out.println("Server: "+ sender);
				}
				Running =false;
			}//while(Running)
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch(Exception e) {
                //error
            }
        }
    } // end main
} // end class Server
