package com.vti.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "`task`")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('TODO', 'IN_PROGRESS', 'DONE') DEFAULT 'TODO'")
    private Status status;

    private LocalDate deadline;

    @ManyToMany
    @JoinTable(
        name = "task_assignment",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedUsers = new HashSet<>();

    public enum Status {
        TODO, IN_PROGRESS, DONE
    }

    // Constructors
    public Task() {
    }

    public Task(Integer id, Project project, String name, String description, Status status, LocalDate deadline, Set<User> assignedUsers) {
        this.id = id;
        this.project = project;
        this.name = name;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.assignedUsers = assignedUsers;
    }

    // Getter + Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public Set<User> getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(Set<User> assignedUsers) { this.assignedUsers = assignedUsers; }
}
