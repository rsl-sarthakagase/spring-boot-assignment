package com.hr.leave_management.service;

import com.hr.leave_management.dto.CreateLeaveRequestDto;
import com.hr.leave_management.dto.LeaveResponseDto;
import com.hr.leave_management.dto.UpdateLeaveRequestDto;
import com.hr.leave_management.exception.BadRequestException;
import com.hr.leave_management.exception.ResourceNotFoundException;
import com.hr.leave_management.model.LeaveRequest;
import com.hr.leave_management.model.LeaveStatus;
import com.hr.leave_management.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;

    @Override
    public LeaveResponseDto createLeave(CreateLeaveRequestDto dto) {
        log.debug("Creating leave request for employee: {}", dto.getEmployeeId());
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(dto.getEmployeeId())
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        LeaveRequest saved = leaveRepository.save(leaveRequest);
        log.debug("Leave request created with id: {}", saved.getId());
        return toResponseDto(saved);
    }

    @Override
    public List<LeaveResponseDto> getAllLeaves() {
        log.debug("Fetching all leave requests");
        return leaveRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public LeaveResponseDto getLeaveById(Long id) {
        log.debug("Fetching leave request with id: {}", id);
        LeaveRequest leaveRequest = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        return toResponseDto(leaveRequest);
    }

    @Override
    public LeaveResponseDto updateLeave(Long id, UpdateLeaveRequestDto dto) {
        log.debug("Updating leave request with id: {}", id);
        LeaveRequest existing = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));

        if (dto.getLeaveType() != null) {
            existing.setLeaveType(dto.getLeaveType());
        }

        // Resolve effective start/end dates for range validation
        java.time.LocalDate effectiveStart = dto.getStartDate() != null ? dto.getStartDate() : existing.getStartDate();
        java.time.LocalDate effectiveEnd = dto.getEndDate() != null ? dto.getEndDate() : existing.getEndDate();

        if (dto.getStartDate() != null || dto.getEndDate() != null) {
            validateDateRange(effectiveStart, effectiveEnd);
        }

        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            existing.setEndDate(dto.getEndDate());
        }
        if (dto.getReason() != null) {
            existing.setReason(dto.getReason());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        LeaveRequest updated = leaveRepository.save(existing);
        log.debug("Leave request updated: {}", updated.getId());
        return toResponseDto(updated);
    }

    @Override
    public void deleteLeave(Long id) {
        log.debug("Deleting leave request with id: {}", id);
        if (!leaveRepository.existsById(id)) {
            throw new ResourceNotFoundException("LeaveRequest", id);
        }
        leaveRepository.deleteById(id);
        log.debug("Leave request deleted with id: {}", id);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException(
                    "End date (" + endDate + ") must be on or after start date (" + startDate + ")");
        }
    }

    private LeaveResponseDto toResponseDto(LeaveRequest lr) {
        return LeaveResponseDto.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployeeId())
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt())
                .build();
    }
}
