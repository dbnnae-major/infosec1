package ru.itmo.infosec1.dto;

import java.util.List;

public record DataResponse(List<String> users) {

    public DataResponse {
        users = List.copyOf(users);
    }

    @Override
    public List<String> users() {
        return List.copyOf(users);
    }
}