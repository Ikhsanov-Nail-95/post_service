package faang.school.postservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Error Docs Controller", description = "Provides descriptions for common API errors")
@RestController
@RequestMapping("/errors")
public class ErrorDocsController {

    @GetMapping("/entity-not-found")
    public Map<String, String> entityNotFoundDoc() {
        return Map.of(
                "description", "The entity with the specified ID was not found in the database. "
                        + "Check if the ID is correct and if the object exists.",
                "title", "The Entity was not found",
                "type", "entity-not-found"
        );
    }

}