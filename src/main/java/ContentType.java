public enum ContentType {
  TEXT_PLAIN("text/plain"),
  APPLICATION_OCTET_STREAM("application/octet-stream");

  private final String value;

  ContentType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}
