package com.hr.leave_management.dto;

import com.hr.leave_management.model.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequestDto {

    @NotBlank(message = "Employee ID must not be blank")
    private String employeeId;

    @NotNull(message = "Leave type must not be null")
    private LeaveType leaveType;

    @NotNull(message = "Start date must not be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date must not be null")
    private LocalDate endDate;

    @NotBlank(message = "Reason must not be blank")
    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
