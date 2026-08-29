package topics.socket.filetransfer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;

/**
 * 文件传输客户端：连接服务端，把网络流读到本地文件（网络流 → 文件流）。
 * 读到 -1（服务端关闭连接）表示文件结束。
 */
public class FileTransferClient {

    public static void main(String[] args) {
        // 保存位置：项目根下的 received.txt（模拟"客户端保存到他的文件"）
        Path targetFile = Path.of("received.txt");

        try (Socket socket = new Socket("127.0.0.1", 8081);
             // 数据源：从网络读（字节流）
             InputStream socketIn = socket.getInputStream();
             // 目标：写到本地文件（字节流）
             OutputStream fileOut = new FileOutputStream(targetFile.toFile())) {

            System.out.println("已连接到文件服务端：" + socket.getInetAddress() + ":" + socket.getPort());

            // 搬运循环：从网络流读进卡车，写到文件流
            byte[] buffer = new byte[8192];
            int len;
            int total = 0;
            while ((len = socketIn.read(buffer)) != -1) {   // 服务端关连接 → 这里读到 -1
                fileOut.write(buffer, 0, len);
                total += len;
            }
            System.out.println("接收完成，共 " + total + " 字节，已保存到 " + targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
