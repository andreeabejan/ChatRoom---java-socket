import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;
	
	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.username = username;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
		
	}
	
	public void sendMessage() {
		try {
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			Scanner scanner = new Scanner(System.in);
			while(socket.isConnected()) {
				String messageToSend = scanner.nextLine();
				bufferedWriter.write(username + ": " + messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		}catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
		
	}
	
	/* wait for messages from other chat users
	 * */
	public void listenForMessage() {
		//use a new thread, because it is a blocking operation
		new Thread(new Runnable(){
			@Override
			public void run() {
				String msgFromGroupChat;
				while(socket.isConnected()) {
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(msgFromGroupChat);
					}catch(IOException e) {
						closeEverything(socket, bufferedReader, bufferedWriter);
					}
				}
			}
		}).start();
	}
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		try {
			//in order to not get a no pointer exception
			if(bufferedReader != null) {
				bufferedReader.close(); 
			}
			if(bufferedWriter != null) {
				bufferedWriter.close();
			}
			if(socket != null) {
				socket.close();
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username for the group chat: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 1234);
		Client client = new Client(socket, username);
		
		//both technically blocking, but because of separate threads they will run at the same time
		client.listenForMessage();
		client.sendMessage();
	}

}
