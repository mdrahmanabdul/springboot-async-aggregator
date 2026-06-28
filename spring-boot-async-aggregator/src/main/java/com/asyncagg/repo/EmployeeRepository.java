package com.asyncagg.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asyncagg.dto.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>{

	
}
