package org.example.taskservice.service;

import org.example.taskservice.exception.TaskForEmployeeNotFoundException;
import org.example.taskservice.exception.TaskForProjectNotFoundException;
import org.example.taskservice.exception.TaskNotFoundException;
import org.example.taskservice.model.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {


    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(UUID id, Task taskDetails) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        return taskRepository.findById(id).map(task -> {
            task.setName(taskDetails.getName());
            task.setDescription(taskDetails.getDescription());
            task.setStatus(taskDetails.getStatus());
            task.setProjectId(taskDetails.getProjectId());
            task.setEmployeeId(taskDetails.getEmployeeId());
            return taskRepository.save(task);
        }).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void deleteTask(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
    }

    public Task assignEmployeeToTask(UUID id, UUID employeeId) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        } else if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        return taskRepository.findById(id).map(task -> {
            task.setEmployeeId(employeeId);
            return taskRepository.save(task);
        }).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void removeEmployeeFromTask(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        task.setEmployeeId(null);
        taskRepository.save(task);
    }

    public List<Task> getTasksByProjectId(UUID projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        } else if (tasks.isEmpty()) {
            throw new TaskForProjectNotFoundException(projectId);
        }
        return taskRepository.findByProjectId(projectId);
    }

    public Task getTaskByEmployeeId(UUID employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        return taskRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new TaskForEmployeeNotFoundException(employeeId));
    }

}