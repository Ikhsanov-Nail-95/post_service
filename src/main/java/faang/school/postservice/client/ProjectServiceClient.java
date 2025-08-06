package faang.school.postservice.client;

import faang.school.postservice.client.dto.ProjectDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "project-service", url = "${project-service.host}:${project-service.port}")
public interface ProjectServiceClient {

    @GetMapping("/api/v1/projects/{projectId}")
    ProjectDto getProject(@PathVariable long projectId, @RequestParam long requestUserId);

    @PostMapping("/projects")
    List<ProjectDto> getProjectsByIds(@RequestBody List<Long> ids);

}