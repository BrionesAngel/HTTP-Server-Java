import java.nio.charset.StandardCharsets;
import java.util.Map;

public record HttpResponse(
    int statusCode,
    String reason,
    Map<String, String> headers,
    String body) {
  public static HttpResponse ok(String body) {
    return new HttpResponse(
        200,
        "OK",
        Map.of(
            "Content-Type",
            "text/plain",
            "Content-Length",
            String.valueOf(body.length())),
        body);
  }

  public static HttpResponse notFound() {
    return new HttpResponse(404, "Not Found", Map.of(), "");
  }

  public byte[] toBytes() {
    StringBuilder sb = new StringBuilder();
    sb.append("HTTP/1.1")
        .append(' ')
        .append(statusCode)
        .append(' ')
        .append(reason)
        .append("\r\n");
    headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\r\n"));
    sb.append("\r\n").append(body);
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }
}
