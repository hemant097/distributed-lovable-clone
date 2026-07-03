package com.example.distribute_lovable_clone.intelligence_service.llm.tools;

import com.example.distribute_lovable_clone.intelligence_service.client.WorkspaceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CodeGenerationTool {

    private final Long projectId;
    private final WorkspaceClient workspaceClient;

    @Tool(name = "read_project_files",
          description = "Read the content of files. Only input the file names present inside the FILE_TREE. DO NOT input any path which is not present under the FILE_TREE.")
    public List<String> readFiles(
            @ToolParam(description = "List of relative paths (e.g., ['src/App.tsx','package.json','src/pages/Index.tsx'])")
                List<String> paths
            , ToolContext toolContext
    ){
        String authorizationHeader =
                (String) toolContext.getContext().get("authorizationHeader");

        log.info("These are the paths, which will be changed: {}",paths.toString());

        List<String> result = new ArrayList<>();

        for(String path: paths){
            String cleanPath = path.startsWith("/")? path.substring(1): path;
            log.info("Requested file: {}",cleanPath);

            String content = workspaceClient.getFileContent(projectId,cleanPath,authorizationHeader);

            result.add(String.format(
                    "--- START OF FILE: %s ---\n%s\n--- END OF FILE ---",
                    cleanPath,content
            ));
        }
        return result;
    }
}
