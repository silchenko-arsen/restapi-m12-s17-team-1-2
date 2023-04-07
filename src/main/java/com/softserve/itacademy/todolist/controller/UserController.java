package com.softserve.itacademy.todolist.controller;

import com.softserve.itacademy.todolist.model.Task;
import com.softserve.itacademy.todolist.model.ToDo;
import com.softserve.itacademy.todolist.model.User;
import com.softserve.itacademy.todolist.dto.UserResponse;
import com.softserve.itacademy.todolist.service.RoleService;
import com.softserve.itacademy.todolist.service.ToDoService;
import com.softserve.itacademy.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final ToDoService toDoService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, ToDoService toDoService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.toDoService = toDoService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    List<UserResponse> getAll() {
        return userService.getAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleService.readById(2));
        return userService.create(user);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable long id) {
        if (userService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return userService.readById(id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable long id, @Valid @RequestBody User user) {
        if (userService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        user.setId(id);
        User oldUser = userService.readById(id);
        if (oldUser.getRole().getName().equals("USER")) {
            user.setRole(oldUser.getRole());
        } else {
            user.setRole(roleService.readById(2));
        }
        userService.update(user);
    }


    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        if (userService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        userService.delete(id);
    }

    @GetMapping("{user_id}/todos")
    List<ToDo> getAllTodos(@PathVariable("user_id") Long user_id) {

        return userService.readById(user_id).getMyTodos();
    }

    @GetMapping("{user_id}/todos/{todo_id}/tasks")
    List<Task> getTasks(@PathVariable("user_id") Long user_id, @PathVariable("todo_id") int todo_id) {
        return userService.readById(user_id).getMyTodos().get(todo_id).getTasks();
    }

    @PostMapping("/api/todos/{todo_id}/tasks")
    Task createTask(@RequestBody Task task, @PathVariable("todo_id") int todo_id) {
        List<Task> tasks = toDoService.readById(todo_id).getTasks();
        tasks.add(task);
        toDoService.readById(todo_id).setTasks(tasks);
        return task;
    }

    @PostMapping("{user_id}/todos/{todo_id}/collaborators")
    void addCollaborator(@PathVariable("user_id") Long user_id, @PathVariable("todo_id") int todo_id){
        List<User> collaborators = toDoService.readById(todo_id).getCollaborators();
        collaborators.add(userService.readById(user_id));
        toDoService.readById(todo_id).setCollaborators(collaborators);
    }
}
