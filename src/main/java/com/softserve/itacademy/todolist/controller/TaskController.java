package com.softserve.itacademy.todolist.controller;

import com.softserve.itacademy.todolist.model.Task;
import com.softserve.itacademy.todolist.model.ToDo;
import com.softserve.itacademy.todolist.repository.TaskRepository;
import com.softserve.itacademy.todolist.repository.ToDoRepository;
import com.softserve.itacademy.todolist.service.ToDoService;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{u_id}/todos/{taskId}/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ToDoService toDoService;

    @GetMapping
    public List<Task> getAllTasksForTodo(@PathVariable Long taskId) {
        return taskRepository.getByTodoId(taskId);
    }

    @GetMapping("/{t_id}")
    public Task getTaskById(@PathVariable Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId));
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTaskForTodo(@PathVariable Long taskId, @RequestBody Task task) {
        List<Task> tasks = toDoService.readById(taskId).getTasks();
        tasks.add(task);
        toDoService.readById(taskId).setTasks(tasks);
        return task;
    }

    @PutMapping("/{t_id}")
    @ResponseStatus(HttpStatus.OK)
    public Task updateTaskById(@PathVariable Long taskId, @RequestBody Task taskRequest) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    task.setName(taskRequest.getName());
                    task.setPriority(taskRequest.getPriority());
                    task.setState(taskRequest.getState());
                    return taskRepository.save(task);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId));
    }

    @DeleteMapping("/{todoId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteTaskById(@PathVariable Long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    taskRepository.delete(task);
                    return ResponseEntity.ok().build();
                })
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId));
    }
}
