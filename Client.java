import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

    public static void main(String[] args) throws UnknownHostException, IOException {
        String msg, temp;

        Scanner scan = new Scanner(System.in);
        Socket socket = new Socket("127.0.0.1", 7802);
        Scanner scan1 = new Scanner(socket.getInputStream());

        System.out.println("Enter a message:\n");
        msg = scan.nextLine();
        PrintStream p = new PrintStream(socket.getOutputStream());
        p.println(msg);

        try {
            temp = scan1.nextLine();
            System.out.println(temp);
        }
        catch(NoSuchElementException) {
            System.out.println("--- No message found ---");
        }
    }
}
