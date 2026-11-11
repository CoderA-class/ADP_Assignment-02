package za.ac.cput.adp_assignment02.dto;

/*
 * ChangePasswordRequest.java
 * What the app sends to change your own password.
 */
public record ChangePasswordRequest(String employeeId, String currentPassword, String newPassword) {
}
