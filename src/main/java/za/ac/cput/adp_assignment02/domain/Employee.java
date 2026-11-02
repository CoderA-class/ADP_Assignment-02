package za.ac.cput.adp_assignment02.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;
/* * Employee.java
* Employee POJO class
* Author: Robyn Dominique Stevens (222201789)
* Date: 19 June 2026 */

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @Column(name = "empID")
    @NotBlank
    private String empID;
    @NotBlank
    @Column(nullable = false)
    @JsonProperty("fName")
    private String fName;
    @NotBlank
    @Column(nullable = false)
    @JsonProperty("lName")
    private String lName;
    @PositiveOrZero
    @Column(nullable = false)
    private double salary;
    @NotBlank
    @Column(nullable = false)
    private String email;
    @NotBlank
    @Column(nullable = false)
    private String phone;
    @NotBlank
    @Column(nullable = false)
    private String role;
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private EmpStatus empStatus;

    public Employee() {
    }

    private Employee(Builder builder) {
        this.empID = builder.empID;
        this.fName = builder.fName;
        this.lName = builder.lName;
        this.salary = builder.salary;
        this.email = builder.email;
        this.phone = builder.phone;
        this.role = builder.role;
        this.empStatus = builder.empStatus;
    }

    public enum EmpStatus {ACTIVE, SUSPENDED, TERMINATED}

    public String getEmpID() {
        return empID;
    }

    public void setEmpID(String empID) {
        this.empID = empID;
    }

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public EmpStatus getEmpStatus() {
        return empStatus;
    }

    public void setEmpStatus(EmpStatus empStatus) {
        this.empStatus = empStatus;
    }

    @Override
    public String toString() {
        return "Employee{" + "Employee ID='" + empID + '\'' + ", First name='" + fName + '\'' + ", Last name='" + lName + '\'' + ", Salary=" + salary + ", Email address='" + email + '\'' + ", Phone number='" + phone + '\'' + ", Role='" + role + '\'' + ", Status=" + empStatus + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee)) {
            return false;
        }
        Employee employee = (Employee) o;
        return Objects.equals(empID, employee.empID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(empID);
    }

    public static class Builder {
        private String empID;
        private String fName;
        private String lName;
        private double salary;
        private String email;
        private String phone;
        private String role;
        private EmpStatus empStatus;

        public Builder setEmpId(String empID) {
            this.empID = empID;
            return this;
        }

        public Builder setFName(String fName) {
            this.fName = fName;
            return this;
        }

        public Builder setLName(String lName) {
            this.lName = lName;
            return this;
        }

        public Builder setSalary(double salary) {
            this.salary = salary;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder setEmpStatus(EmpStatus empStatus) {
            this.empStatus = empStatus;
            return this;
        }

        public Builder copy(Employee employee) {
            this.empID = employee.empID;
            this.fName = employee.fName;
            this.lName = employee.lName;
            this.salary = employee.salary;
            this.email = employee.email;
            this.phone = employee.phone;
            this.role = employee.role;
            this.empStatus = employee.empStatus;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}
