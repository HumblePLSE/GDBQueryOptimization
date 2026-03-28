package org.example.project;

public interface projectDBConnection extends AutoCloseable {

    String getDatabaseVersion() throws Exception;
}
