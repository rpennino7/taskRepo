package org.example.taskservice.service;
import org.example.taskservice.exception.TaskForEmployeeNotFoundException;
import org.example.taskservice.exception.TaskForProjectNotFoundException;
import org.example.taskservice.exception.TaskNotFoundException;
import org.example.taskservice.model.Task;
import org.example.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task1;
    private Task task2;
    private UUID taskId1;
    private UUID taskId2;
    private UUID projectId;
    private UUID employeeId;

    @BeforeEach
    public void setUp() {
        // Inizializzazione dei mock e creazione di esempi di task
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);

        taskId1 = UUID.randomUUID();
        taskId2 = UUID.randomUUID();
        projectId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        task1 = new Task(taskId1, "Task 1", "Descrizione Task 1", "New", projectId, null);
        task2 = new Task(taskId2, "Task 2", "Descrizione Task 2", "In Progress", projectId, null);

        List<Task> allTasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(allTasks);
        when(taskRepository.findById(taskId1)).thenReturn(Optional.of(task1));
        when(taskRepository.findById(taskId2)).thenReturn(Optional.of(task2));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByProjectId(projectId)).thenReturn(allTasks);
        when(taskRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(task1));
    }

    @Test
    public void testFindAllTasks() {
        // Simula il comportamento del repository
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        // Chiamata al servizio
        List<Task> tasks = taskService.getAllTasks();

        // Verifica
        assertEquals(2, tasks.size());
        assertEquals(task1.getName(), tasks.get(0).getName());
        assertEquals(task2.getName(), tasks.get(1).getName());
    }

    @Test
    public void testFindAllTasks_EmptyArray() {
        // Simula il comportamento del repository
        when(taskRepository.findAll()).thenReturn(Arrays.asList());

        // Chiamata al servizio
        List<Task> tasks = taskService.getAllTasks();

        // Verifica
        assertEquals(0, tasks.size());
    }

    @Test
    public void testFindTaskById_found() {
        // Simula il comportamento del repository
        UUID taskId = task1.getId();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task1));

        // Chiamata al servizio
        Task foundTask = taskService.getTaskById(taskId);

        // Verifica
        assertEquals(taskId, foundTask.getId());
        assertEquals("Task 1", foundTask.getName());
    }

    @Test
    public void testFindTaskById_notFound() {
        // Simula il comportamento del repository
        UUID nonExistingId = UUID.randomUUID();
        when(taskRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Chiamata al servizio e verifica dell'eccezione
        Exception exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(nonExistingId);
        });

        // Verifica del messaggio dell'eccezione
        String expectedMessage = "Could not find task with id: " + nonExistingId;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testFindTaskById_NullId() {
        // Chiamata al servizio e verifica dell'eccezione
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTaskById(null);
        });

        // Verifica del messaggio dell'eccezione
        String expectedMessage = "Task ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testCreateTask() {
        Task newTask = new Task(UUID.randomUUID(), "New Task", "Descrizione del nuovo task", "Pending", projectId, null);
        Task createdTask = taskService.createTask(newTask);
        assertNotNull(createdTask.getId());
        assertEquals(newTask.getName(), createdTask.getName());
    }



    @Test
    public void testUpdateTask() {
        Task updatedTask = new Task(taskId1, "Task 1 Updated", "Updated description", "In Progress", projectId, null);
        Task savedTask = taskService.updateTask(taskId1, updatedTask);
        assertEquals(updatedTask.getName(), savedTask.getName());
        assertEquals(updatedTask.getDescription(), savedTask.getDescription());
        assertEquals(updatedTask.getStatus(), savedTask.getStatus());
    }

    @Test
    public void testUpdateTask_NullId() {
        Task updatedTask = new Task(UUID.randomUUID(), "Task Updated", "Updated description", "In Progress", projectId, null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(null, updatedTask);
        });

        String expectedMessage = "Task ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testDeleteTask() {
        assertDoesNotThrow(() -> taskService.deleteTask(taskId1));
    }

    @Test
    public void testDeleteTask_notFound() {
        UUID nonExistingId = UUID.randomUUID();
        // Simula il comportamento del repository
        doThrow(new TaskNotFoundException(nonExistingId))
                .when(taskRepository).deleteById(nonExistingId);

        // Chiamata al servizio e verifica dell'eccezione
        Exception exception = assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(nonExistingId);
        });

        // Verifica del messaggio dell'eccezione
        String expectedMessage = "Could not find task with id: " + nonExistingId;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testDeleteTask_NullId() {
        // Chiamata al servizio e verifica dell'eccezione
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(null);
        });

        // Verifica del messaggio dell'eccezione
        String expectedMessage = "Task ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }
    @Test
    public void testFindTasksByProjectId() {
        List<Task> tasks = taskService.getTasksByProjectId(projectId);
        assertEquals(2, tasks.size());
    }

    @Test
    public void testFindTaskByProjectId_notFound() {
        UUID nonExistingProjectId = UUID.randomUUID();
        Exception exception = assertThrows(TaskForProjectNotFoundException.class, () -> taskService.getTasksByProjectId(nonExistingProjectId));
        assertEquals("Could not find any task for project with id: " + nonExistingProjectId, exception.getMessage());
    }

    @Test
    public void testGetTasksByProjectId_NullId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.getTasksByProjectId(null);
        });

        String expectedMessage = "Project ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testFindTaskByEmployeeId_found() {
        Task foundTask = taskService.getTaskByEmployeeId(employeeId);
        assertEquals(taskId1, foundTask.getId());
    }

    @Test
    public void testFindTaskByEmployeeId_notFound() {
        UUID nonExistingEmployeeId = UUID.randomUUID();
        Exception exception = assertThrows(TaskForEmployeeNotFoundException.class, () -> taskService.getTaskByEmployeeId(nonExistingEmployeeId));
        assertEquals("Could not find task for employee with id: " + nonExistingEmployeeId, exception.getMessage());
    }

    @Test
    public void testAssignEmployeeToTask() {
        Task assignedTask = taskService.assignEmployeeToTask(taskId1, employeeId);
        assertEquals(employeeId, assignedTask.getEmployeeId());
    }

    @Test
    public void testAssignEmployeeToTask_NullTaskId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.assignEmployeeToTask(null, employeeId);
        });

        String expectedMessage = "Task ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testAssignEmployeeToTask_NullEmployeeId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.assignEmployeeToTask(taskId1, null);
        });

        String expectedMessage = "Employee ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testRemoveEmployeeFromTask() {
        taskService.removeEmployeeFromTask(taskId1);
        assertNull(task1.getEmployeeId());
    }

    @Test
    public void testRemoveEmployeeFromTask_NullTaskId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.removeEmployeeFromTask(null);
        });

        String expectedMessage = "Task ID cannot be null";
        assertEquals(expectedMessage, exception.getMessage());
    }


}