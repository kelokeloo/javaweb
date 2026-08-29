package topics.socket.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * 最简单的 HTTP 服务器：
 * 1. 监听 8082，接受浏览器连接
 * 2. 读取请求头（读到空行 \r\n\r\n 为止）
 * 3. 读取 hello.html 文件，拼接 HTTP 响应头 + 正文
 * 4. 返回给浏览器，关闭连接
 */
public class HttpServer {

    public static void main(String[] args) {
        // 页面文件：和本类同目录下的 hello.html（用 Path 定位）
        Path htmlFile = Path.of("src", "main", "java", "topics", "socket", "http", "hello.html");

        try (ServerSocket serverSocket = new ServerSocket(8082)) {
            System.out.println("HTTP 服务器已启动：http://localhost:8082");
            System.out.println("等待浏览器连接...");

            // 循环接客：accept() 阻塞等一个连接，处理完再等下一个
            while (true) {
                try (Socket socket = serverSocket.accept();
                     InputStream in = socket.getInputStream();
                     OutputStream out = socket.getOutputStream()) {

                    System.out.println("收到连接：" + socket.getInetAddress() + ":" + socket.getPort());

                    // ---- 1. 读请求头（读到空行 \r\n\r\n 为止，简单请求无 body）----
                    StringBuilder requestHead = new StringBuilder();
                    int c;
                    while ((c = in.read()) != -1) {
                        requestHead.append((char) c);
                        if (requestHead.toString().endsWith("\r\n\r\n")) {
                            break;
                        }
                    }
                    // 打印请求头（第一行是请求行，如 GET / HTTP/1.1）
                    System.out.println("----- 请求头 -----");
                    System.out.println(requestHead);
                    System.out.println("------------------");

                    // ---- 2. 读取 HTML 文件，得到正文的字节 ----
                    byte[] body = Files.readAllBytes(htmlFile);

                    // ---- 3. 拼接响应头（用 Content-Length 告诉浏览器正文长度）----
                    // 响应头以 \r\n 结尾，正文前必须有一个空行 \r\n\r\n 分隔头和正文
                    String header = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html; charset=utf-8\r\n"
                            + "Content-Length: " + body.length + "\r\n"
                            + "\r\n";
                    byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

                    // ---- 4. 把"响应头 + 正文"整体写回浏览器 ----
                    out.write(headerBytes);
                    out.write(body);
                    out.flush();
                    System.out.println("已返回页面，" + body.length + " 字节");
                } catch (IOException e) {
                    // 单个连接出错不影响整个服务器，打印后继续等下一个连接
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
