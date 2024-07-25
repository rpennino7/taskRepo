package org.example.taskservice.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException{
    public TaskNotFoundException(UUID id) {
        super("Could not find task with id: " + id);
    }
}
