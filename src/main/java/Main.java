import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // TODO: Uncomment the code below to pass the first stage

    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      Socket socket = serverSocket.accept(); // Wait for connection from client.
      System.out.println("socket: " + socket);

      OutputStream outputStream = socket.getOutputStream();
      InputStream inputStream = socket.getInputStream();

      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      byte[] chunk = new byte[1024];
      int bytesRead;
      boolean headersComplete = false;
      while (!headersComplete && (bytesRead = inputStream.read(chunk)) != -1) {
        buffer.write(chunk, 0, bytesRead);

        byte[] data = buffer.toByteArray();
        for (int i = 0; i + 3 < data.length; i++) {
          if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
            headersComplete = true;
            break;
          }
        }
      }

      String request = buffer.toString(StandardCharsets.UTF_8);
      System.out.println("request: " + request);

      String[] parts = request.split(" ");
      System.out.println("request parts: " + Arrays.toString(parts));

      String path = parts[1];
      System.out.println("path: " + path);

      if (path.equals("/")) {
        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        outputStream.flush();
      } else {
        outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        outputStream.flush();

      }
      System.out.println("accepted new connection");
    } catch (

    IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
