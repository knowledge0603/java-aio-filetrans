import java.io.BufferedOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


//����Ĳ����ͺţ��ܵ������ĺ����������������ܿͻ���socket.read����
public class AioReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel socket;
	public String msg;

	WriteBlockThread writeBlockThread;
	
	public AioReadHandler(AsynchronousSocketChannel socket) {
		this.socket = socket;
		
		writeBlockThread = new WriteBlockThread(100);
		Thread writeThread = new Thread(writeBlockThread);
		writeThread.start();
		
	}

	// String encode = "US-ASCII";
	// private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	// private CharsetDecoder decoder =
	// Charset.forName("US-ASCII").newDecoder();

	private boolean first = true;
	private FileOutputStream fos = null;
	private BufferedOutputStream bufferedOutputStream = null;
	
	private FileOutputStream debugos = null;
//	private BufferedOutputStream debugStream = null;
	private PrintStream debugStream = null;

	private CharsetDecoder decoder = Charset.forName(TransSetting.TRANSENCODE).newDecoder();

	private int cnt =0;
	
	@Override
	public void completed(Integer i, ByteBuffer buf) {
		if (i > 0) {

			// long completeTime = System.currentTimeMillis();
			// System.out.println("���ʱ�� : " + completeTime);

			buf.flip();
			ByteBuffer tmpBuffer = null;
			try {
				
				msg = decoder.decode(buf).toString();
				
//				ByteBuffer tmpBuffer = ByteBuffer.allocate(buf.array().length);
				tmpBuffer = buf.duplicate(); //����ֵ��֮�������buf֮��read��������ֵ��Ӱ��
				
				// // System.out.println("�յ�" +
				// // socket.getRemoteAddress().toString() + "����Ϣ:" + msg);
				// //TODO
				
				buf.compact();
				
			} catch (CharacterCodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			socket.read(buf, buf, this);
			
//			if (msg.length() == 1024 * 1024 + 33) 
//			if (msg.length() > 1024 * 1024) 
			if (msg.length() > 12) 
			{  
//				cnt++;
//
//				tmpBuffer.position(0); // ��������λ��
//
//				// ��ͷ��
//				int fileNameLen = tmpBuffer.getInt();
//				tmpBuffer.position(16 + fileNameLen); // ֻ��ȡblockcnt��index
//
//				int curIndex = tmpBuffer.getInt();
//				
//				if (debugStream == null) {
//				System.out.println("�����ļ�");
//				// first = false;
//								
//				File debugfile = new File(System.getProperty("user.dir") + File.separator + "receiveDebug"
//						+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
//				if (!debugfile.exists()) {
//					try {
//						debugfile.createNewFile();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				try {
//					debugos = new FileOutputStream(debugfile);
//					debugStream = new PrintStream(debugos);
//
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				}
//			}
//				debugStream.print(cnt);
//				debugStream.print("    ");
//				if(cnt==curIndex+1){
//				debugStream.println(curIndex+1);
//				}
//				else{
//					debugStream.print(curIndex+1);
//					debugStream.println("    notequal");
//				}
//				debugStream.flush();
				
				//�������
				writeBlockThread.addBlock(tmpBuffer);
				
				//28
////			if (bytes.length == 1024 * 1024 + 40) {  //40
//				// System.out.println("read cnt : " + msg);
//
//				if (bufferedOutputStream == null) {
//					System.out.println("�����ļ�");
//					// first = false;
//					File file = new File(System.getProperty("user.dir") + File.separator + "test"
//							+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
//					if (!file.exists()) {
//						try {
//							file.createNewFile();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					
//					File debugfile = new File(System.getProperty("user.dir") + File.separator + "debug"
//							+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
//					if (!debugfile.exists()) {
//						try {
//							debugfile.createNewFile();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//					
//					try {
//						FileOutputStream fos = new FileOutputStream(file);
//						bufferedOutputStream = new BufferedOutputStream(fos);
//						// ����hash��
//						// ctxMap.put(key, bufferedOutputStream);
//						
//						debugos = new FileOutputStream(debugfile);
////						debugStream = new BufferedOutputStream(debugos);
//						debugStream = new PrintStream(debugos);
//
//					} catch (FileNotFoundException e) {
//						e.printStackTrace();
//					}
//				}
//				
//				try {
//					cnt ++;
//					System.out.println("cur cnt : " + cnt );
//                    //ת����ȡ
////					byte[] wbyte = msg.getBytes(TransSetting.TRANSENCODE);
//					
//					tmpBuffer.position(0);  //��������λ��
//					
//					//��ͷ��
//					System.out.println("0 cur pos : " + tmpBuffer.position());
//					int fileNameLen = tmpBuffer.getInt();
//					System.out.println("file name len : " + fileNameLen);
//					System.out.println("1 cur pos : " + tmpBuffer.position());
//					
//					
////					byte[] fileNameByte = new byte[fileNameLen];
////					tmpBuffer.get(fileNameByte, 0, fileNameLen);
////					
//////					String fileNameStr = new String(fileNameByte, "GB2312");  //"GB2312"
////					System.out.println("2 cur pos : " + tmpBuffer.position());
////					long fileLen = tmpBuffer.getLong(); //�ļ���
////					System.out.println("fileLen : " + fileLen);
////					
////					System.out.println("3 cur pos : " + tmpBuffer.position());
//					
//					tmpBuffer.position(12+fileNameLen);  //ֻ��ȡblockcnt��index
//					
//					int blockCnt = tmpBuffer.getInt();
//					System.out.println("4 cur pos : " + tmpBuffer.position());
//					System.out.println("blockCnt : " + blockCnt );
//					
//					int curIndex = tmpBuffer.getInt();
//					System.out.println("5 cur pos : " + tmpBuffer.position());
//					System.out.println("curIndex : " + curIndex );
//					
//					debugStream.print(cnt);
//					debugStream.print("    ");
//					if(cnt==curIndex+1){
//					debugStream.println(curIndex+1);
//					}
//					else{
//						debugStream.print(curIndex+1);
//						debugStream.println("    notequal");
//					}
//					debugStream.flush();
//					
//					byte[] bytes = new byte[blockCnt];
//					System.out.println("0 bytes len : " + bytes.length );
//					
//					tmpBuffer.get(bytes, 0, blockCnt);
//					System.out.println("1 bytes len : " + bytes.length );
//					
//					bufferedOutputStream.write(bytes, 0, bytes.length);
//					bufferedOutputStream.flush();
//					
//					
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//
			}


//			// try {
//			// write(socket); //��д��client
//			// } catch (UnsupportedEncodingException ex) {
//			// Logger.getLogger(AioReadHandler.class.getName()).log(Level.SEVERE,
//			// null, ex);
//			// }

		} else if (i == -1) {
			try {
				System.out.println("�ͻ��˶���:" + socket.getRemoteAddress().toString());
				buf = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}


	private byte[] intToByteArray(int a) { 
	    return new byte[] {  
	        (byte) ((a >> 24) & 0xFF),  
	        (byte) ((a >> 16) & 0xFF),     
	        (byte) ((a >> 8) & 0xFF),     
	        (byte) (a & 0xFF)  
	    };  
	} 
	
	
	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		System.out.println("cancelled");

		/////////////////// �ر��ļ�
//		try {
//			bufferedOutputStream.flush();
//			bufferedOutputStream.close();
			bufferedOutputStream = null;
			fos = null;
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		/////////////////

	}

	// ����CompletionHandler�ķ���
	public void write(AsynchronousSocketChannel socket) throws UnsupportedEncodingException {
		String sendString = "��������Ӧ,���������:" + msg;
		// ByteBuffer
		// clientBuffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));
		ByteBuffer clientBuffer = ByteBuffer.wrap(sendString.getBytes(TransSetting.TRANSENCODE));
		socket.write(clientBuffer, clientBuffer, new AioWriteHandler(socket));
	}
}
