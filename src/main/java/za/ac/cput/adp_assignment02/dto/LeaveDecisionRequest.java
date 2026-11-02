package za.ac.cput.adp_assignment02.dto;

/* reviewerId = the admin's employee ID, comment = optional note to the employee */
public record LeaveDecisionRequest(String reviewerId, String comment) {
}
