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
	public static ConcurrentHashMap<String, AsynchronousSocketChannel> sockets = new ConcurrentHashMap<>(); // 并行hash表

	// static AioTcpClient me;

	private AsynchronousChannelGroup asyncChannelGroup;

	public AioTcpClient() throws Exception {
		// 创建线程池
		ExecutorService executor = Executors.newFixedThreadPool(20);
		// 创建异眇通道管理器
		asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
	}

	// String encode = "US-ASCII";
	// private final CharsetDecoder decoder =
	// Charset.forName("GBK").newDecoder();

	public void start(final String ip, final int port) throws Exception {
		// 启动20000个并发连接，使用20个线程的池子
		for (int i = 0; i < 2; i++) {
//			for (int i = 0; i < 1; i++) {
			try {
				// 客户端socket.当然它是异步方式的。
				AsynchronousSocketChannel connector = null;
				if (connector == null || !connector.isOpen()) {
					// 从异步通道管理器处得到客户端socket
					connector = AsynchronousSocketChannel.open(asyncChannelGroup); // 最多启动20个
					sockets.putIfAbsent(String.valueOf(i), connector);

					connector.setOption(StandardSocketOptions.TCP_NODELAY, true);
					connector.setOption(StandardSocketOptions.SO_REUSEADDR, true);
					connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					// 开始连接服务器。这里的的connect原型是
					// connect(SocketAddress remote, A attachment,
					// CompletionHandler<Void,? super A> handler)
					// 也就是它的CompletionHandler 的A型参数是由这里的调用方法
					// 的第二个参数决定。即是connector。客户端连接器。
					// V型为null
					connector.connect(new InetSocketAddress(ip, port), connector, new AioConnectHandler(i)); // 连到服务端
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
	// 文件分割相关变量
	public AioSendHandler aioSendHandler; // 发送handle
	public int curBlockIndex = 0; // 当前发送的文件块索引
	public long fileLength = 0; // 文件长度
	public int fileNameLength = 0; // 文件名长度
	public String fileName; // 文件名
	public int blockCnt = 0; // 文件分割整块数
	public int yu = 0; // 文件分割尾部
	public int totalBlockCnt = 0; // 文件总块数，可能比blockCnt大1，yu不为零时，yu为0时，与blockCnt相等

	public ByteBuffer headBuffer; // 文件头buffer

	public RandomAccessFile randomAccessFile; // 文件随机读取

	public byte[] blockBuffer = new byte[TransSetting.blockSize]; // 每次读取的长度
																	// //1M

	public byte[] sendBlockBuffer; // 每次发送的块，headBuffer加blockBuffer
	public int sendBlockBufferOffset; // 头部初始化一次即可

	public AsynchronousSocketChannel sendSocket;

	/////////////////////////////////////////////////

	// send应该是个入口方法，应该有个对应的具体方法(每次发送一个文件块)
	public void send() throws UnsupportedEncodingException {

		sendSocket = sockets.get("0");
		AsynchronousSocketChannel socket = sockets.get("0");
		if (aioSendHandler == null) {
			aioSendHandler = new AioSendHandler(socket, this);
		}

		// old send style，send message
		// String sendString = jt.getText();
		// // ByteBuffer
		// // clientBuffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));
		// ByteBuffer clientBuffer =
		// ByteBuffer.wrap(sendString.getBytes(TransSetting.TRANSENCODE));
		// socket.write(clientBuffer, clientBuffer, aioSendHandler);

		// 初始信息
		// String filePath = "D:/最发数/128M字节.txt"; //先如此
		// fileName = "D:/最发数/128M字节.txt";
//		 fileName = "F:/todo/C#/异步test/128M字节.txt"; //32
		// fileName = "F:/todo/C#/异步test/256M字节.txt";
//		fileName = "F:/todo/C#/异步test/12-M字节.txt";  //32
//		fileName = "F:/todo/C#/异步test/12M字节-.txt";  //32
//		fileName = "F:/todo/C#/异步test/13-M字节.txt";
		// fileName = "F:/todo/C#/异步test/16M字节.txt";
		// fileName = "F:/todo/C#/异步test/1M字节.txt";
		// fileName = "F:/todo/C#/异步test/12M字节.txt";
		// fileName = "E:/工具/DVD1.iso";
//		 fileName = "E:/工具/ubuntu-15.04-desktop-amd64.iso";  //文件名长度要与server的AioAcceptHandler的startRead方法的包长度对应
//		 fileName = "D:/编程软件/visual studio/[Visual.Studio.2010.简体中文旗舰版（MSDN原版下载）内置KEY].iso";  //88
		 fileName = "e:/ubuntu-18.04.2-desktop-amd64.iso";  //46
//		 fileName = "D:/编程软件/visual studio/VS2013Documentation.iso";  //49
//		 fileName = "E:/工具/cn_windows_7_ultimate_with_sp1_x64_dvd_618537.iso";  
//		 fileName = "E:/工具/visio2003.iso";  //21
//		 fileName = "E:/工具/JBuilder2005.rar";  //24
//		 fileName = "E:/工具/SW_DVD5_Office_Professional_Plus_2013w_SP1_64Bit_ChnSimp_MLF_X19-35964.ISO";  //82
		File file = new File(fileName);
		// FileInputStream fis = new FileInputStream(file);
		fileLength = file.length(); // 文件长度
		fileNameLength = fileName.getBytes().length; // 文件名长度 //字节长度

		blockCnt = (int) (fileLength / TransSetting.blockSize); // 多少块文件
		yu = (int) (fileLength % TransSetting.blockSize); // 求余
		if (yu != 0) {
			totalBlockCnt = blockCnt + 1;
		} else {
			totalBlockCnt = blockCnt;
		}

		// 初始文件头
		initBlockHead();

		// 随机文件
		try {
			randomAccessFile = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// 启动文件发送
		sendBlock();

	}

	//写入tmp文件
//	private FileOutputStream fos = null;
//	private BufferedOutputStream bufferedOutputStream = null;

	
	// 文件块头部
	public void initBlockHead() {

		// sendBlockBufferOffset = 0;
		// sendBlockBuffer = new byte[sendBlockBufferOffset +
		// TransSetting.blockSize];

		//////////////////////////////////////
		// 设置发送block的大小
		int fileNameByteLen = fileName.getBytes().length;
		// sendBlockBufferOffset = 14 + fileName.getBytes().length;
		sendBlockBufferOffset = 20 + fileNameByteLen;  //文件名长度4, 文件长度8, 块长度4, 索引号4
		sendBlockBuffer = new byte[sendBlockBufferOffset + TransSetting.blockSize];

		// headBuffer = ByteBuffer.allocate(14 + fileName.getBytes().length);
		headBuffer = ByteBuffer.allocate(sendBlockBufferOffset);
		// headBuffer.putChar('7');
		// headBuffer.putChar('e');

		// headBuffer.putInt(fileName.length());// 文件名长度 //文件名有中文时，长度怎么算
		headBuffer.putInt(fileNameByteLen);// 文件名长度 //文件名有中文时，长度怎么算
		headBuffer.put(fileName.getBytes()); // 文件名
		headBuffer.putLong(fileLength); // 文件长度
		headBuffer.putInt(TransSetting.blockSize); // 块长度
		 // 索引号
		

		byte[] headBytes = headBuffer.array();

		// System.arraycopy(headBytes, 0, sendBlockBuffer, 0, 16 +
		// fileName.getBytes().length); // 拷贝数组
		System.arraycopy(headBytes, 0, sendBlockBuffer, 0, sendBlockBufferOffset); // 拷贝数组

		//debug
//		if (bufferedOutputStream == null) {
//			System.out.println("创建文件");
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
//				// 存入hash表
//				// ctxMap.put(key, bufferedOutputStream);
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
		
	}

	// 发送结束
	public boolean isSenFinished() {
		if (curBlockIndex == totalBlockCnt) {
			return true;
		} else {
			return false;
		}
	}

	public void incCurBlockIndex() {
		curBlockIndex++; // 发送完成加一
	}

	public void decCurBlockIndex() {
		curBlockIndex--; // 先加一，若发送失败时再减一
	}

	private byte[] intToByteArray(int a) { 
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	} 
	
	// 发送文件块
	public void sendBlock() {

		// 文件分割
		// 文件只有一块
		// if (fileLength <= TransSetting.blockSize) {
		// try {
		// // the total number of bytes read into the buffer, or -1 if
		// // there is no more data because
		// // the end of this file has been reached.
		// int readCnt = randomAccessFile.read(sendBlockBuffer,
		// sendBlockBufferOffset, fileLength); // 读到文件那么长
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// } else {
		// // 文件大于一块
		// try {
		// randomAccessFile.seek(curBlockIndex * TransSetting.blockSize);
		// int readCnt = randomAccessFile.read(sendBlockBuffer,
		// sendBlockBufferOffset, TransSetting.blockSize); // 读固定长度
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		// 统一
		ByteBuffer clientBuffer = null;
		try {
			System.out.println("cur block index : " + curBlockIndex);
			randomAccessFile.seek((long)curBlockIndex * TransSetting.blockSize);  //int型相乘超界了,换long
			int readCnt = 0;
			if (curBlockIndex < this.totalBlockCnt - 1) {
				//////////////////////
				byte[] indexBytes = intToByteArray(curBlockIndex);
				sendBlockBuffer[sendBlockBufferOffset-4] = indexBytes[0];
				sendBlockBuffer[sendBlockBufferOffset-3] = indexBytes[1];
				sendBlockBuffer[sendBlockBufferOffset-2] = indexBytes[2];
				sendBlockBuffer[sendBlockBufferOffset-1] = indexBytes[3];
				//////////////////////
				readCnt = randomAccessFile.read(sendBlockBuffer, sendBlockBufferOffset, TransSetting.blockSize); // 读固定长度
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
					
					readCnt = randomAccessFile.read(sendBlockBuffer, sendBlockBufferOffset, TransSetting.blockSize); // 读固定长度
					clientBuffer = ByteBuffer.wrap(sendBlockBuffer);
					sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);
				} else {
					
					ByteBuffer tmpHeadBuffer = ByteBuffer.allocate(sendBlockBufferOffset);

					tmpHeadBuffer.putInt(fileName.getBytes().length);// 文件名长度 //文件名有中文时，长度怎么算
					tmpHeadBuffer.put(fileName.getBytes()); // 文件名
					tmpHeadBuffer.putLong(fileLength); // 文件长度
					tmpHeadBuffer.putInt(yu); // 剩余长度

					byte[] tmpheadBytes = tmpHeadBuffer.array();
					
					byte[] sendBlockBufferLast = new byte[sendBlockBufferOffset + yu];
//					byte[] sendBlockBufferLast = new byte[sendBlockBufferOffset + TransSetting.blockSize];
					
					System.arraycopy(tmpheadBytes, 0, sendBlockBufferLast, 0, sendBlockBufferOffset); // 拷贝数组
					System.out.println("last size : " + sendBlockBufferLast.length);
					System.out.println("yu : " + yu);

                    //////////////////////
					byte[] indexBytes = intToByteArray(curBlockIndex);
					sendBlockBufferLast[sendBlockBufferOffset-4] = indexBytes[0];
					sendBlockBufferLast[sendBlockBufferOffset-3] = indexBytes[1];
					sendBlockBufferLast[sendBlockBufferOffset-2] = indexBytes[2];
					sendBlockBufferLast[sendBlockBufferOffset-1] = indexBytes[3];
					//////////////////////
					
					readCnt = randomAccessFile.read(sendBlockBufferLast, sendBlockBufferOffset, yu); // 读剩余长度
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

		// 发送文件块
//		clientBuffer = ByteBuffer.wrap(sendBlockBuffer);
//		sendSocket.write(clientBuffer, clientBuffer, aioSendHandler);

	}

	// 创建面板
	public void createPanel() {

		// me=this;

		JFrame f = new JFrame("Wallpaper");
		f.getContentPane().setLayout(new BorderLayout());

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton bt = new JButton("点我");
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

		bt = new JButton("结束");
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
