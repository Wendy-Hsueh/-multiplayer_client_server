import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Server extends JFrame 
{
	private JTextArea txt = new JTextArea("伺服器已經啟動");
	private ServerSocket serverSocket = null;
	private static Map<Socket, Integer> list = new LinkedHashMap<Socket,Integer>();	
	private ExecutorService exec= null;
	
	public Server() throws IOException
	{
		setLayout(new BorderLayout());
		this.add(new JScrollPane(txt),BorderLayout.CENTER);
		setSize(500,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		serverSocket = new ServerSocket(54321);
		exec = Executors.newCachedThreadPool(); 
		while(true)
		{
			Socket socket = null;
			socket = serverSocket.accept();
			System.out.println(1);
			list.put(socket,socket.getPort());
			exec.execute(new Communication(socket));
		}
	}
	
	public class Communication implements Runnable 
	{
		private Socket socket;
		private DataInputStream fromClient =null;
		private DataOutputStream toClient = null;
		private String msg;
		
		public Communication(Socket socket) throws IOException
		{
			this.socket = socket;
			fromClient = new DataInputStream(socket.getInputStream());
			msg ="["+socket.getPort()+ "]已連線到伺服器";
			txt.append(msg+"\n");
			sendMessage();
			msg="";
		}
		
		@Override
		public void run() 
		{
			try {
				while((msg= fromClient.readUTF())!=null){
					msg = "[" + socket.getPort() + "]："+ msg;
					txt.append(msg+"\n");
					sendMessage();
				}
			} catch (Exception e) {
			}
			
		}
	
		public void sendMessage()  
		{
			Iterator it = list.entrySet().iterator();
			while (it.hasNext()) 
			{
				Entry entry = (Entry) it.next();
				Socket client =(Socket) entry.getKey();
				int port = (Integer) entry.getValue();
				try{
					toClient = new DataOutputStream(client.getOutputStream());
					toClient.writeUTF(msg);	
				} catch (IOException e) {
					e.printStackTrace();
				}					
			}
		}
	}
	
	public static void main(String[] args) throws IOException 
	{
		Server server = new Server();
	}
}