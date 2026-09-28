package com.hr.leave_management.repository;

import com.hr.leave_management.model.LeaveRequest;

import java.util.List;
import java.util.Optional;

public interface LeaveRepository {

    LeaveRequest save(LeaveRequest leaveRequest);

    Optional<LeaveRequest> findById(Long id);

    List<LeaveRequest> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
