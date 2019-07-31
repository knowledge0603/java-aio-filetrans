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

		// ���Ͷ˵ķ���index��һ
		aioTcpClient.incCurBlockIndex();

		if (i > 0) {
			// socket.write(buf, buf, this); //ûʲôЧ��
			// System.out.println("client send");

			// ��ʾ�Ѿ����͵���server��?
			long completeTime = System.currentTimeMillis();
			System.out.println("���ʱ�� : " + completeTime);

			// �����ڴ˵ݹ�
			try {
				if (!aioTcpClient.isSenFinished()) {
					
					System.out.println("����������� : " + aioTcpClient.curBlockIndex);
					
					Thread.sleep(140); // 130˯��
					// aioTcpClient.send(); // �ݹ���� //������־
					aioTcpClient.sendBlock(); // �����ļ���
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
			
			//CurBlockIndex�Ƿ��һ���ط�?
			aioTcpClient.decCurBlockIndex();
			//TODO //�ط�
			
			try {
				System.out.println("�Զ˶���:" + socket.getRemoteAddress().toString());
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
