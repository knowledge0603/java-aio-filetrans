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
         connector.write(ByteBuffer.wrap(String.valueOf(content).getBytes())).get();  //发送一个消息，int
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
    
    //这不是 CompletionHandler接口的方法。
    public void startRead(AsynchronousSocketChannel socket) { 
//        ByteBuffer clientBuffer = ByteBuffer.allocate(1024); 
        ByteBuffer clientBuffer = ByteBuffer.allocate(44+1024*1024); 
        //read的原型是
        //read(ByteBuffer dst, A attachment,
        //    CompletionHandler<Integer,? super A> handler) 
        //即它的操作处理器，的A型，是实际调用read的第二个参数，即clientBuffer。
        // V型是存有read的连接情况的参数
        socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket)); 
        try { 
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
 
}
