package com.vti.repository;

import com.vti.model.ProjectMember;
import com.vti.model.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    List<ProjectMember> findByProjectId(Integer projectId);
    List<ProjectMember> findByUserId(Integer userId);
    boolean existsByProjectIdAndUserId(Integer projectId, Integer userId);
}