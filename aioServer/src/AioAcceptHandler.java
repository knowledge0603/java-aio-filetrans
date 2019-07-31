
import java.io.IOException; 
import java.nio.ByteBuffer; 
import java.nio.channels.AsynchronousServerSocketChannel; 
import java.nio.channels.AsynchronousSocketChannel; 
import java.nio.channels.CompletionHandler; 
 
//����Ĳ�����ʵ�ʵ������ĺ��������������Ƿ����socket.accetp���þ���
public class AioAcceptHandler implements CompletionHandler
        <AsynchronousSocketChannel, AsynchronousServerSocketChannel > 
{ 
//    private  AsynchronousSocketChannel socket;
    @Override
    public void completed(AsynchronousSocketChannel socket, 
        AsynchronousServerSocketChannel attachment) 
    { //ע���һ���ǿͻ���socket���ڶ����Ƿ�����socket
        try { 
            System.out.println("AioAcceptHandler.completed called");
            attachment.accept(attachment, this); 
            System.out.println("�пͻ�������:" +
                socket.getRemoteAddress().toString()
            );
            //����ʱ���жϵ�ǰ������������ȴ������߽�ÿ�η��͵����ݳ��ȼ���
            startRead(socket); //��һ����Ϣ   
        } catch (IOException e) { 
            e.printStackTrace(); 
        } 
    } 
 
    @Override
    public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) 
    { 
        exc.printStackTrace(); 
    } 
    
    //����CompletionHandler�ķ���
    public void startRead(AsynchronousSocketChannel socket) { 
//        ByteBuffer clientBuffer = ByteBuffer.allocate(1024); 
        ByteBuffer clientBuffer = ByteBuffer.allocate(20+46+1024*1024); //20Ϊͷ�Ĺ̶�����  //82Ϊ�ļ�������
        //read��ԭ����
        //read(ByteBuffer dst, A attachment,
        //    CompletionHandler<Integer,? super A> handler) 
        //�����Ĳ�������������A�ͣ���ʵ�ʵ���read�ĵڶ�����������clientBuffer��
        // V���Ǵ���read����������Ĳ���
        AioReadHandler rd=new AioReadHandler(socket);
        socket.read(clientBuffer, clientBuffer, rd); 
        try {             
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 
 
}
