package com.hireflow.controller;

import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.dto.response.dashboard.AdminOverviewResponse;
import com.hireflow.entity.User;
import com.hireflow.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform administration — ADMIN role only")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    @Operation(summary = "Platform-wide stats overview")
    public ResponseEntity<AdminOverviewResponse> getOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<Page<User>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<MessageResponse> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok(new MessageResponse("User deactivated successfully"));
    }

    @PatchMapping("/users/{id}/activate")
    @Operation(summary = "Reactivate a user account")
    public ResponseEntity<MessageResponse> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(new MessageResponse("User activated successfully"));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Force delete any job posting")
    public ResponseEntity<MessageResponse> forceDeleteJob(@PathVariable Long id) {
        adminService.forceDeleteJob(id);
        return ResponseEntity.ok(new MessageResponse("Job deleted successfully"));
    }
}
