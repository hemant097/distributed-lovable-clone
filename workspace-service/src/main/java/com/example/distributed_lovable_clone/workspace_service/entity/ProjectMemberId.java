package com.example.distributed_lovable_clone.workspace_service.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ProjectMemberId  {

    Long projectId;
    Long userId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectMemberId that)) return false;

        return getProjectId().equals(that.getProjectId()) && getUserId().equals(that.getUserId());
    }

    @Override
    public int hashCode() {
        int result = getProjectId().hashCode();
        result = 31 * result + getUserId().hashCode();
        return result;
    }
}
