package com.vti.repository;

import com.vti.model.ProgressLog;
import com.vti.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressLogRepository extends JpaRepository<ProgressLog, Integer> {
    List<ProgressLog> findByTask(Task task);
    List<ProgressLog> findByTaskIdOrderByCreatedAtDesc(Integer taskId);
    List<ProgressLog> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
