import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AioTcpClient {
	public static JTextField jt = new JTextField();
	public static ConcurrentHashMap<String, AsynchronousSocketChannel> sockets = new ConcurrentHashMap<>(); // ����hash��

	// static AioTcpClient me;

	private AsynchronousChannelGroup asyncChannelGroup;

	public AioTcpClient() throws Exception {
		// �����̳߳�
		ExecutorService executor = Executors.newFixedThreadPool(20);
		// ��������ͨ��������
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
	}

	// String encode = "US-ASCII";
	// private final CharsetDecoder decoder =
	// Charset.forName("GBK").newDecoder();

	public void start(final String ip, final int port) throws Exception {
		// ����20000���������ӣ�ʹ��20���̵߳ĳ���
		for (int i = 0; i < 2; i++) {
//			for (int i = 0; i < 1; i++) {
			try {
				// �ͻ���socket.��Ȼ�����첽��ʽ�ġ�
				AsynchronousSocketChannel connector = null;
				if (connector == null || !connector.isOpen()) {
					// ���첽ͨ�����������õ��ͻ���socket
					connector = AsynchronousSocketChannel.open(asyncChannelGroup); // �������20��
					sockets.putIfAbsent(String.valueOf(i), connector);

					connector.setOption(StandardSocketOptions.TCP_NODELAY, true);
					connector.setOption(StandardSocketOptions.SO_REUSEADDR, true);
					connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					// ��ʼ���ӷ�����������ĵ�connectԭ����
					// connect(SocketAddress remote, A attachment,
					// CompletionHandler<Void,? super A> handler)
					// Ҳ��������CompletionHandler ��A�Ͳ�����������ĵ��÷���
					// �ĵڶ�����������������connector���ͻ�����������
					// V��Ϊnull
					connector.connect(new InetSocketAddress(ip, port), connector, new AioConnectHandler(i)); // ���������
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void work() throws Exception {
		// AioTcpClient client = new AioTcpClient();
		// client.start("localhost", 9008);

		start("localhost", 1009);

	}

	/////////////////////////////////////////////////
	// �ļ��ָ���ر���
	public AioSendHandler aioSendHandler; // ����handle
	public int curBlockIndex = 0; // ��ǰ���͵��ļ�������
	public long fileLength = 0; // �ļ�����
	public int fileNameLength = 0; // �ļ�������
	public String fileName; // �ļ���
	public int blockCnt = 0; // �ļ��ָ�������
	public int yu = 0; // �ļ��ָ�β��
	public int totalBlockCnt = 0; // �ļ��ܿ��������ܱ�blockCnt��1��yu��Ϊ��ʱ��yuΪ0ʱ����blockCnt���

	public ByteBuffer headBuffer; // �ļ�ͷbuffer

	public RandomAccessFile randomAccessFile; // �ļ������ȡ

	public byte[] blockBuffer = new byte[TransSetting.blockSize]; // ÿ�ζ�ȡ�ĳ���
																	// //1M

	public byte[] sendBlockBuffer; // ÿ�η��͵Ŀ飬headBuffer��blockBuffer
	public int sendBlockBufferOffset; // ͷ����ʼ��һ�μ���

	public AsynchronousSocketChannel sendSocket;

	/////////////////////////////////////////////////

	// sendӦ���Ǹ���ڷ�����Ӧ���и���Ӧ�ľ��巽��(ÿ�η���һ���ļ���)
	public void send() throws UnsupportedEncodingException {

		sendSocket = sockets.get("0");
		AsynchronousSocketChannel socket = sockets.get("0");
		if (aioSendHandler == null) {
			aioSendHandler = new AioSendHandler(socket, this);
		}

		// old send style��send message
		// String sendString = jt.getText();
		// // ByteBuffer
		// // clientBuffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));
		// ByteBuffer clientBuffer =
		// ByteBuffer.wrap(sendString.getBytes(TransSetting.TRANSENCODE));
		// socket.write(clientBuffer, clientBuffer, aioSendHandler);

		// ��ʼ��Ϣ
		// String filePath = "D:/���/128M�ֽ�.txt"; //�����
		// fileName = "D:/���/128M�ֽ�.txt";
//		 fileName = "F:/todo/C#/�첽test/128M�ֽ�.txt"; //32
		// fileName = "F:/todo/C#/�첽test/256M�ֽ�.txt";
//		fileName = "F:/todo/C#/�첽test/12-M�ֽ�.txt";  //32
//		fileName = "F:/todo/C#/�첽test/12M�ֽ�-.txt";  //32
//		fileName = "F:/todo/C#/�첽test/13-M�ֽ�.txt";
		// fileName = "F:/todo/C#/�첽test/16M�ֽ�.txt";
		// fileName = "F:/todo/C#/�첽test/1M�ֽ�.txt";
		// fileName = "F:/todo/C#/�첽test/12M�ֽ�.txt";
		// fileName = "E:/����/DVD1.iso";
//		 fileName = "E:/����/ubuntu-15.04-desktop-amd64.iso";  //�ļ�������Ҫ��server��AioAcceptHandler��startRead�����İ����ȶ�Ӧ
//		 fileName = "D:/������/visual studio/[Visual.Studio.2010.���������콢�棨MSDNԭ�����أ�����KEY].iso";  //88
		 fileName = "e:/ubuntu-18.04.2-desktop-amd64.iso";  //46
//		 fileName = "D:/������/visual studio/VS2013Documentation.iso";  //49
//		 fileName = "E:/����/cn_windows_7_ultimate_with_sp1_x64_dvd_618537.iso";  
//		 fileName = "E:/����/visio2003.iso";  //21
//		 fileName = "E:/����/JBuilder2005.rar";  //24
//		 fileName = "E:/����/SW_DVD5_Office_Professional_Plus_2013w_SP1_64Bit_ChnSimp_MLF_X19-35964.ISO";  //82
		File file = new File(fileName);
		// FileInputStream fis = new FileInputStream(file);
		fileLength = file.length(); // �ļ�����
		fileNameLength = fileName.getBytes().length; // �ļ������� //�ֽڳ���

		blockCnt = (int) (fileLength / TransSetting.blockSize); // ���ٿ��ļ�
		yu = (int) (fileLength % TransSetting.blockSize); // ����
		if (yu != 0) {
			totalBlockCnt = blockCnt + 1;
		} else {
			totalBlockCnt = blockCnt;
		}

		// ��ʼ�ļ�ͷ
		initBlockHead();

		// ����ļ�
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// �����ļ�����
		sendBlock();

	}

	//д��tmp�ļ�
//	private FileOutputStream fos = null;
//	private BufferedOutputStream bufferedOutputStream = null;

	
	// �ļ���ͷ��
	public void initBlockHead() {

		// sendBlockBufferOffset = 0;
		// sendBlockBuffer = new byte[sendBlockBufferOffset +
		// TransSetting.blockSize];

		//////////////////////////////////////
		// ���÷���block�Ĵ�С
		int fileNameByteLen = fileName.getBytes().length;
		// sendBlockBufferOffset = 14 + fileName.getBytes().length;
		sendBlockBufferOffset = 20 + fileNameByteLen;  //�ļ�������4, �ļ�����8, �鳤��4, ������4
		sendBlockBuffer = new byte[sendBlockBufferOffset + TransSetting.blockSize];

		// headBuffer = ByteBuffer.allocate(14 + fileName.getBytes().length);
		headBuffer = ByteBuffer.allocate(sendBlockBufferOffset);
		// headBuffer.putChar('7');
		// headBuffer.putChar('e');

		// headBuffer.putInt(fileName.length());// �ļ������� //�ļ���������ʱ��������ô��
		headBuffer.putInt(fileNameByteLen);// �ļ������� //�ļ���������ʱ��������ô��
		headBuffer.put(fileName.getBytes()); // �ļ���
		headBuffer.putLong(fileLength); // �ļ�����
		headBuffer.putInt(TransSetting.blockSize); // �鳤��
		 // ������
		

		byte[] headBytes = headBuffer.array();

		// System.arraycopy(headBytes, 0, sendBlockBuffer, 0, 16 +
		// fileName.getBytes().length); // ��������
		System.arraycopy(headBytes, 0, sendBlockBuffer, 0, sendBlockBufferOffset); // ��������

		//debug
//		if (bufferedOutputStream == null) {
//			System.out.println("�����ļ�");
//			// first = false;
//			File file = new File(System.getProperty("user.dir") + File.separator + "test"
//					+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
//			if (!file.exists()) {
//				try {
//					file.createNewFile();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			try {
//				FileOutputStream fos = new FileOutputStream(file);
//				bufferedOutputStream = new BufferedOutputStream(fos);
//				// ����hash��
//				// ctxMap.put(key, bufferedOutputStream);
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
		
	}

	// ���ͽ���
	public boolean isSenFinished() {
		if (curBlockIndex == totalBlockCnt) {
			return true;
		} else {
			return false;
		}
	}

	public void incCurBlockIndex() {
		curBlockIndex++; // ������ɼ�һ
	}

	public void decCurBlockIndex() {
		curBlockIndex--; // �ȼ�һ��������ʧ��ʱ�ټ�һ
	}

	private byte[] intToByteArray(int a) { 
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	} 
	
	// �����ļ���
	public void sendBlock() {

		// �ļ��ָ�
		// �ļ�ֻ��һ��
		// if (fileLength <= TransSetting.blockSize) {
		// try {
		// // the total number of bytes read into the buffer, or -1 if
		// // there is no more data because
		// // the end of this file has been reached.
		// int readCnt = randomAccessFile.read(sendBlockBuffer,
		// sendBlockBufferOffset, fileLength); // �����ļ���ô��
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// } else {
		// // �ļ�����һ��
		// try {
		// randomAccessFile.seek(curBlockIndex * TransSetting.blockSize);
		// int readCnt = randomAccessFile.read(sendBlockBuffer,
		// sendBlockBufferOffset, TransSetting.blockSize); // ���̶�����
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		// ͳһ
		ByteBuffer clientBuffer = null;
		try {
			System.out.println("cur block index : " + curBlockIndex);
			randomAccessFile.seek((long)curBlockIndex * TransSetting.blockSize);  //int����˳�����,��long
			int readCnt = 0;
			if (curBlockIndex < this.totalBlockCnt - 1) {
				//////////////////////
				byte[] indexBytes = intToByteArray(curBlockIndex);
				sendBlockBuffer[sendBlockBufferOffset-4] = indexBytes[0];
				sendBlockBuffer[sendBlockBufferOffset-3] = indexBytes[1];
				sendBlockBuffer[sendBlockBufferOffset-2] = indexBytes[2];
				sendBlockBuffer[sendBlockBufferOffset-1] = indexBytes[3];
				//////////////////////
				readCnt = randomAccessFile.read(sendBlockBuffer, sendBlockBufferOffset, TransSetting.blockSize); // ���̶�����
				clientBuffer = ByteBuffer.wrap(sendBlockBuffer);
				sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);
			} else {
				if (this.yu == 0) {
                    //////////////////////
					byte[] indexBytes = intToByteArray(curBlockIndex);
					sendBlockBuffer[sendBlockBufferOffset-4] = indexBytes[0];
					sendBlockBuffer[sendBlockBufferOffset-3] = indexBytes[1];
					sendBlockBuffer[sendBlockBufferOffset-2] = indexBytes[2];
					sendBlockBuffer[sendBlockBufferOffset-1] = indexBytes[3];
					//////////////////////
					
					readCnt = randomAccessFile.read(sendBlockBuffer, sendBlockBufferOffset, TransSetting.blockSize); // ���̶�����
					clientBuffer = ByteBuffer.wrap(sendBlockBuffer);
					sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);
				} else {
					
					ByteBuffer tmpHeadBuffer = ByteBuffer.allocate(sendBlockBufferOffset);

					tmpHeadBuffer.putInt(fileName.getBytes().length);// �ļ������� //�ļ���������ʱ��������ô��
					tmpHeadBuffer.put(fileName.getBytes()); // �ļ���
					tmpHeadBuffer.putLong(fileLength); // �ļ�����
					tmpHeadBuffer.putInt(yu); // ʣ�೤��

					byte[] tmpheadBytes = tmpHeadBuffer.array();
					
					byte[] sendBlockBufferLast = new byte[sendBlockBufferOffset + yu];
//					byte[] sendBlockBufferLast = new byte[sendBlockBufferOffset + TransSetting.blockSize];
					
					System.arraycopy(tmpheadBytes, 0, sendBlockBufferLast, 0, sendBlockBufferOffset); // ��������
					System.out.println("last size : " + sendBlockBufferLast.length);
					System.out.println("yu : " + yu);

                    //////////////////////
					byte[] indexBytes = intToByteArray(curBlockIndex);
					sendBlockBufferLast[sendBlockBufferOffset-4] = indexBytes[0];
					sendBlockBufferLast[sendBlockBufferOffset-3] = indexBytes[1];
					sendBlockBufferLast[sendBlockBufferOffset-2] = indexBytes[2];
					sendBlockBufferLast[sendBlockBufferOffset-1] = indexBytes[3];
					//////////////////////
					
					readCnt = randomAccessFile.read(sendBlockBufferLast, sendBlockBufferOffset, yu); // ��ʣ�೤��
					System.out.println("readCnt : " + readCnt);
					clientBuffer = ByteBuffer.wrap(sendBlockBufferLast);
					sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);

				}
				
			}

			//debug
//			byte[] tmpbyte = clientBuffer.array();
//			bufferedOutputStream.write(tmpbyte, 0, tmpbyte.length);
//			bufferedOutputStream.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		// �����ļ���
//		clientBuffer = ByteBuffer.wrap(sendBlockBuffer);
//		sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);

	}

	// �������
	public void createPanel() {

		// me=this;

		JFrame f = new JFrame("Wallpaper");
		f.getContentPane().setLayout(new BorderLayout());

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton bt = new JButton("����");
		p.add(bt);
		// me=this;
		bt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// me.send();
					send();

				} catch (Exception ex) {
					Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

		});

		bt = new JButton("����");
		p.add(bt);
		// me=this;
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}

		});

		f.getContentPane().add(jt, BorderLayout.CENTER);
		f.getContentPane().add(p, BorderLayout.EAST);

		f.setSize(450, 300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				AioTcpClient d = null;
				try {
					d = new AioTcpClient();
				} catch (Exception ex) {
					Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
				}

				d.createPanel();
				try {
					d.work();
				} catch (Exception ex) {
					Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
				}

			}
		});
	}
}
