package gr.uni.cinema.cinemamanagement.dto;

public record ChangePasswordRequest(

        String oldPassword,
        String newPassword

) {
}