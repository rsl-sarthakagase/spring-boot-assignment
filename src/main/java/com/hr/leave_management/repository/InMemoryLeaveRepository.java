package com.hr.leave_management.repository;

import com.hr.leave_management.model.LeaveRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryLeaveRepository implements LeaveRepository {

    private final ConcurrentHashMap<Long, LeaveRequest> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public LeaveRequest save(LeaveRequest leaveRequest) {
        if (leaveRequest.getId() == null) {
            leaveRequest.setId(idGenerator.getAndIncrement());
        }
        store.put(leaveRequest.getId(), leaveRequest);
        return leaveRequest;
    }

    @Override
    public Optional<LeaveRequest> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LeaveRequest> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
