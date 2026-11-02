package za.ac.cput.adp_assignment02.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;
/* * Employee.java
* Employee POJO class
* Author: Robyn Dominique Stevens (222201789)
* Date: 19 June 2026
* Updated: auto-generated employee ID (EMP-0001), login fields (password + access level),
*          optional image and hire date. The password is never sent back to the frontend.
*/

@Entity
@Table(name = "employee")
public class Employee {
    @Id
    @Column(name = "empID")
    private String empID;
    @Column(nullable = false)
    private String fName;
    @Column(nullable = false)
    private String lName;
    @Column(nullable = false)
    private double salary;
    @Column(nullable = false)
    private String email;
    private String phone;
    /* Job title, e.g. "Store Manager", "Sales Assistant" */
    private String role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmpStatus empStatus;

    /* What the employee can do in the app: ADMIN or EMPLOYEE */
    @Enumerated(EnumType.STRING)
    private AccessLevel accessLevel;

    /* BCrypt hash of the employee's password */
    private String password;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    private LocalDate hireDate;

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
        this.accessLevel = builder.accessLevel;
        this.password = builder.password;
        this.imageUrl = builder.imageUrl;
        this.hireDate = builder.hireDate;
    }

    public enum EmpStatus {ACTIVE, SUSPENDED, TERMINATED}

    public enum AccessLevel {ADMIN, EMPLOYEE}

    @JsonProperty("empID")
    public String getEmpID() {
        return empID;
    }

    @JsonProperty("empID")
    public void setEmpID(String empID) {
        this.empID = empID;
    }

    @JsonProperty("firstName")
    public String getFName() {
        return fName;
    }

    @JsonProperty("firstName")
    public void setFName(String fName) {
        this.fName = fName;
    }

    @JsonProperty("lastName")
    public String getLName() {
        return lName;
    }

    @JsonProperty("lastName")
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

    public AccessLevel getAccessLevel() {
        return accessLevel == null ? AccessLevel.EMPLOYEE : accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /* Write-only: accepted from the frontend, never returned to it */
    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getFullName() {
        return ((fName == null ? "" : fName) + " " + (lName == null ? "" : lName)).trim();
    }

    @Override
    public String toString() {
        return "Employee{" + "Employee ID='" + empID + '\'' + ", First name='" + fName + '\'' + ", Last name='" + lName + '\'' + ", Salary=" + salary + ", Email address='" + email + '\'' + ", Phone number='" + phone + '\'' + ", Role='" + role + '\'' + ", Status=" + empStatus + ", Access=" + getAccessLevel() + '}';
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
        private AccessLevel accessLevel;
        private String password;
        private String imageUrl;
        private LocalDate hireDate;

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

        public Builder setAccessLevel(AccessLevel accessLevel) {
            this.accessLevel = accessLevel;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setHireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
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
            this.accessLevel = employee.accessLevel;
            this.password = employee.password;
            this.imageUrl = employee.imageUrl;
            this.hireDate = employee.hireDate;
            return this;
        }

        public Employee build() {
            return new Employee(this);
        }
    }
}
