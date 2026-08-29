package topics.socket.filetransfer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/**
 * 文件传输服务端：客户端连上来后，把本地文件用"文件流 → 网络流"搬运给客户端，
 * 搬完关闭连接（关闭 = 告诉客户端"文件结束"）。
 *
 * 核心：流与流的搬运 —— 从文件流读进 byte[] 缓冲区，再从缓冲区写到网络流。
 */
public class FileTransferServer {

    public static void main(String[] args) {
        // 源文件：和本类同目录下的 sample.txt（用 Path 定位，避免写死绝对路径）
        Path sourceFile = Path.of("src", "main", "java", "topics", "socket", "filetransfer", "sample.txt");

        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("文件服务端已启动，监听 8081，等待连接...");

            try (Socket socket = serverSocket.accept();
                 // 数据源：从文件读（字节流）
                 InputStream fileIn = new FileInputStream(sourceFile.toFile());
                 // 目标：写到网络（字节流）
                 OutputStream socketOut = socket.getOutputStream()) {

                System.out.println("客户端已连接：" + socket.getInetAddress() + ":" + socket.getPort());

                // 搬运循环：卡车 = byte[8192]，从文件流搬到网络流
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fileIn.read(buffer)) != -1) {
                    socketOut.write(buffer, 0, len);
                }
                socketOut.flush(); // 冲干净缓冲
                System.out.println("文件已发送完毕");
            }
            // socketOut 和 fileIn 自动 close（close socketOut 会让客户端读到 -1）
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
