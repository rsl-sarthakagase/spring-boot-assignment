package com.hr.leave_management.dto;

import com.hr.leave_management.model.LeaveStatus;
import com.hr.leave_management.model.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeaveRequestDto {

    private LeaveType leaveType;

    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;

    private LeaveStatus status;
}
