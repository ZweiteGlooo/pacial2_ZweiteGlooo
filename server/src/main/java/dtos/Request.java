package dtos;

import java.util.Map;

public class Request {
    public String action;
    public Map<String, Object> data;

    public Request() {}

    public Request(String action, Map<String, Object> data) {
        this.action = action;
        this.data = data;
    }
}
