package org.example.taskservice.controller;

import org.example.taskservice.exception.TaskForEmployeeNotFoundException;
import org.example.taskservice.exception.TaskForProjectNotFoundException;
import org.example.taskservice.exception.TaskNotFoundException;
import org.example.taskservice.model.Task;
import org.example.taskservice.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        logger.info("Request to get all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable UUID id) {
        logger.info("Request to get a tasks with id {}", id);
        try {
            Task task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (TaskNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        logger.info("Request to create a new task");
        try {
            Task createdTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        }
        catch (IllegalArgumentException e) {
            logger.error("Invalid task data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable UUID id, @RequestBody Task taskDetails) {
        logger.info("Request to update the task with id {}", id);
        try {
            Task updatedTask = taskService.updateTask(id, taskDetails);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (TaskNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while updating the task");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID id) {
        logger.info("Request to delete the task with id {}", id);
        try {
            taskService.deleteTask(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (TaskNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/by-project/{projectId}")
    public ResponseEntity<?> getTasksByProjectId(@PathVariable UUID projectId) {
        logger.info("Request to get task by project with id {}", projectId);
        try {
            List<Task> tasks = taskService.getTasksByProjectId(projectId);
            return ResponseEntity.ok(tasks);
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (TaskForProjectNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<?> getTaskByEmployeeId(@PathVariable UUID employeeId) {
        logger.info("Request to get task by employee with id {}", employeeId);
        try {
            Task task = taskService.getTaskByEmployeeId(employeeId);
            return ResponseEntity.ok(task);
        } catch (TaskForEmployeeNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/employee")
    public ResponseEntity<?> assignEmployeeToTask(@PathVariable UUID id,  @RequestBody Map<String, String> request) {
        logger.info("Request to assign employee to task with id {}", id);
        try {
            UUID employeeId = UUID.fromString(request.get("employeeId"));
            Task updatedTask = taskService.assignEmployeeToTask(id, employeeId);
            return ResponseEntity.ok(updatedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (TaskNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}/employee")
    public ResponseEntity<?> removeEmployeeFromTask(@PathVariable UUID id) {
        logger.info("Request to remove employee from task with id {}", id);
        try {
            taskService.removeEmployeeFromTask(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (TaskNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

}