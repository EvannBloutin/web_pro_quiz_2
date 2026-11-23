package com.campuslibrary.util;

/** Business-rule violation whose message can be shown to the user. */
public class ServiceException extends Exception {
    public ServiceException(String message) { super(message); }
}
