package org.example.taskservice.exception;

import java.util.UUID;

public class TaskForEmployeeNotFoundException extends RuntimeException{
    public TaskForEmployeeNotFoundException(UUID id) {
        super("Could not find task for employee with id: " + id);
    }
}
