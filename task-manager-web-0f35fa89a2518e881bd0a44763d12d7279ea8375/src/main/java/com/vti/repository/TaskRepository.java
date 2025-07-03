package com.vti.repository;

import com.vti.model.Project;
import com.vti.model.Task;
import com.vti.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByProject(Project project);

    // Tìm tất cả task mà user được phân công
    List<Task> findByAssignedUsersContaining(User user);

    // Tìm task theo project và user được phân công
    List<Task> findByProjectAndAssignedUsersContaining(Project project, User user);
}
