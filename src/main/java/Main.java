import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here! \n");

    try {
      ServerSocket serverSocket = new ServerSocket(4221);

      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket socket = serverSocket.accept(); // Wait for connection from client.
        System.out.println("socket: " + socket);

        Thread.startVirtualThread(() -> handleRequest(socket));
        System.out.println("accepted new connection");
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }

  }

  private static void handleRequest(Socket socket) {
    try (socket) {
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

      String[] parts = request.split("\r\n");
      System.out.println("\n--- REQUEST ---");
      System.out.println("request parts size: " + parts.length);
      System.out.println(Arrays.toString(parts));

      String[] method = parts[0].split(" ");

      String met = method[0];
      System.out.println("method: " + met);

      String path = method[1];
      System.out.println("path: " + path);

      String httpVersion = method[2];
      System.out.println("httpVersion: " + httpVersion);

      String[] host = new String[2];
      String[] accept = new String[2];
      String[] userAgent = new String[2];

      for (int i = 1; i < parts.length; i++) {
        if (parts[i].startsWith("Host:")) {
          host = parts[i].split(": ", 2);
          System.out.println(Arrays.toString(host));
        }
        if (parts[i].startsWith("Accept:")) {
          accept = parts[i].split(": ", 2);
          System.out.println(Arrays.toString(accept));
        }
        if (parts[i].startsWith("User-Agent:")) {
          userAgent = parts[i].split(": ", 2);
          System.out.println(Arrays.toString(userAgent));
        }
      }
      System.out.println("--- --- ---\n");

      HttpResponse response;

      if (path.equals("/")) {
        response = HttpResponse.ok("");
      } else if (path.startsWith("/echo/")) {
        String body = path.substring(6);
        response = HttpResponse.ok(ContentType.TEXT_PLAIN, body);
      } else if (path.startsWith("/user-agent")) {
        String body = userAgent[1];
        response = HttpResponse.ok(ContentType.TEXT_PLAIN, body);
      } else if (path.startsWith("/files")) {
        String directory = "/tmp/";
        String fileName = path.substring(6);
        try {
          String content = Files.readString(Path.of(directory, fileName));
          response = HttpResponse.ok(ContentType.APPLICATION_OCTET_STREAM, content);
        } catch (NoSuchFileException e) {
          response = HttpResponse.notFound();
        }

      } else {
        response = HttpResponse.notFound();
      }

      outputStream.write(response.toBytes());
      outputStream.flush();

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }

  }

}
