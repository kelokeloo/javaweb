package topics.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端：监听 8080 端口，接受一个客户端连接，
 * 打印客户端地址后关闭连接。
 */
public class Server {

    public static void main(String[] args) {
        // 1. 创建 ServerSocket 并监听 8080 端口（端口被占用会抛异常）
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("服务端已启动，监听端口 8080，等待客户端连接...");

            // 2. accept() 会阻塞，直到有客户端连上来，返回一个已连接的 Socket
            try (Socket socket = serverSocket.accept()) {
                // 3. 打印客户端地址（IP）和客户端本地端口
                System.out.println("客户端已连接：" + socket.getInetAddress() + ":" + socket.getPort());
            }
            // 4. 走到这里说明上面的 try 块结束，socket 已自动 close
            System.out.println("客户端连接已关闭，服务端退出");
        } catch (IOException e) {
            // 端口被占用、accept 异常等都会走到这里
            e.printStackTrace();
        }
    }
}
