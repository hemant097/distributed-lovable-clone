package com.example.distribute_lovable_clone.workspace_service.mapper;


import com.example.distribute_lovable_clone.workspace_service.entity.ProjectFile;
import com.example.distributelovableclone.commonlib.dto.FileNode;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
