package com.example.MediaService.exception;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String detected) {
        super("Unsupported file type: " + detected);
    }
}
