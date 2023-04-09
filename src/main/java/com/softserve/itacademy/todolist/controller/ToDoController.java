package com.softserve.itacademy.todolist.controller;

import com.softserve.itacademy.todolist.model.ToDo;
import com.softserve.itacademy.todolist.service.TaskService;
import com.softserve.itacademy.todolist.service.ToDoService;
import com.softserve.itacademy.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/{user_id}/todos")
public class ToDoController {
    private final ToDoService toDoService;
    private final TaskService taskService;
    private final UserService userService;

    public ToDoController(ToDoService toDoService, TaskService taskService, UserService userService) {
        this.toDoService = toDoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping()
    List<ToDo> getAll(@PathVariable("user_id") Long user_id) {
        if(userService.readById(user_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<ToDo> todos = userService.readById(user_id).getMyTodos();
        toDoService.getAll().forEach(toDo -> {
            if (toDo.getCollaborators().contains(userService.readById(user_id))) {
                todos.add(toDo);
            }
        });
        return userService.readById(user_id).getMyTodos();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ToDo create(@PathVariable("user_id") long ownerId, @Valid @RequestBody ToDo todo) {
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        return toDoService.create(todo);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ToDo read(@PathVariable long id, @PathVariable long user_id) {
        if (toDoService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (toDoService.readById(id).getCollaborators().contains(userService.readById(user_id)) || userService.readById(user_id).getRole().getName().equals("ADMIN") || toDoService.readById(id).getOwner().getId() == user_id){
            return toDoService.readById(id);
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable long id, @Valid @RequestBody ToDo toDo, @PathVariable long user_id) {
        if (toDoService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (toDoService.readById(id).getCollaborators().contains(userService.readById(user_id)) || userService.readById(user_id).getRole().getName().equals("ADMIN") || toDoService.readById(id).getOwner().getId() == user_id) {
            ToDo oldTodo = toDoService.readById(id);
            toDo.setId(id);
            toDo.setOwner(oldTodo.getOwner());
            toDo.setCollaborators(oldTodo.getCollaborators());
            toDo.setCreatedAt(oldTodo.getCreatedAt());
            toDoService.update(toDo);
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id, @PathVariable long user_id) {
        if (toDoService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (toDoService.readById(id).getCollaborators().contains(userService.readById(user_id)) || userService.readById(user_id).getRole().getName().equals("ADMIN") || toDoService.readById(id).getOwner().getId() == user_id) {
            toDoService.delete(id);
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
