package za.ac.cput.adp_assignment02.dto;

public record ChangePasswordRequest(String employeeId, String currentPassword, String newPassword) {
}
