package com.example.distribute_lovable_clone.workspace_service.service.impl;

import com.example.distribute_lovable_clone.workspace_service.dto.project.DeployResponse;
import com.example.distribute_lovable_clone.workspace_service.repository.ProjectRepository;
import com.example.distribute_lovable_clone.workspace_service.service.DeploymentService;
import com.example.distributelovableclone.commonlib.errors.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class KubernetesDeploymentServiceImpl implements DeploymentService {

    private final KubernetesClient kubernetesClient;
    private final ProjectRepository projectRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String NAMESPACE = "my-apps";
    private static final String POOL_LABEL = "status";
    private static final String PROJECT_LABEL = "project-id";
    private static final String IDLE = "idle";
    private static final String BUSY = "busy";
    private static final String RUNNER_CONTAINER = "runner";
    private static final String SYNCER_CONTAINER = "syncer";
    private static final String REVERSE_PROXY_PORT = "8090";
    @Override
    public DeployResponse deploy(Long projectId) {
        checkIfProjectExists(projectId);
        String domain = "project-"+projectId+".app.domain.com";

        Pod existingPod = findActivePod(projectId);
        if(existingPod!=null){
            registerRoute(domain,existingPod);
            return new DeployResponse("http://"+domain+":"+REVERSE_PROXY_PORT);
        }

        return claimAndStartNewPod(projectId,domain);
    }

    private DeployResponse claimAndStartNewPod(Long projectId, String domain) {
        //checking for an idle pod
        Pod newPod =  kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(POOL_LABEL,IDLE)
                .list()
                .getItems()
                .stream()
                .findFirst()
                .orElseThrow(()->new RuntimeException("no idle runners available. Please scale up runner-pool"));

        String podName = newPod.getMetadata().getName();
        log.info("Claiming pod {} for project {}",podName,projectId);

        //labelling the claimed pod with labels projectId, and BUSY
        kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName).edit(pod->{
            pod.getMetadata().getLabels().put(POOL_LABEL,BUSY);
            pod.getMetadata().getLabels().put(PROJECT_LABEL,projectId.toString());
            return pod;
        });

        try {
            //Syncer commands

            //Copies everything from MinIO's  to local /app/ folder, replacing any existing files.
            String initialSyncCommand = String.format(
                    "mc mirror --overwrite myminio/projects/%s/ /app/",
                    "project-" + projectId); //
            log.info("Starting initial sync for projectId {} in pod {}", projectId, podName);
            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", initialSyncCommand);


            log.info("Starting watch command for projectId {} in pod {}", projectId, podName);
            String watchCommand = String.format(
                    "nohup mc mirror --overwrite --watch myminio/projects/%s/ /app/ > /app/sync.log 2>&1 &"
                    , "project-" + projectId);
            ;
            execCommand(podName, SYNCER_CONTAINER, "sh", "-c", watchCommand);

            //Runner commands
            String startCommand = "npm install && nohup npm run dev -- --host 0.0.0.0 --port 5173 > /app/dev.log 2>&1 &";
            log.info("Starting dev server for projectId {}...", projectId);
            execCommand(podName, RUNNER_CONTAINER, "sh", "-c", startCommand);

            registerRoute(domain,newPod);

            log.info("Deployment successful: http://{}:{}", domain, REVERSE_PROXY_PORT);
            return new DeployResponse("http://" + domain + ":" + REVERSE_PROXY_PORT);
        }catch (Exception exception){
            log.error("Deployment failed due to for project {}, releasing pod {}",projectId,podName);
            kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName).delete();
            throw new RuntimeException("Failed to deploy the project with id: "+projectId,exception);
        }
    }

    private void registerRoute(String domain,Pod pod){
        String podIP = pod.getStatus().getPodIP();
        if(podIP == null)
            throw new RuntimeException("Pod is running but has no IP!");

        redisTemplate.opsForValue().set("route:"+domain,podIP+":5173",6,TimeUnit.HOURS);
    }

    private void execCommand(String podName,String container, String... command){

        // for storing the eventual result or error
        CompletableFuture<String> data = new CompletableFuture<>();
        try(ExecWatch ignored = kubernetesClient.pods().inNamespace(NAMESPACE).withName(podName)
                                    .inContainer(container)
                .writingOutput(new ByteArrayOutputStream())
                .writingError(new ByteArrayOutputStream())
                .usingListener(new ExecListener() {
                    //command completed with some exit code
                    @Override
                    public void onClose(int i, String reason) {
                        log.info("Closed with code: {}, and reason: {}",i,reason);
                            data.complete("Done");
                    }
                    //exec session failed before completion
                    @Override
                    public void onFailure(Throwable t, Response failureResponse) {
                        log.info("Execution failed with message: {}",t.getMessage());
                        data.completeExceptionally(t);
                    }
                })
                .exec(command))
        {

            // wait briefly to ensure command fired (Fabric8 exec is async)
            // for long-running background jobs(nohup or no hang up), we don't wait for "Done" e.g., nohup java -jar myapp.jar &
            if(command[command.length-1].trim().endsWith("&")){
                Thread.sleep(500);
            }
            else{
                data.get(30, TimeUnit.SECONDS);// block for synchronous setup commands (npm install)
            }

        }
        catch (Exception e){
            log.error("Exec failed -> ",e);
            throw new RuntimeException("Pod execution failed",e);
        }
    }

    Pod findActivePod(Long projectId){
        return kubernetesClient.pods().inNamespace(NAMESPACE)
                .withLabel(PROJECT_LABEL,projectId.toString())
                .withLabel(POOL_LABEL,BUSY)
                .list()
                .getItems()
                .stream()
                .filter(pod -> pod.getStatus().getPhase().equals("Running"))
                .findFirst()
                .orElse(null);
    }

    //Internal functions
    public void checkIfProjectExists(Long projectId){
        if(!projectRepo.existsById(projectId))
            throw new ResourceNotFoundException("Project",projectId.toString());
    }
}
