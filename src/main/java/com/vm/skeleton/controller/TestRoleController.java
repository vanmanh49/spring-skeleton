package com.vm.skeleton.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Test", description = "Role-based access test endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class TestRoleController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Admin only", description = "Accessible only by users with ADMINISTRATOR role")
    public String adminRoleOnly() {
        return "Hello admin";
    }

    @GetMapping("/editor")
    @PreAuthorize("hasRole('EDITOR')")
    @Operation(summary = "Editor only", description = "Accessible only by users with EDITOR role")
    public String editorRoleOnly() {
        return "Hello editor";
    }

    @GetMapping("/authenticated-user")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','EDITOR')")
    @Operation(summary = "Any authenticated user", description = "Accessible by any authenticated user with a valid role")
    public String anyRoles() {
        return "Hello authenticated user";
    }
}
