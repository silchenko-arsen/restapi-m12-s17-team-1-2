package com.softserve.itacademy.todolist.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CollaboratorDTO {
    @NotNull
    private long collaborator_id;
}