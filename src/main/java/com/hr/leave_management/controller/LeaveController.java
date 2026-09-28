package com.hr.leave_management.controller;

import com.hr.leave_management.dto.CreateLeaveRequestDto;
import com.hr.leave_management.dto.LeaveResponseDto;
import com.hr.leave_management.dto.UpdateLeaveRequestDto;
import com.hr.leave_management.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * GET /leaves -> 200 OK with list of all leave requests.
     */
    @GetMapping
    public ResponseEntity<List<LeaveResponseDto>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    /**
     * GET /leaves/{id} -> 200 OK with the matching leave request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    /**
     * POST /leaves -> 201 Created with the newly created leave request.
     */
    @PostMapping
    public ResponseEntity<LeaveResponseDto> createLeave(
            @Valid @RequestBody CreateLeaveRequestDto dto) {
        LeaveResponseDto created = leaveService.createLeave(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /leaves/{id} -> 200 OK with the updated leave request.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeaveRequestDto dto) {
        return ResponseEntity.ok(leaveService.updateLeave(id, dto));
    }

    /**
     * DELETE /leaves/{id} -> 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.noContent().build();
    }
}
