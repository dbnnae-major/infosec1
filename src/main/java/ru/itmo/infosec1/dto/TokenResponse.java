package ru.itmo.infosec1.dto;

public record TokenResponse(String token, String type, long expiresInSeconds) {
}
