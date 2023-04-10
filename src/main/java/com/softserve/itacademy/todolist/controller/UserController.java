package com.softserve.itacademy.todolist.controller;

import com.softserve.itacademy.todolist.model.User;
import com.softserve.itacademy.todolist.dto.UserResponse;
import com.softserve.itacademy.todolist.service.RoleService;
import com.softserve.itacademy.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
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
    public User findById(@PathVariable long id, Authentication authentication) {
        authorize(id, authentication);
        if (userService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return userService.readById(id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable long id, @Valid @RequestBody User user, Authentication authentication) {
        authorize(id, authentication);
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
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        authorize(id, authentication);
        if (userService.readById(id) == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        userService.delete(id);
    }

    public void authorize(long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        long idFromAuth = user.getId();
        if (id != idFromAuth && !userService.readById(idFromAuth).getRole().getName().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }


}