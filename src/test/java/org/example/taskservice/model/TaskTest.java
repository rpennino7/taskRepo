package org.example.taskservice.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testNoArgsConstructor() {
        Task task = new Task();
        assertNotNull(task);
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();
        Task task = new Task(id, "Test Task", "Description", "In Progress", projectId, employeeId);

        assertEquals(id, task.getId());
        assertEquals("Test Task", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals("In Progress", task.getStatus());
        assertEquals(projectId, task.getProjectId());
        assertEquals(employeeId, task.getEmployeeId());
    }

    @Test
    void testGettersAndSetters() {
        Task task = new Task();

        UUID id = UUID.randomUUID();
        task.setId(id);
        assertEquals(id, task.getId());

        task.setName("Updated Task");
        assertEquals("Updated Task", task.getName());

        task.setDescription("Updated Description");
        assertEquals("Updated Description", task.getDescription());

        task.setStatus("Completed");
        assertEquals("Completed", task.getStatus());

        UUID projectId = UUID.randomUUID();
        task.setProjectId(projectId);
        assertEquals(projectId, task.getProjectId());

        UUID employeeId = UUID.randomUUID();
        task.setEmployeeId(employeeId);
        assertEquals(employeeId, task.getEmployeeId());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        Task task1 = new Task(id, "Task", "Description", "In Progress", projectId, employeeId);
        Task task2 = new Task(id, "Task", "Description", "In Progress", projectId, employeeId);
        Task task3 = new Task(UUID.randomUUID(), "Different Task", "Different Description", "Completed", UUID.randomUUID(), UUID.randomUUID());

        // Test equals
        assertEquals(task1, task2);
        assertNotEquals(task1, task3);

        // Test hashCode
        assertEquals(task1.hashCode(), task2.hashCode());
        assertNotEquals(task1.hashCode(), task3.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        Task task = new Task(UUID.randomUUID(), "Task", "Description", "In Progress", UUID.randomUUID(), UUID.randomUUID());
        assertNotEquals(null, task);
    }

    @Test
    void testEqualsWithDifferentClass() {
        Task task = new Task(UUID.randomUUID(), "Task", "Description", "In Progress", UUID.randomUUID(), UUID.randomUUID());
        assertNotEquals(task, new Object());
    }

    @Test
    void testEmptyStatus() {
        Task task = new Task();

        assertThrows(IllegalArgumentException.class, () -> {
            task.setStatus("");
        }, "Setting an empty status should throw an IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, () -> {
            task.setStatus(null);
        }, "Setting a null status should throw an IllegalArgumentException");
    }

    @Test
    void testEqualsIgnoreCase() {
        UUID id = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        Task task1 = new Task(id, "Task", "Description", "In Progress", projectId, null);
        Task task2 = new Task(id, "TASK", "Description", "in progress", projectId, null);

        // Questo test fallirà perché equals() non ignora il case
        assertEquals(task1, task2, "Tasks should be equal ignoring case");
    }

    @Test
    void testProjectIdMatch() {
        Task task = new Task();
        UUID projectId = UUID.randomUUID();
        task.setProjectId(projectId);

        // Questo test passerà se getProjectId() restituisce l'ID corretto
        assertEquals(projectId, task.getProjectId(), "ProjectId should match");
    }

    @Test
    void testProjectIdNull() {
        Task task = new Task();
        assertNull(task.getProjectId(), "ProjectId should be null by default");

        task.setProjectId(null);
        assertNull(task.getProjectId(), "ProjectId should allow null values");
    }

}