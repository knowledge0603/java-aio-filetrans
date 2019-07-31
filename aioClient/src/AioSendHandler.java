import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioSendHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel socket;

	private AioTcpClient aioTcpClient;

	public AioSendHandler(AsynchronousSocketChannel socket) {
		this.socket = socket;
	}

	public AioSendHandler(AsynchronousSocketChannel socket, AioTcpClient taioTcpClient) {
		this.socket = socket;
		this.aioTcpClient = taioTcpClient;
	}

	@Override
	public void completed(Integer i, ByteBuffer buf) {

		// 发送端的发送index加一
		aioTcpClient.incCurBlockIndex();

		if (i > 0) {
			// socket.write(buf, buf, this); //没什么效果
			// System.out.println("client send");

			// 表示已经传送到了server端?
			long completeTime = System.currentTimeMillis();
			System.out.println("完成时间 : " + completeTime);

			// 可以在此递归
			try {
				if (!aioTcpClient.isSenFinished()) {
					
					System.out.println("传送完块索引 : " + aioTcpClient.curBlockIndex);
					
					Thread.sleep(140); // 130睡眠
					// aioTcpClient.send(); // 递归调用 //结束标志
					aioTcpClient.sendBlock(); // 发送文件块
				} else {
					System.out.println("file trans Complete !!!");
				}

			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			// catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
			// }

		} else if (i == -1) {
			
			//CurBlockIndex是否减一后重发?
			aioTcpClient.decCurBlockIndex();
			//TODO //重发
			
			try {
				System.out.println("对端断线:" + socket.getRemoteAddress().toString());
				buf = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		System.out.println("cancelled");
	}

}
