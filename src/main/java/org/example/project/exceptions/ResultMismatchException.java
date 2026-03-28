package org.example.project.exceptions;

public class ResultMismatchException extends RuntimeException{

    private int index;

    public ResultMismatchException(String msg){
        super(msg);
    }
}
