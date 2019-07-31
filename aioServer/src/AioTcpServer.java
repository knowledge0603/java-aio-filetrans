import java.net.InetSocketAddress; 
import java.nio.channels.AsynchronousChannelGroup; 
import java.nio.channels.AsynchronousServerSocketChannel; 
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
/**
 *     AIO�첽socketͨѶ���ֳ� ���ڷ���˵�socekt�����ڿͻ��˵�socket����Ȼ�����߶���<br>
 * �첽�ġ�����ʹ��ʱ�����õ���ͬ�����첽ͨ�����������첽ͨ��������ͨ���̳߳ع���<br>
 *    �첽ͨ�����������������ɷ����socket��ͻ���socket�� * 
 *    ʹ�÷����socket��ͻ���socket����Ҫһ��������������CompletionHandler����<br>
 *������Ϣʱ�첽ͨ����������� �����Ϣ���ݸ��������������� * 
 *    �����������ķ�����ͬһ�������������Ĳ����Ƿ��ͣ����ŵ������ķ�����ͬ���ı䡣<br> * 
 *    ��AIO�У�CompletionHandler��������������������Ǹ����ͽӿڣ����ص������á�<br>
 * ʹ��CompletionHandler�ķ�����Լ���ǰѸ÷���ǰһ������ʵ�����ݸ�A�Ͳ���<br>
 * ��attachment����CompletionHandler����һ���������Ǵ��и÷�����ʹ�������ʵ����
 * 
 */
public class AioTcpServer implements Runnable { 
    private AsynchronousChannelGroup asyncChannelGroup;  
    private AsynchronousServerSocketChannel listener;  
  
    public AioTcpServer(int port) throws Exception { 
        //�����̳߳�
        ExecutorService executor = Executors.newFixedThreadPool(20); //20���߳�?
        //�첽ͨ��������
        asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);  //�������������20���߳�
        //���� ���ڷ���˵��첽Socket.���¼�Ʒ�����socket��
        //�첽ͨ������������ѷ�������õ�����ز���
        listener = AsynchronousServerSocketChannel.open(asyncChannelGroup).
                bind(new InetSocketAddress(port)); //��
    } 
 
    public void run() { 
        try { 

//            AioAcceptHandler acceptHandler = new AioAcceptHandler();
            //Ϊ�����socketָ�����ղ�������.acceptԭ���ǣ�
            //accept(A attachment, CompletionHandler<AsynchronousSocketChannel,
            // ? super A> handler)
            //Ҳ���������CompletionHandler��A�Ͳ�����ʵ�ʵ���accept�����ĵ�һ������
            //����listener����һ������V������ԭ���еĿͻ���socket
            listener.accept(listener, new AioAcceptHandler()); //��ʼ����
            Thread.sleep(400000); //400��?
        } catch (Exception e) { 
            e.printStackTrace(); 
        } finally { 
            System.out.println("finished server");
        } 
    } 
 
    public static void main(String... args) throws Exception { 
        AioTcpServer server = new AioTcpServer(1009); //���ö˿�
        new Thread(server).start(); //�����߳�
    } 
}

