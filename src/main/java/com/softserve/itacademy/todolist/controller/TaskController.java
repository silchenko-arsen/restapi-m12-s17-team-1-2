package com.softserve.itacademy.todolist.controller;
import com.softserve.itacademy.todolist.model.Task;
import com.softserve.itacademy.todolist.model.User;
import com.softserve.itacademy.todolist.repository.TaskRepository;
import com.softserve.itacademy.todolist.service.StateService;
import com.softserve.itacademy.todolist.service.TaskService;
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
@RequestMapping("/api/")
public class TaskController {
    private final TaskService taskService;
    private final ToDoService todoService;
    private final StateService stateService;

    private final TaskRepository taskRepository;

    private final ToDoService toDoService;
    private final UserService userService;


    public TaskController(TaskRepository taskRepository, ToDoService toDoService, TaskService taskService, ToDoService todoService, StateService stateService, UserService userService) {
        this.taskService = taskService;
        this.todoService = todoService;
        this.stateService = stateService;
        this.taskRepository = taskRepository;
        this.toDoService = toDoService;
        this.userService = userService;
    }


    @GetMapping()
    List<Task> getAllTasks(@PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id,
                      Authentication authentication) {
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (user_id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return todoService.readById(todo_id).getTasks();
    }

    @GetMapping("tasks/{taskId}")
    public Task getTaskById(@PathVariable Long taskId, @PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id, @Valid @RequestBody Task task,
                            Authentication authentication) throws Exception {
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (user_id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    Task create(@PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id, @Valid @RequestBody Task task,
                Authentication authentication) {
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (user_id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        List<Task> tasks = todoService.readById(todo_id).getTasks();
        tasks.add(task);
        todoService.readById(todo_id).setTasks(tasks);
        return task;
    }

    @PutMapping("tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public Task updateTaskById(@PathVariable Long taskId, @RequestBody Task taskRequest, @PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id, @Valid @RequestBody Task task,
                               Authentication authentication) throws Exception {
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (user_id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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
    public ResponseEntity<?> deleteTaskById(@PathVariable Long taskId, @RequestBody Task taskRequest, @PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id, @Valid @RequestBody Task task,
                                            Authentication authentication) throws Exception {
        if(userService.readById(user_id) == null || todoService.readById(todo_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (user_id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return taskRepository.findById(taskId)
                .map(task1 -> {
                    taskRepository.delete(task);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
