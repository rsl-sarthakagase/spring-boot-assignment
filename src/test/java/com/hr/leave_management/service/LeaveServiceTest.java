package com.hr.leave_management.service;

import com.hr.leave_management.dto.CreateLeaveRequestDto;
import com.hr.leave_management.dto.LeaveResponseDto;
import com.hr.leave_management.dto.UpdateLeaveRequestDto;
import com.hr.leave_management.exception.BadRequestException;
import com.hr.leave_management.exception.ResourceNotFoundException;
import com.hr.leave_management.model.LeaveRequest;
import com.hr.leave_management.model.LeaveStatus;
import com.hr.leave_management.model.LeaveType;
import com.hr.leave_management.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    private LeaveServiceImpl leaveService;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveServiceImpl(leaveRepository);
    }

    // -------------------------------------------------------------------------
    // Helper factory methods
    // -------------------------------------------------------------------------

    private LeaveRequest buildLeaveRequest(Long id) {
        LeaveRequest lr = new LeaveRequest();
        lr.setId(id);
        lr.setEmployeeId("EMP001");
        lr.setLeaveType(LeaveType.ANNUAL);
        lr.setStartDate(LocalDate.now().plusDays(1));
        lr.setEndDate(LocalDate.now().plusDays(3));
        lr.setReason("Vacation");
        lr.setStatus(LeaveStatus.PENDING);
        lr.setCreatedAt(LocalDateTime.now());
        lr.setUpdatedAt(LocalDateTime.now());
        return lr;
    }

    private CreateLeaveRequestDto buildCreateDto() {
        return new CreateLeaveRequestDto(
                "EMP001",
                LeaveType.ANNUAL,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "Vacation");
    }

    // -------------------------------------------------------------------------
    // createLeave
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createLeave - success: saves and returns response DTO")
    void createLeave_success() {
        CreateLeaveRequestDto dto = buildCreateDto();
        LeaveRequest saved = buildLeaveRequest(1L);
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(saved);

        LeaveResponseDto result = leaveService.createLeave(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmployeeId()).isEqualTo("EMP001");
        assertThat(result.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    @DisplayName("createLeave - failure: endDate before startDate throws BadRequestException")
    void createLeave_invalidDateRange_throws() {
        CreateLeaveRequestDto dto = new CreateLeaveRequestDto(
                "EMP001",
                LeaveType.ANNUAL,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(1), // end before start
                "Vacation");

        assertThatThrownBy(() -> leaveService.createLeave(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End date");
        verify(leaveRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getLeaveById
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getLeaveById - success: returns DTO when exists")
    void getLeaveById_found() {
        LeaveRequest lr = buildLeaveRequest(42L);
        when(leaveRepository.findById(42L)).thenReturn(Optional.of(lr));

        LeaveResponseDto result = leaveService.getLeaveById(42L);

        assertThat(result.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getLeaveById - failure: throws ResourceNotFoundException when not found")
    void getLeaveById_notFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getLeaveById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // updateLeave
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateLeave - success: updates existing leave and returns DTO")
    void updateLeave_success() {
        LeaveRequest existing = buildLeaveRequest(1L);
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(leaveRepository.save(any(LeaveRequest.class))).thenReturn(existing);

        UpdateLeaveRequestDto dto = new UpdateLeaveRequestDto();
        dto.setReason("Updated reason");
        dto.setStatus(LeaveStatus.APPROVED);

        LeaveResponseDto result = leaveService.updateLeave(1L, dto);

        assertThat(result).isNotNull();
        verify(leaveRepository).save(existing);
    }

    @Test
    @DisplayName("updateLeave - failure: throws ResourceNotFoundException on missing ID")
    void updateLeave_notFound() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateLeaveRequestDto dto = new UpdateLeaveRequestDto();
        assertThatThrownBy(() -> leaveService.updateLeave(999L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------------
    // deleteLeave
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteLeave - success: removes existing leave")
    void deleteLeave_success() {
        when(leaveRepository.existsById(1L)).thenReturn(true);

        leaveService.deleteLeave(1L);

        verify(leaveRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteLeave - failure: throws ResourceNotFoundException on missing ID")
    void deleteLeave_notFound() {
        when(leaveRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> leaveService.deleteLeave(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("404");
        verify(leaveRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // getAllLeaves
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllLeaves - returns list of response DTOs")
    void getAllLeaves_returnsList() {
        when(leaveRepository.findAll()).thenReturn(List.of(buildLeaveRequest(1L), buildLeaveRequest(2L)));

        List<LeaveResponseDto> results = leaveService.getAllLeaves();

        assertThat(results).hasSize(2);
    }
}
