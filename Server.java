/*
SENG 360 Fall 2017
Assignment 3



Things to keep in mind:


	You should compare the (hashed) password to a password hash in a protected (access controlled) directory.

	When I talk about "protected directory", I am referring to a directory that is access controlled, so that it is not accessible to the world.


	Integrity just ensures that the message has not been changed while in transit between client and server. You can ensure this by using message authentication codes (with AES) (see your question 5).

	Authentication also requires that users authenticate. This can be done using a password authentication on client and server, respectively.


TO do:

	when values are equal:
	1 - 001
	2 - 010
	3 - 011
	4 - 100
	5 - 101
	6 - 110
	7 - 111
	0 - 000


*/


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
import java.util.NoSuchElementException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;








public class Server{

    private static Socket socket;
	public static int Authentication;
	public static int Integrity;
	public static int Confidentiality;
	public static int Command_total;
	public static boolean Running = true;


// unused
private static void commands(){
	        System.out.println("Server Side Running, requires information:\n");

			System.out.print("Do you want Confidentiality? \nPlease type 'n' for no and 'y' for yes:");
			Scanner sc = new Scanner(System.in);
			Confidentiality = sc.nextInt();
				if(Confidentiality!=0 && Confidentiality!= 1){
					System.out.println("Invalid input");
					System.exit(0);
				}
			System.out.print("Do you want Integrity? \nPlease type 'n' for no and 'y' for yes:");
			Integrity = sc.nextInt();
				if(Integrity!=0 && Integrity!= 1){
						System.out.println("Invalid input");
						System.exit(0);
				}
			System.out.print("Do you want Authentication? \nPlease type 'n' for no and 'y' for yes:");
			Authentication = sc.nextInt();
				if(Authentication!=0 && Authentication!= 1){
						System.out.println("Invalid input");
						System.exit(0);
				}
//			selected(Confidentiality,Integrity,Authentication);
}



/**
* This is the method that confirms what was requested
*/
public static void selected(){
	System.out.println("\nYou have Selected the Following:");
	if (Command_total>4){
		System.out.println("\tYou want Confidentiality");
	}else{
		System.out.println("\tYou do not want Confidentiality");
	}
	if (Command_total==2||Command_total==6||Command_total==7||Command_total==3){
		System.out.println("\tYou want Integrity");
	}else{
		System.out.println("\tYou do not want Integrity");
	}
	if (Command_total==7||Command_total==5||Command_total==3||Command_total==1){
		System.out.println("\tYou want Authentication");
	}else{
		System.out.println("\tYou do not want Authentication");
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


public static boolean check_user(String name, String pword){
	if(name=="Nik"){
		return false;
	}else{
		return true;
	}
}



public static void main(String[] args){
        try{

            int port = 7802;
            ServerSocket serverSocket = new ServerSocket(port);
			Command_total = getCIA();
			selected();
			System.out.println("\nServer Started and listening to the port 7802\nReady for a client connection.");
			socket = serverSocket.accept();
			Scanner scan1 = new Scanner(socket.getInputStream());
			PrintStream p = new PrintStream(socket.getOutputStream());

			while(Running){
				String recived_1;
				recived_1 = scan1.nextLine();
				int recived = Integer.parseInt(recived_1);



				String returnMessage;
				if(recived==Command_total){
					returnMessage = "Selected security properties were accepted";
				}else{
					returnMessage = "Selected security properties were denied";
				}


				p.println(returnMessage);
				System.out.println("Sent message to client: "+returnMessage);


				String user = "unknown";
				String password = "unknown";
				if (Command_total==7||Command_total==5||Command_total==3||Command_total==1){
					boolean checking_authentication =true;
					while(checking_authentication){
						p.println("Please input Username:");
						System.out.println("Message sent to the client is: Please input Username");

						try{
							user = scan1.nextLine();
						}catch(NoSuchElementException e){
							System.out.println("should never get here");
						}

						System.out.println("Message sent to the client is: Please input Password");
						try{
						password = scan1.nextLine();
						}catch(NoSuchElementException e){
							System.out.println("should never get here");
						}

						if (check_user(user,password)){
							checking_authentication=false;
						}else{
							p.println("Username/Password is not vaild please try again");
						}


					}//while checking authentication
				}else{

				}
				Running =false;
			}//running while




        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try{
                socket.close();
            }catch(Exception e){}
        }
        int i = 0;
        while(true) {
            if(i == 0) {
                System.out.print("Waiting\r");
                i++;
            } else if (i == 1) {
                System.out.print("Waiting.\r");
                i++;
            } else if (i == 2) {
                System.out.print("Waiting..\r");
                i++;
            } else {
                System.out.print("Waiting...\r");
                i = 0;
            }
        } // end while
    } // end main
} // end class Server
