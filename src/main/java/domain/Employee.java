package domain;

public class Employee {

    private final String empID;
    private final String fName;
    private final String lName;
    private final double salary;
    private final String email;
    private final String phone;
    private final String role;
    private final EmpStatus empStatus;

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

    public enum EmpStatus {
        ACTIVE, SUSPENDED, TERMINATED
    }

    // Builder class
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
            this.empID = this.empID;
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

        // Build method
        public Employee build() {
            return new Employee(this);
        }
    }

    public String getEmpID() {
        return empID;
    }

    public String getFName() {
        return fName;
    }

    public String getLName() {
        return lName;
    }

    public double getSalary() {
        return salary;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public EmpStatus getEmpStatus() {
        return empStatus;
    }
}