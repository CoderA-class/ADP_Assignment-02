package factory;

/*
        EmployeeFactory.java
        EmployeeFactory model class
        Author: Robyn Dominique Stevens (222201789)
        Date: 19 March 2026
        */

import domain.Employee;

public class EmployeeFactory {

    private static int counter = 1;

    public static Employee createEmployee(String fName,
                                          String lName,
                                          double salary,
                                          String email,
                                          String phone,
                                          String role) {

        String empId = String.format("EMP%04d", counter++);

        return new Employee.Builder()
                .setEmpId(empId)
                .setFName(fName)
                .setLName(lName)
                .setSalary(salary)
                .setEmail(email)
                .setPhone(phone)
                .setRole(role)
                .setEmpStatus(Employee.EmpStatus.ACTIVE)
                .build();
    }
}