package topics.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 客户端：连接本机 8080 端口的服务端，从控制台输入一行发给服务端，然后关闭连接。
 */
public class Client {

    public static void main(String[] args) {
        // 连接本机（127.0.0.1）8080 端口的服务端，成功就返回一个已连接的 Socket
        // PrintWriter 负责把文字写入输出流；autoFlush=true 表示 println 后立即冲刷(flush)
        try (Socket socket = new Socket("127.0.0.1", 8080);
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             // 控制台输入也是一个"流"！System.in 是 InputStream，套上
             // InputStreamReader + BufferedReader 就能用 readLine() 按行读
             BufferedReader console = new BufferedReader(
                     new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.println("已连接到服务端：" + socket.getInetAddress() + ":" + socket.getPort());
            System.out.println("请输入一行消息，回车发送：");

            // 从控制台读一行（阻塞等待你输入 + 回车）
            String line = console.readLine();

            // 用 PrintWriter 发给服务端（println 自动补换行，服务端 readLine() 才能读到）
            out.println(line);
            System.out.println("已发送：" + line);
        } catch (IOException e) {
            // 服务端没启动、端口不对等都会在这里报错
            System.out.println("连接失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
