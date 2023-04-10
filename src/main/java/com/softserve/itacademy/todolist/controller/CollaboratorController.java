package com.softserve.itacademy.todolist.controller;

import com.softserve.itacademy.todolist.dto.CollaboratorDTO;
import com.softserve.itacademy.todolist.model.ToDo;
import com.softserve.itacademy.todolist.model.User;
import com.softserve.itacademy.todolist.service.TaskService;
import com.softserve.itacademy.todolist.service.ToDoService;
import com.softserve.itacademy.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users/{user_id}/todos/{todo_id}/collaborators")
public class CollaboratorController {

    private final ToDoService toDoService;
    private final UserService userService;

    public CollaboratorController(ToDoService toDoService, UserService userService) {
        this.toDoService = toDoService;
        this.userService = userService;
    }

    @GetMapping()
    List<User> getCollaborator(@PathVariable("user_id") long user_id, @PathVariable("todo_id") long todo_id){
        if(toDoService.readById(todo_id) == null || userService.readById(user_id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (toDoService.readById(todo_id).getOwner().getId() == user_id || toDoService.readById(todo_id).getCollaborators().contains(userService.readById(user_id)) || userService.readById(user_id).getRole().getName().equals("ADMIN")){
            ToDo todo = toDoService.readById(todo_id);
            return todo.getCollaborators();
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
    @PostMapping()
    void addCollaborator(@PathVariable("user_id") long user_id, @PathVariable("todo_id") int todo_id, @Valid @RequestBody CollaboratorDTO clbDTO){
        if(toDoService.readById(todo_id) == null || userService.readById(user_id) == null || userService.readById(clbDTO.getCollaborator_id()) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (toDoService.readById(todo_id).getCollaborators().contains(userService.readById(clbDTO.getCollaborator_id()))) throw new ResponseStatusException(HttpStatus.CONFLICT);
        if (toDoService.readById(todo_id).getOwner().getId() == user_id || userService.readById(user_id).getRole().getName().equals("ADMIN")){
            ToDo todo = toDoService.readById(todo_id);
            List<User> collaborators = todo.getCollaborators();
            collaborators.add(userService.readById(clbDTO.getCollaborator_id()));
            todo.setCollaborators(collaborators);
            toDoService.update(todo);
        }else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
    @DeleteMapping()
    void removeCollaborator(@PathVariable("user_id") long user_id, @PathVariable("todo_id") int todo_id, @Valid @RequestBody CollaboratorDTO clbDTO){
        if(toDoService.readById(todo_id) == null || userService.readById(user_id) == null || userService.readById(clbDTO.getCollaborator_id()) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!toDoService.readById(todo_id).getCollaborators().contains(userService.readById(clbDTO.getCollaborator_id()))) throw new ResponseStatusException(HttpStatus.CONFLICT);
        if (toDoService.readById(todo_id).getOwner().getId() == user_id || userService.readById(user_id).getRole().getName().equals("ADMIN")){
            ToDo todo = toDoService.readById(todo_id);
            List<User> collaborators = todo.getCollaborators();
            collaborators.remove(userService.readById(clbDTO.getCollaborator_id()));
            todo.setCollaborators(collaborators);
            toDoService.update(todo);
        }else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

}
