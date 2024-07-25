package org.example.taskservice.model;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private UUID projectId;

    private UUID employeeId;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id) &&
                name.equalsIgnoreCase(task.name) &&
                description.equalsIgnoreCase(task.description) &&
                status.equalsIgnoreCase(task.status) &&
                Objects.equals(projectId, task.projectId) &&
                Objects.equals(employeeId, task.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name.toLowerCase(),
                description.toLowerCase(),
                status.toLowerCase(),
                projectId,
                employeeId
        );
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        this.status = status;
    }
}