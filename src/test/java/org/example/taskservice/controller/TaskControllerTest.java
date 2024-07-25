package org.example.taskservice.controller;
import org.example.taskservice.exception.TaskForEmployeeNotFoundException;
import org.example.taskservice.exception.TaskForProjectNotFoundException;
import org.example.taskservice.exception.TaskNotFoundException;
import org.example.taskservice.model.Task;
import org.example.taskservice.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

    public class TaskControllerTest {

        @Mock
        private TaskService taskService;

        @InjectMocks
        private TaskController taskController;

        private UUID taskId1, taskId2, projectId, employeeId;
        private Task task1, task2;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);

            // Inizializzazione dei mock e creazione di esempi di task
            taskId1 = UUID.randomUUID();
            taskId2 = UUID.randomUUID();
            projectId = UUID.randomUUID();
            employeeId = UUID.randomUUID();
            task1 = new Task(taskId1, "Task 1", "Descrizione Task 1", "New", projectId, null);
            task2 = new Task(taskId2, "Task 2", "Descrizione Task 2", "In Progress", projectId, null);

            List<Task> allTasks = Arrays.asList(task1, task2);
            when(taskService.getAllTasks()).thenReturn(allTasks);
            when(taskService.getTaskById(taskId1)).thenReturn(task1);
            when(taskService.getTaskById(taskId2)).thenReturn(task2);
            when(taskService.createTask(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(taskService.updateTask(eq(taskId1), any(Task.class))).thenReturn(task1);
            when(taskService.updateTask(eq(taskId2), any(Task.class))).thenReturn(task2);
            when(taskService.getTasksByProjectId(projectId)).thenReturn(allTasks);
            when(taskService.getTaskByEmployeeId(employeeId)).thenReturn(task1);
            when(taskService.updateTask(eq(taskId1), any(Task.class))).thenAnswer(invocation -> invocation.getArgument(1));
            when(taskService.updateTask(isNull(), any(Task.class)))
                    .thenThrow(new IllegalArgumentException("Task ID cannot be null"));
            doThrow(new IllegalArgumentException("Task ID cannot be null"))
                    .when(taskService).deleteTask(null);
            doThrow(new IllegalArgumentException("Task ID cannot be null"))
                    .when(taskService).getTaskById(null);
        }
        @Test
        void getAllTasks_Success() {
            ResponseEntity<List<Task>> response = taskController.getAllTasks();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, response.getBody().size());
            assertTrue(response.getBody().contains(task1));
            assertTrue(response.getBody().contains(task2));
        }

        @Test
        void getTaskById_Success() {
            ResponseEntity<?> response = taskController.getTaskById(taskId1);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(task1, response.getBody());
        }

        @Test
        void getTaskById_NotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(taskService.getTaskById(nonExistentId)).thenThrow(new TaskNotFoundException(nonExistentId));

            ResponseEntity<?> response = taskController.getTaskById(nonExistentId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task with id: "+nonExistentId, response.getBody());
        }

        @Test
        void getTaskById_NullId() {
            // Act
            ResponseEntity<?> response = taskController.getTaskById(null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Task ID cannot be null", response.getBody());
        }

        @Test
        void createTask_Success() {
            Task newTask = new Task(null, "New Task", "Descrizione New Task", "New", projectId, null);
            ResponseEntity<Task> response = taskController.createTask(newTask);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(newTask, response.getBody());
        }

        @Test
        void createTask_WithInvalidData_ShouldFail() {
            // Arrange
            Task invalidTask = new Task(null, "", "Description", "New", projectId, null); // Nome vuoto
            when(taskService.createTask(any(Task.class))).thenThrow(new IllegalArgumentException("Task name cannot be empty"));

            // Act
            ResponseEntity<Task> response = taskController.createTask(invalidTask);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        void updateTask_Success() {
            Task updatedTask = new Task(taskId1, "Updated Task 1", "Descrizione Aggiornata", "In Progress", projectId, null);
            ResponseEntity<?> response = taskController.updateTask(taskId1, updatedTask);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(updatedTask, response.getBody());
        }

        @Test
        void updateTask_NotFound() {
            UUID nonExistentId = UUID.randomUUID();
            Task updatedTask = new Task(nonExistentId, "Updated Task", "Descrizione", "New", projectId, null);
            when(taskService.updateTask(eq(nonExistentId), any(Task.class))).thenThrow(new TaskNotFoundException(nonExistentId));

            ResponseEntity<?> response = taskController.updateTask(nonExistentId, updatedTask);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task with id: "+nonExistentId, response.getBody());
        }
        @Test
        void updateTask_NullId() {
            Task updatedTask = new Task(null, "Updated Task", "Description", "In Progress", projectId, null);

            ResponseEntity<?> response = taskController.updateTask(null, updatedTask);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Task ID cannot be null", response.getBody());
        }
        @Test
        void deleteTask_Success() {
            ResponseEntity<?> response = taskController.deleteTask(taskId1);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(taskService).deleteTask(taskId1);
        }

        @Test
        void deleteTask_NotFound() {
            UUID nonExistentId = UUID.randomUUID();
            doThrow(new TaskNotFoundException(nonExistentId)).when(taskService).deleteTask(nonExistentId);

            ResponseEntity<?> response = taskController.deleteTask(nonExistentId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task with id: "+nonExistentId, response.getBody());
        }
        @Test
        void deleteTask_NullId() {

            // Act
            ResponseEntity<?> response = taskController.deleteTask(null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Task ID cannot be null", response.getBody());
        }
        @Test
        void getTasksByProjectId_Success() {
            ResponseEntity<?> response = taskController.getTasksByProjectId(projectId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            List<Task> tasks = (List<Task>) response.getBody();
            assertEquals(2, tasks.size());
            assertTrue(tasks.contains(task1));
            assertTrue(tasks.contains(task2));
        }

        @Test
        void getTasksByProjectId_NotFound() {
            UUID nonExistentProjectId = UUID.randomUUID();
            when(taskService.getTasksByProjectId(nonExistentProjectId)).thenThrow(new TaskForProjectNotFoundException(nonExistentProjectId));

            ResponseEntity<?> response = taskController.getTasksByProjectId(nonExistentProjectId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find any task for project with id: "+nonExistentProjectId, response.getBody());
        }
        @Test
        void getTasksByProjectId_NullId() {
            when(taskService.getTasksByProjectId(null)).thenThrow(new IllegalArgumentException("Project ID cannot be null"));

            // Act
            ResponseEntity<?> response = taskController.getTasksByProjectId(null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Project ID cannot be null", response.getBody());
        }
        @Test
        void getTaskByEmployeeId_Success() {
            ResponseEntity<?> response = taskController.getTaskByEmployeeId(employeeId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(task1, response.getBody());
        }

        @Test
        void getTaskByEmployeeId_NotFound() {
            UUID nonExistentEmployeeId = UUID.randomUUID();
            when(taskService.getTaskByEmployeeId(nonExistentEmployeeId)).thenThrow(new TaskForEmployeeNotFoundException(nonExistentEmployeeId));

            ResponseEntity<?> response = taskController.getTaskByEmployeeId(nonExistentEmployeeId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task for employee with id: "+nonExistentEmployeeId, response.getBody());
        }

        @Test
        void assignEmployeeToTask_Success() {
            UUID newEmployeeId = UUID.randomUUID();
            Task updatedTask = new Task(taskId1, "Task 1", "Descrizione Task 1", "In Progress", projectId, newEmployeeId);
            when(taskService.assignEmployeeToTask(taskId1, newEmployeeId)).thenReturn(updatedTask);

            Map<String, String> request = new HashMap<>();
            request.put("employeeId", newEmployeeId.toString());

            ResponseEntity<?> response = taskController.assignEmployeeToTask(taskId1, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(updatedTask, response.getBody());
        }

        @Test
        void assignEmployeeToTask_NotFound() {
            UUID nonExistentTaskId = UUID.randomUUID();
            when(taskService.assignEmployeeToTask(eq(nonExistentTaskId), any(UUID.class))).thenThrow(new TaskNotFoundException(nonExistentTaskId));

            Map<String, String> request = new HashMap<>();
            request.put("employeeId", UUID.randomUUID().toString());

            ResponseEntity<?> response = taskController.assignEmployeeToTask(nonExistentTaskId, request);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task with id: "+nonExistentTaskId, response.getBody());
        }

        @Test
        void removeEmployeeFromTask_Success() {
            ResponseEntity<?> response = taskController.removeEmployeeFromTask(taskId1);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(taskService).removeEmployeeFromTask(taskId1);
        }

        @Test
        void removeEmployeeFromTask_NotFound() {
            UUID nonExistentTaskId = UUID.randomUUID();
            doThrow(new TaskNotFoundException(nonExistentTaskId)).when(taskService).removeEmployeeFromTask(nonExistentTaskId);

            ResponseEntity<?> response = taskController.removeEmployeeFromTask(nonExistentTaskId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("Could not find task with id: "+nonExistentTaskId, response.getBody());
        }

        @Test
        void removeEmployeeFromTask_NullId() {
            // Arrange
            doThrow(new IllegalArgumentException("Task ID cannot be null"))
                    .when(taskService).removeEmployeeFromTask(null);

            // Act
            ResponseEntity<?> response = taskController.removeEmployeeFromTask(null);

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Task ID cannot be null", response.getBody());
        }
    }