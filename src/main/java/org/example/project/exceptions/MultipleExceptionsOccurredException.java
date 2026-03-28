package org.example.project.exceptions;

import java.util.List;

public class MultipleExceptionsOccurredException extends Exception {
    private List<Exception> exceptions;

    public MultipleExceptionsOccurredException(String message, List<Exception> exceptions) {
        super(message);
        this.exceptions = exceptions;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        for (Exception exception : exceptions) {
            exception.printStackTrace(); // 打印每个异常的堆栈信息
        }
    }
}
