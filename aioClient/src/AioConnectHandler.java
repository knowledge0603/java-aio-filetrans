import java.util.concurrent.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioConnectHandler implements CompletionHandler
    <Void,AsynchronousSocketChannel>
{
    private Integer content = 0;
    
    public AioConnectHandler(Integer value){
        this.content = value;
    }
 
    @Override
    public void completed(Void attachment,AsynchronousSocketChannel connector) { 
        try {  
         connector.write(ByteBuffer.wrap(String.valueOf(content).getBytes())).get();  //����һ����Ϣ��int
         startRead(connector); 
        } catch (ExecutionException e) { 
            e.printStackTrace(); 
        } catch (InterruptedException ep) { 
            ep.printStackTrace(); 
        } 
    } 
 
    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) { 
        exc.printStackTrace(); 
    } 
    
    //�ⲻ�� CompletionHandler�ӿڵķ�����
    public void startRead(AsynchronousSocketChannel socket) { 
//        ByteBuffer clientBuffer = ByteBuffer.allocate(1024); 
        ByteBuffer clientBuffer = ByteBuffer.allocate(44+1024*1024); 
        //read��ԭ����
        //read(ByteBuffer dst, A attachment,
        //    CompletionHandler<Integer,? super A> handler) 
        //�����Ĳ�������������A�ͣ���ʵ�ʵ���read�ĵڶ�����������clientBuffer��
        // V���Ǵ���read����������Ĳ���
        socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket)); 
        try { 
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
 
}
