package com.softserve.itacademy.todolist.controller;
import com.softserve.itacademy.todolist.model.Task;
import com.softserve.itacademy.todolist.model.User;
import com.softserve.itacademy.todolist.repository.TaskRepository;

import com.softserve.itacademy.todolist.service.ToDoService;
import com.softserve.itacademy.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users/{user_id}/todos/{todo_id}/tasks")
public class TaskController {
    private final ToDoService todoService;

    private final TaskRepository taskRepository;

    private final UserService userService;


    public TaskController(TaskRepository taskRepository, ToDoService todoService, UserService userService) {
        this.todoService = todoService;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }


    @GetMapping()
    List<Task> getAllTasks(@PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id,
                           Authentication authentication) {

        authorize(user_id, authentication);
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return todoService.readById(todo_id).getTasks();
    }

    @GetMapping("tasks/{taskId}")
    public Task getTaskById(
            @PathVariable Long taskId,
            @PathVariable("user_id") long user_id,
            @PathVariable("todo_id") long todo_id,
            Authentication authentication) {

        authorize(user_id, authentication);
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    Task create(
            @PathVariable("user_id") long user_id,
            @PathVariable("todo_id") long todo_id,
            @Valid @RequestBody Task task,
            Authentication authentication) {

        authorize(user_id, authentication);
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<Task> tasks = todoService.readById(todo_id).getTasks();
        tasks.add(task);
        todoService.readById(todo_id).setTasks(tasks);
        return task;
    }

    @PutMapping("tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public Task updateTaskById(
            @PathVariable Long taskId,
            @RequestBody Task taskRequest,
            @PathVariable("user_id") long user_id,
            @PathVariable("todo_id") long todo_id,
            @Valid @RequestBody Task task,
            Authentication authentication) {

        authorize(user_id, authentication);
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return taskRepository.findById(taskId)
                .map(task1 -> {
                    task.setName(taskRequest.getName());
                    task.setPriority(taskRequest.getPriority());
                    task.setState(taskRequest.getState());
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteTaskById(
            @PathVariable Long taskId,
            @PathVariable("user_id") long user_id,
            @PathVariable("todo_id") long todo_id,
            @Valid @RequestBody Task task,
            Authentication authentication) {

        authorize(user_id, authentication);
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return taskRepository.findById(taskId)
                .map(task1 -> {
                    taskRepository.delete(task);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void authorize(long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
