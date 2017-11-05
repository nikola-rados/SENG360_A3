import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{

    private static Socket socket;
	public static int Authentication;
	public static int Integrity;
	public static int Confidentiality;
    public static void main(String[] args){
        try{

            int port = 7802;
			
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Server Side Running, requires information:");
			System.out.print("Do you want Authentication? \nPlease type '0' for no and '1' for yes:"); 
			Scanner sc = new Scanner(System.in);
			Authentication = sc.nextInt();
			System.out.print("Do you want Integrity? \nPlease type '0' for no and '1' for yes:"); 
			Integrity = sc.nextInt();
			System.out.print("Do you want Confidentiality? \nPlease type '0' for no and '1' for yes:"); 
			Confidentiality = sc.nextInt();
			
			System.out.println("Server Started and listening to the port 7802");

            //Server is running always. This is done using this while(true) loop
/*			while(true)
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String recived = br.readLine();
*/


/*             String returnMessage;
				returnMessage="hello";

                //Sending the response back to the client.
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(returnMessage);
                System.out.println("Message sent to the client is "+returnMessage);
                bw.flush();
*/          
       }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
    }
}
