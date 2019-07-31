import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class WriteBlockThread implements Runnable {

	private ArrayBlockingQueue<ByteBuffer> blockQueue;

	public WriteBlockThread(int size) {
		blockQueue = new ArrayBlockingQueue<ByteBuffer>(size);

	}

	public void addBlock(ByteBuffer byteBuffer) {

//		synchronized (this) 
		{
			try {
				sem.acquire();
			blockQueue.add(byteBuffer);
			sem.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Semaphore sem = new Semaphore(1);
	private boolean first = true;
	private FileOutputStream fos = null;
	private BufferedOutputStream bufferedOutputStream = null;

	private FileOutputStream debugos = null;
	// private BufferedOutputStream debugStream = null;
	private PrintStream debugStream = null;

	private CharsetDecoder decoder = Charset.forName(TransSetting.TRANSENCODE).newDecoder();

	private int cnt = 0;

	@Override
	public void run() {

		ByteBuffer tmpBuffer;
		while (true) {

			tmpBuffer = null;
//			synchronized (this)
			{
				if (!blockQueue.isEmpty()) {
					try {
						sem.acquire();
						tmpBuffer = blockQueue.poll();
						sem.release();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} // 队列里有数
			} // 同步
			if (tmpBuffer != null)
			// if (msg.length() > 12)
			{ // 28
				// if (bytes.length == 1024 * 1024 + 40) { //40
				// System.out.println("read cnt : " + msg);

				if (bufferedOutputStream == null) {
					System.out.println("创建文件");
					// first = false;
					File file = new File(System.getProperty("user.dir") + File.separator + "test"
							+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
					if (!file.exists()) {
						try {
							file.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					File debugfile = new File(System.getProperty("user.dir") + File.separator + "debug"
							+ new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");
					if (!debugfile.exists()) {
						try {
							debugfile.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					try {
						FileOutputStream fos = new FileOutputStream(file);
						bufferedOutputStream = new BufferedOutputStream(fos);
						// 存入hash表
						// ctxMap.put(key, bufferedOutputStream);

						debugos = new FileOutputStream(debugfile);
						// debugStream = new
						// BufferedOutputStream(debugos);
						debugStream = new PrintStream(debugos);

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}

				try {
					cnt++;
//					System.out.println("cur cnt : " + cnt);
					// 转换读取
					// byte[] wbyte =
					// msg.getBytes(TransSetting.TRANSENCODE);

					tmpBuffer.position(0); // 必须重设位置

					// 读头部
//					System.out.println("0 cur pos : " + tmpBuffer.position());
					int fileNameLen = tmpBuffer.getInt();
//					System.out.println("file name len : " + fileNameLen);
//					System.out.println("1 cur pos : " + tmpBuffer.position());

					// byte[] fileNameByte = new byte[fileNameLen];
					// tmpBuffer.get(fileNameByte, 0, fileNameLen);
					//
					//// String fileNameStr = new String(fileNameByte,
					// "GB2312"); //"GB2312"
					// System.out.println("2 cur pos : " +
					// tmpBuffer.position());
					// long fileLen = tmpBuffer.getLong(); //文件长
					// System.out.println("fileLen : " + fileLen);
					//
					// System.out.println("3 cur pos : " +
					// tmpBuffer.position());

					tmpBuffer.position(12 + fileNameLen); // 只读取blockcnt和index

					int blockCnt = tmpBuffer.getInt();
//					System.out.println("4 cur pos : " + tmpBuffer.position());
//					System.out.println("blockCnt : " + blockCnt);

					int curIndex = tmpBuffer.getInt();
//					System.out.println("5 cur pos : " + tmpBuffer.position());
//					System.out.println("curIndex : " + curIndex);

					debugStream.print(cnt);
					debugStream.print("    ");
					if (cnt == curIndex + 1) {
						debugStream.println(curIndex + 1);
					} else {
						debugStream.print(curIndex + 1);
						debugStream.println("    notequal");
						System.out.println("cur cnt : " + cnt);
					    System.out.println("curIndex : " + curIndex);
						
					}
					debugStream.flush();

					byte[] bytes = new byte[blockCnt];
//					System.out.println("0 bytes len : " + bytes.length);

					tmpBuffer.get(bytes, 0, blockCnt);
//					System.out.println("1 bytes len : " + bytes.length);

					bufferedOutputStream.write(bytes, 0, bytes.length);
					bufferedOutputStream.flush();

					if (curIndex == 7254) { // 4146  //2561  //7254
						// 最后一个，线程中要手动关闭
						debugStream.close();
						bufferedOutputStream.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			} // 可写入

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // while
	}

}
