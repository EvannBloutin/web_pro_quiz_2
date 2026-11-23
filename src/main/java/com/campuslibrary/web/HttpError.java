package com.campuslibrary.web;

/** Thrown by controllers to produce an HTTP error page (403, 404...). */
public class HttpError extends RuntimeException {
    private final int status;
    public HttpError(int status, String message) { super(message); this.status = status; }
    public int getStatus() { return status; }
}
