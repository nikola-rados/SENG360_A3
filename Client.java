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


public class Client {
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{'Z', '4', 'e', 't', 'e', '_', 't', 'S', '-', '!', '2', '%', 't', 'K', 'e', '9'};
    private static PrivateKey privateKey;
    private static PublicKey publicKeyClient;
    private static PublicKey publicKeyServer;
    private static Signature signature;
    private static byte[] digital_signature;


    /* MAIN */
    public static void main(String[] args) throws UnknownHostException, IOException {
        //key test
        /*
        try {
            // generate key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024); // KeySize
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // private/public key generation
            PrivateKey privateKey1 = keyPair.getPrivate();
            PublicKey publicKey1 = keyPair.getPublic();

            // signature
            byte[] data = "Digital Signature".getBytes();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey1);
            signature.update(data);
            byte[] signedData = signature.sign();
            byte[] txt = "Testing this message".getBytes();

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

            // get signature to verify
            ByteArrayOutputStream s_verify = new ByteArrayOutputStream();
            for(int i = 0; i < signedData.length; i++) {
                s_verify.write(final_msg[txt.length + i]);
            }
            byte[] verify_this = s_verify.toByteArray();

            // get original message
            ByteArrayOutputStream s_txt = new ByteArrayOutputStream();
            for(int i = 0; i < txt.length; i++) {
                s_txt.write(final_msg[i]);
            }
            byte[] txt_f = s_txt.toByteArray();
            String txt_s = new String(txt_f);

            // verify
            signature.initVerify(publicKey1);
            signature.update(data);
            if(signature.verify(verify_this)){
                System.out.println("Verified");
                System.out.println("MESSAGE: " + txt_s);
            }else{
                System.out.println("Something is wrong");
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such algorithm exception...");
        } catch (InvalidKeyException e) {
            System.out.println("Invalid key exception...");
        } catch (SignatureException e) {
            System.out.println("Signature exception...");
        }
        //------- */

        String msg, server_str, sig_str;
        String verified = "";
        Scanner scan_client = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 7802);
        Scanner scan_server = new Scanner(socket.getInputStream());

        int cia = getCIA();

        // send server message containing triad number to check security protocols
        PrintStream cia_check = new PrintStream(socket.getOutputStream());
        cia_check.println(cia);
        System.out.println("--------------------------------------------------------");

        /*
         *  Integrity
         *  If we have selected the security option it is at this point we need
         *  to generate a key pair and exchange public keys with the Server.
         */
        if(cia == 7 || cia == 6 || cia == 3 || cia == 2) {
            generateKeyPair();
            sendPublicKey();
            publicKeyServer = recievePubicKey();
            createSignature();
            // testing key grabber
/*            try {
                // signature
                byte[] data = "Digital Signature".getBytes();
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(data);
                byte[] signedData = signature.sign();
                byte[] txt = "Testing this message".getBytes();

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

                // get signature to verify
                ByteArrayOutputStream s_verify = new ByteArrayOutputStream();
                for(int i = 0; i < signedData.length; i++) {
                    s_verify.write(final_msg[txt.length + i]);
                }
                byte[] verify_this = s_verify.toByteArray();

                // get original message
                ByteArrayOutputStream s_txt = new ByteArrayOutputStream();
                for(int i = 0; i < txt.length; i++) {
                    s_txt.write(final_msg[i]);
                }
                byte[] txt_f = s_txt.toByteArray();
                String txt_s = new String(txt_f);

                // verify
                sendPublicKey();
                PublicKey pk_test = recievePubicKey();
                signature.initVerify(pk_test);
                signature.update(data);
                if(signature.verify(verify_this)){
                    System.out.println("Verified");
                    System.out.println("MESSAGE: " + txt_s);
                }else{
                    System.out.println("Something is wrong");
                }
            } catch (NoSuchAlgorithmException e) {
                System.out.println("No such algorithm exception...");
            } catch (InvalidKeyException e) {
                System.out.println("Invalid key exception...");
            } catch (SignatureException e) {
                System.out.println("Signature exception...");
            }*/
        }

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
                // Integrity loop check
                if (cia == 7 || cia == 6 || cia == 3 || cia == 2) {
                    // this var is the digital signature from the server
                    sig_str = scan_server.nextLine();
                    byte[] tobeverified = sig_str.getBytes();
                    try {
                        signature.initVerify(publicKeyServer);
                        if (signature.verify(tobeverified)) {
                            verified = "(VERIFIED)";
                        } else {
                            verified = "(UNVERIFIED)";
                        }
                    } catch (SignatureException e) {
                        System.out.println("Error: Signature Exception...");
                    } catch (InvalidKeyException e) {
                        System.out.println("Error: Invalid key Exception...");
                    }

                }
                System.out.println("Server: " + server_str + " " + verified);
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
            // Integrity check
            if(cia == 7 || cia == 6 || cia == 3 || cia == 2) {
                String out = new String(digital_signature);
                client_out.println(out);
            }
        }
    } // end main


    /*
     *
     */
    private static void createSignature() {
        // signature
        try {
            byte[] data = "Digital Signature".getBytes();
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            digital_signature = signature.sign();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such algorithm exception...");
        } catch (InvalidKeyException e) {
            System.out.println("Invalid key exception...");
        } catch (SignatureException e) {
            System.out.println("Signature exception...");
        }
    }

    /*
     *
     */
    private static String extractMessage(String full_msg) {
        // separate the message from the signature
        byte[] msg_b = full_msg.getBytes();
        System.out.println("FULL MESSAGE LENGTH WHEN RECEIVING: " + msg_b.length);
        ByteArrayOutputStream s_txt = new ByteArrayOutputStream();
        for(int i = 0; i < (msg_b.length - 128); i++) {
            s_txt.write(msg_b[i]);
        }
        byte[] txt_f = s_txt.toByteArray();
        String txt_s = new String(txt_f);
        return txt_s;
    }


    /*
     *
     */
    private static void sendPublicKey() throws IOException {
        FileOutputStream f_out = new FileOutputStream("public_key.ser");
		ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
		obj_out.writeObject(publicKeyClient);
		obj_out.close();
    }


    /*
     *
     */
    private static PublicKey recievePubicKey() {
        File pk = new File("public_key.ser");
        while (pk.length() == 0) {
            System.out.println("Waiting for file");
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
     *  Create key pairing public/private.
     */
    private static void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair keypair = keyGen.genKeyPair();
            privateKey = keypair.getPrivate();
            publicKeyClient = keypair.getPublic();
        } catch (Exception e) {
            System.out.println("Error:");
        }
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
