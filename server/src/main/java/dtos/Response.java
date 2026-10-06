package dtos;

public class Response {
    public String status; // "ok" o "error"
    public Object data;

    public Response() {}

    public Response(String status, Object data) {
        this.status = status;
        this.data = data;
    }

    public static Response ok(Object data) {
        return new Response("ok", data);
    }

    public static Response error(String message) {
        return new Response("error", java.util.Map.of("message", message));
    }
}
