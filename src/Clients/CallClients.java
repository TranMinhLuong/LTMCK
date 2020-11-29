package Clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import Models.CallingMessenger;

import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class CallClients extends JFrame {

	private JPanel contentPane;
	JLabel lbldifClient;
	Webcam webCam;
	Socket socket;
	WebcamPanel webcamPanel;
	private JPanel panel;
	private JTextField txtIP;
	private JButton btnNewButton;
	private JTextField txtPort;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CallClients frame = new CallClients();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 * @throws UnknownHostException 
	 */
	public CallClients() throws Exception {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 617, 506);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout(0,0));
		contentPane.add(jPanel,BorderLayout.EAST);
		
		lbldifClient = new JLabel("");
		lbldifClient.setPreferredSize(new Dimension(650, 650));
		lbldifClient.setBackground(Color.RED);
		jPanel.add(lbldifClient, BorderLayout.CENTER);
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		lblNewLabel = new JLabel("IP");
		lblNewLabel.setPreferredSize(new Dimension(30, 14));
		panel.add(lblNewLabel);
		
		txtIP = new JTextField();
		panel.add(txtIP);
		txtIP.setColumns(10);
		
		lblNewLabel_1 = new JLabel("Port");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setPreferredSize(new Dimension(50, 14));
		panel.add(lblNewLabel_1);
		
		txtPort = new JTextField();
		panel.add(txtPort);
		txtPort.setColumns(10);
		
		btnNewButton = new JButton("Kết nối");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					socket = new Socket(txtIP.getText().toString().trim(),Integer.parseInt(txtPort.getText().toString()));
					new ReceiveThread().start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		panel.add(btnNewButton);
		
		webCam = Webcam.getDefault();
		webCam.setViewSize(WebcamResolution.VGA.getSize());
		webcamPanel = new WebcamPanel(webCam);
		webcamPanel.setImageSizeDisplayed(true);
		webcamPanel.setFPSDisplayed(true);
		webcamPanel.setMirrored(true);
		webcamPanel.setDisplayDebugInfo(true);
		contentPane.add(webcamPanel,BorderLayout.CENTER);
		
		new ServerThread(3000);
		
	}
	
	public ImageIcon reIcon(byte[] path) {
		ImageIcon img = new ImageIcon(path);
		Image im = img.getImage().getScaledInstance(lbldifClient.getWidth(), lbldifClient.getHeight(), Image.SCALE_SMOOTH);
		ImageIcon anh = new ImageIcon(im);
		return anh;
	}
	
	class ServerThread extends Thread {
		ServerSocket serverSocket;
		public ServerThread(int port) {
			try {
				serverSocket = new ServerSocket(port);
				start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		@Override
		public void run() {
			try {	
				while(true) {
					Socket client = serverSocket.accept(); 
					System.out.println("Co nguoi ket noi");
					new SendClient(client);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class ReceiveThread extends Thread {

		@Override
		public void run() {
			CallingMessenger calling = null;
			try {
				while (true) {
					InputStream inputStream = socket.getInputStream();
					ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
					calling = (CallingMessenger) objectInputStream.readObject();
					lbldifClient.setIcon(reIcon(calling.getImg()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class SendClient extends Thread{
		Socket socket;
		
		public SendClient(Socket socket) {
			super();
			this.socket = socket;
			start();
		}

		@Override
		public void run() {
			boolean loop = true;
			BufferedImage bi;
			while (loop) {
				try {
					bi = webcamPanel.getImage();
					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
					ImageIO.write(bi, "jpg", bStream);
					sendMessage(socket,bStream.toByteArray());
				} catch (Exception e) {
					if (socket == null) {
						loop = false;
						e.printStackTrace();
					}
				}
			}
		}
		
		public void sendMessage(Socket socket,byte[] img) throws Exception {
	        OutputStream ops = socket.getOutputStream();
	        ObjectOutputStream ots = new ObjectOutputStream(ops);
	        ots.writeObject(new CallingMessenger(img));
	        ots.flush();
	    }
	}
}
