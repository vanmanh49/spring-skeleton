package com.vm.skeleton.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestRoleController {

    @GetMapping("admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String adminRoleOnly() {
        return "Hello admin";
    }

    @GetMapping("editor")
    @PreAuthorize("hasRole('EDITOR')")
    public String editorRoleOnly() {
        return "Hello editor";
    }

    @GetMapping("authenticated-user")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','EDITOR')")
    public String anyRoles() {
        return "Hello authenticated user";
    }
}
