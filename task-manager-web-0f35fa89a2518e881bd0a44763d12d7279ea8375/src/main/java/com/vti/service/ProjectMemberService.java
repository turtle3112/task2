package com.vti.service;

import com.vti.model.Project;
import com.vti.model.ProjectMember;
import com.vti.model.ProjectMemberId;
import com.vti.model.User;
import com.vti.repository.ProjectMemberRepository;
import com.vti.repository.ProjectRepository;
import com.vti.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository, ProjectRepository projectRepository,
			UserRepository userRepository, NotificationService notificationService) {
		super();
		this.projectMemberRepository = projectMemberRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	public ProjectMember addMember(Integer projectId, Integer userId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        ProjectMember member = new ProjectMember(project.getId(), user.getId());
        notificationService.notifyProjectAssignment(user.getUsername(),projectId);
        return projectMemberRepository.save(member);
        
    }

    public void removeMember(Integer projectId, Integer userId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        ProjectMemberId id = new ProjectMemberId(project.getId(), user.getId());
        projectMemberRepository.deleteById(id);
    }

    public List<ProjectMember> getMembersByProject(Integer projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        return projectMemberRepository.findByProjectId(project.getId());
    }

    public List<ProjectMember> getProjectsByUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return projectMemberRepository.findByUserId(user.getId());
    }

    public boolean isUserInProject(Integer projectId, Integer userId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        return projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
    }
}
