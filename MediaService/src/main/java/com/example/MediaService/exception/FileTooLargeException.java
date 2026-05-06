package com.example.MediaService.exception;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException() {
        super("File exceeds the 2MB limit");
    }
}
