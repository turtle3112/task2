package com.vti.model;

import jakarta.persistence.*;

@Entity
@IdClass(ProjectMemberId.class)
@Table(name = "`projectmember`")
public class ProjectMember {

    @Id
    private Integer projectId;

    @Id
    private Integer userId;

    public ProjectMember() {
    }

    public ProjectMember(Integer projectId, Integer userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
