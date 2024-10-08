package com.project.email.scheduler.employee.service;

import com.project.email.scheduler.employee.dao.Employee;
import com.project.email.scheduler.employee.dao.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepo repo;

    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    public Optional<Employee> getEmployeeById(@NotNull Long id) {
        return repo.findById(id);
    }

    public Employee createEmployee(@Valid Employee employee) {
        if (!isValidGender(employee.getGender())) {
            throw new IllegalArgumentException("Invalid gender value");
        }
        return repo.save(employee);
    }

    public Employee updateEmployee(@NotNull Long id, @Valid Employee employee) {
        if (repo.existsById(id)) {
            if (!isValidGender(employee.getGender())) {
                throw new IllegalArgumentException("Invalid gender value");
            }
            employee.setEmployeeId(id);
            return repo.save(employee);
        }
        return null;
    }

    public void deleteEmployee(@NotNull Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        }
    }

    private boolean isValidGender(String gender) {
        return "Male".equalsIgnoreCase(gender) || "Female".equalsIgnoreCase(gender) || "Other".equalsIgnoreCase(gender);
    }
}