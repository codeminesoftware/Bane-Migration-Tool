package com.inpart.bane.service;

import com.inpart.bane.exception.ClientAlreadyMigratedException;
import com.inpart.bane.exception.ClientNotFoundException;
import com.inpart.bane.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClientMigrationService {

    private static final Logger log = LoggerFactory.getLogger(ClientMigrationService.class);

    private final Map<Long, Client> legacyClients = new LinkedHashMap<>();
    private final Map<Long, Client> newClients = new LinkedHashMap<>();

    public ClientMigrationService() {
        seedClients();
    }

    public List<Client> getLegacyClients() {
        return new ArrayList<>(legacyClients.values());
    }

    public List<Client> getNewClients() {
        return new ArrayList<>(newClients.values());
    }

    public Client migrateClient(Long id) {
        Client client = legacyClients.get(id);
        if (client == null) {
            throw new ClientNotFoundException(id);
        }
        if (client.isMigrated()) {
            throw new ClientAlreadyMigratedException(id);
        }

        client.setMigrated(true);
        Client migratedCopy = new Client(client.getId(), client.getName(), client.getEmail(), true);
        newClients.put(migratedCopy.getId(), migratedCopy);

        log.info("Migrated client {} successfully", id);

        return migratedCopy;
    }

    private void seedClients() {
        legacyClients.put(1L, new Client(1L, "Alice Johnson", "alice.johnson@example.com", false));
        legacyClients.put(2L, new Client(2L, "Bruno Silva", "bruno.silva@example.com", false));
        legacyClients.put(3L, new Client(3L, "Chen Li", "chen.li@example.com", false));
        legacyClients.put(4L, new Client(4L, "Diana Prince", "diana.prince@example.com", false));
    }
}

