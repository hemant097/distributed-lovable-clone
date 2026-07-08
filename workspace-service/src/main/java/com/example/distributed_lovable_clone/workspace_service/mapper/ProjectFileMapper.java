package com.example.distributed_lovable_clone.workspace_service.mapper;


import com.example.distributed_lovable_clone.workspace_service.entity.ProjectFile;
import com.example.distributed_lovable_clone.commonlib.dto.FileNode;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

    List<FileNode> toListOfFileNode(List<ProjectFile> projectFileList);
}
