package ru.itmo.infosec1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import ru.itmo.infosec1.dto.DataResponse;
import ru.itmo.infosec1.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class DataController {
    private final UserRepository users;

    public DataController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/data")
    public DataResponse getData() {
        return new DataResponse(users.findAll().stream()
                .map(user -> HtmlUtils.htmlEscape(user.getUsername()))
                .sorted()
                .toList());
    }
}
