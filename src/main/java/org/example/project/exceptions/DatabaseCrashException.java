package org.example.project.exceptions;

public class DatabaseCrashException extends RuntimeException{

    private int index;

    public Databaspackage org.example.project.exceptions;

public class DatabaseCrashException extends RuntimeException{

    private int index;

    public DatabaseCrashException(Exception e, int index){
        super("database "+index+" crashed",e);
    }
}
