package com.hr.leave_management.controller;

import com.hr.leave_management.dto.CreateLeaveRequestDto;
import com.hr.leave_management.dto.LeaveResponseDto;
import com.hr.leave_management.dto.UpdateLeaveRequestDto;
import com.hr.leave_management.exception.GlobalExceptionHandler;
import com.hr.leave_management.exception.ResourceNotFoundException;
import com.hr.leave_management.model.LeaveStatus;
import com.hr.leave_management.model.LeaveType;
import com.hr.leave_management.service.LeaveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { LeaveController.class, GlobalExceptionHandler.class })
class LeaveControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private LeaveService leaveService;

        @Autowired
        private ObjectMapper objectMapper;

        // -------------------------------------------------------------------------
        // Helper
        // -------------------------------------------------------------------------

        private LeaveResponseDto buildResponseDto(Long id) {
                return new LeaveResponseDto(
                                id, "EMP001", LeaveType.ANNUAL,
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(3),
                                "Vacation", LeaveStatus.PENDING,
                                LocalDateTime.now(), LocalDateTime.now());
        }

        // -------------------------------------------------------------------------
        // GET /leaves
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("GET /leaves -> 200 OK with JSON array")
        void getAllLeaves_returns200() throws Exception {
                when(leaveService.getAllLeaves()).thenReturn(List.of(buildResponseDto(1L), buildResponseDto(2L)));

                mockMvc.perform(get("/leaves").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2));
        }

        // -------------------------------------------------------------------------
        // GET /leaves/{id}
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("GET /leaves/{id} -> 200 OK when found")
        void getLeaveById_returns200() throws Exception {
                when(leaveService.getLeaveById(1L)).thenReturn(buildResponseDto(1L));

                mockMvc.perform(get("/leaves/1").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.employeeId").value("EMP001"));
        }

        @Test
        @DisplayName("GET /leaves/{id} -> 404 NOT FOUND when not found")
        void getLeaveById_returns404() throws Exception {
                when(leaveService.getLeaveById(99L))
                                .thenThrow(new ResourceNotFoundException("LeaveRequest", 99L));

                mockMvc.perform(get("/leaves/99").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }

        // -------------------------------------------------------------------------
        // POST /leaves
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("POST /leaves -> 201 Created on valid body")
        void createLeave_returns201() throws Exception {
                CreateLeaveRequestDto dto = new CreateLeaveRequestDto(
                                "EMP001", LeaveType.ANNUAL,
                                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "Vacation");
                when(leaveService.createLeave(any(CreateLeaveRequestDto.class))).thenReturn(buildResponseDto(1L));

                mockMvc.perform(post("/leaves")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("POST /leaves -> 400 Bad Request when employeeId is blank")
        void createLeave_missingEmployeeId_returns400() throws Exception {
                String body = """
                                {
                                  "leaveType": "ANNUAL",
                                  "startDate": "%s",
                                  "endDate": "%s",
                                  "reason": "Vacation"
                                }
                                """.formatted(
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(3));

                mockMvc.perform(post("/leaves")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.fieldErrors.employeeId").exists());
        }

        // -------------------------------------------------------------------------
        // PUT /leaves/{id}
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("PUT /leaves/{id} -> 200 OK on valid update")
        void updateLeave_returns200() throws Exception {
                UpdateLeaveRequestDto dto = new UpdateLeaveRequestDto();
                dto.setReason("Updated reason");
                dto.setStatus(LeaveStatus.APPROVED);

                when(leaveService.updateLeave(eq(1L), any(UpdateLeaveRequestDto.class)))
                                .thenReturn(buildResponseDto(1L));

                mockMvc.perform(put("/leaves/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));
        }

        // -------------------------------------------------------------------------
        // DELETE /leaves/{id}
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("DELETE /leaves/{id} -> 204 No Content")
        void deleteLeave_returns204() throws Exception {
                doNothing().when(leaveService).deleteLeave(1L);

                mockMvc.perform(delete("/leaves/1"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /leaves/{id} -> 404 when not found")
        void deleteLeave_returns404() throws Exception {
                doThrow(new ResourceNotFoundException("LeaveRequest", 99L))
                                .when(leaveService).deleteLeave(99L);

                mockMvc.perform(delete("/leaves/99").accept(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }
}
