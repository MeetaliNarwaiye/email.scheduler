package com.project.email.scheduler.audit.employee.service;

import com.project.email.scheduler.audit.employee.dao.Employee;
import com.project.email.scheduler.audit.employee.dao.EmployeeRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        if (employee.getGender() != null && !isValidGender(employee.getGender())) {
            throw new IllegalArgumentException("Invalid gender value");
        }
        return repo.save(employee);
    }

    public Employee updateEmployee(@NotNull Long id, @Valid Employee employee) {
        // Check if the employee exists in the database
        if (repo.existsById(id)) {
            if (employee.getGender() != null && !isValidGender(employee.getGender())) {
                throw new IllegalArgumentException("Invalid gender value");
            }

            // Fetch the existing employee object
            Optional<Employee> existingEmployeeOpt = repo.findById(id);
            if (existingEmployeeOpt.isPresent()) {
                Employee existingEmployee = existingEmployeeOpt.get();

                // Copy the non-null properties from the incoming employee to the existing one
                BeanUtils.copyProperties(employee, existingEmployee, getNullPropertyNames(employee));

                return repo.save(existingEmployee);
            }
        }
        throw new EntityNotFoundException("Employee with ID " + id + " not found");
    }

    // Helper method to get null property names
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
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