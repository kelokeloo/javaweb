package topics.socket;

import java.io.IOException;
import java.net.Socket;

/**
 * 客户端：连接本机 8080 端口的服务端，连上即打印信息，然后关闭连接。
 */
public class Client {

    public static void main(String[] args) {
        // 连接本机（127.0.0.1）8080 端口的服务端，成功就返回一个已连接的 Socket
        try (Socket socket = new Socket("127.0.0.1", 8080)) {
            System.out.println("已连接到服务端：" + socket.getInetAddress() + ":" + socket.getPort());
        } catch (IOException e) {
            // 服务端没启动、端口不对等都会在这里报错
            System.out.println("连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
