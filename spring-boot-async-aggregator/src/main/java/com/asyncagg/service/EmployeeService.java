package com.asyncagg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.asyncagg.dto.Employee;
import com.asyncagg.repo.EmployeeRepository;

@Service
public class EmployeeService {

	 @Autowired
	 private EmployeeRepository repository;

	 @Cacheable(
	            value = "employees",
	            key = "#id"
	    )
	    public Employee getEmployee(Long id) {

	        System.out.println("Fetching from DB");

	        return repository.findById(id)
	                .orElseThrow();
	    }
	 
	 @CachePut(
		        value = "employees",
		        key = "#id"
		)
		public Employee updateEmployee(
		        Long id,
		        Employee employee
		) {

		    Employee existing =
		            repository.findById(id)
		                    .orElseThrow();

		    existing.setName(employee.getName());
		    existing.setDepartment(employee.getDepartment());
		    existing.setSalary(employee.getSalary());

		    return repository.save(existing);
		}
	 
	 @CacheEvict(
		        value = "employees",
		        key = "#id"
		)
		public void deleteEmployee(Long id) {

		    repository.deleteById(id);
		}
	    
	 public Employee createEmployee(Employee employee) {

	        System.out.println("Saving Employee to Database...");

	        return repository.save(employee);
	    }
	    
}
