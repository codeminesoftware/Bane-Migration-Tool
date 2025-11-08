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
        registerLegacyClient(new Client(1L, "Feddasa Bote", "feddasa.bote@example.com", false));
        registerLegacyClient(new Client(2L, "Ebissa Chemeda", "ebissa.chemeda@example.com", false));
        registerLegacyClient(new Client(3L, "Mintesnot Sibhatu", "mintesnot.sibhatu@example.com", false));
        registerLegacyClient(new Client(4L, "Esayas Niguse", "esayas.niguse@example.com", false));
        registerLegacyClient(new Client(5L, "Efrem Shimels", "efrem.shimels@example.com", true));
        registerLegacyClient(new Client(6L, "Addisu Birile", "addisu.birile@example.com", true));
        registerLegacyClient(new Client(7L, "Chala Demesa", "chala.demesa@example.com", false));
    }

    private void registerLegacyClient(Client client) {
        legacyClients.put(client.getId(), client);

        if (client.isMigrated()) {
            Client migratedCopy = new Client(client.getId(), client.getName(), client.getEmail(), true);
            newClients.put(migratedCopy.getId(), migratedCopy);
        }
    }
}

