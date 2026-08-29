package topics.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 服务端：监听 8080 端口，接受一个客户端连接，
 * 读取客户端发来的一行内容并打印，然后关闭连接。
 */
public class Server {

    public static void main(String[] args) {
        // 1. 创建 ServerSocket 并监听 8080 端口（端口被占用会抛异常）
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("服务端已启动，监听端口 8080，等待客户端连接...");

            // 2. accept() 会阻塞，直到有客户端连上来，返回一个已连接的 Socket
            //    BufferedReader 负责按"行"读取客户端发来的内容
            try (Socket socket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

                // 3. 打印客户端地址（IP）和客户端本地端口
                System.out.println("客户端已连接：" + socket.getInetAddress() + ":" + socket.getPort());

                // 4. readLine() 阻塞，直到收到一行（以换行结尾）或连接关闭
                String line = in.readLine();
                System.out.println("收到客户端消息：" + line);
            }
            // 5. 走到这里说明上面的 try 块结束，in 和 socket 已自动 close
            System.out.println("客户端连接已关闭，服务端退出");
        } catch (IOException e) {
            // 端口被占用、accept 异常等都会走到这里
            e.printStackTrace();
        }
    }
}
