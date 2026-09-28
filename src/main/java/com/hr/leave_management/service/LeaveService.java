package com.hr.leave_management.service;

import com.hr.leave_management.dto.CreateLeaveRequestDto;
import com.hr.leave_management.dto.LeaveResponseDto;
import com.hr.leave_management.dto.UpdateLeaveRequestDto;

import java.util.List;

public interface LeaveService {

    LeaveResponseDto createLeave(CreateLeaveRequestDto dto);

    List<LeaveResponseDto> getAllLeaves();

    LeaveResponseDto getLeaveById(Long id);

    LeaveResponseDto updateLeave(Long id, UpdateLeaveRequestDto dto);

    void deleteLeave(Long id);
}
