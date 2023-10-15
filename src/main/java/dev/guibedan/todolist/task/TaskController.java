package dev.guibedan.todolist.task;

import dev.guibedan.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: The start/end date is less than the current date!");
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: The start date is greater than the end date!");


        var task = taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.findByIdUser((UUID) idUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        var task = taskRepository.findById(id).orElse(null);
        if (task == null)
            return ResponseEntity.status((HttpStatus.NOT_FOUND)).body("Task not found.");

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser))
            return ResponseEntity.status((HttpStatus.BAD_REQUEST)).body("User not have permission.");

        Utils.copyNoNullProperties(taskModel, task);

        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(task));
    }

}
