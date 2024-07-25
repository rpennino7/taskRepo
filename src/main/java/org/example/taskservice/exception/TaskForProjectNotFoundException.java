package org.example.taskservice.exception;

import java.util.UUID;

public class TaskForProjectNotFoundException extends RuntimeException{
    public TaskForProjectNotFoundException(UUID id) {
        super("Could not find any task for project with id: " + id);
    }
}