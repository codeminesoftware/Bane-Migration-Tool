package com.inpart.bane.controller;

import com.inpart.bane.model.Client;
import com.inpart.bane.service.ClientMigrationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    private final ClientMigrationService clientMigrationService;

    public ClientController(ClientMigrationService clientMigrationService) {
        this.clientMigrationService = clientMigrationService;
    }

    @GetMapping("/legacy/clients")
    public List<Client> getLegacyClients() {
        return clientMigrationService.getLegacyClients();
    }

    @PostMapping("/migrate/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Client migrateClient(@PathVariable Long id) {
        return clientMigrationService.migrateClient(id);
    }

    @GetMapping("/new/clients")
    public List<Client> getNewClients() {
        return clientMigrationService.getNewClients();
    }
}

