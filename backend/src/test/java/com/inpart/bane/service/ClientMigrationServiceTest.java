package com.inpart.bane.service;

import com.inpart.bane.exception.ClientAlreadyMigratedException;
import com.inpart.bane.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientMigrationServiceTest {

    private ClientMigrationService service;

    @BeforeEach
    void setUp() {
        service = new ClientMigrationService();
    }

    @Test
    void shouldLoadSeedLegacyClients() {
        List<Client> legacyClients = service.getLegacyClients();

        assertThat(legacyClients).hasSize(7);
        assertThat(legacyClients).filteredOn(Client::isMigrated).hasSize(2);
        assertThat(legacyClients).filteredOn(client -> !client.isMigrated()).hasSize(5);
        assertThat(service.getNewClients()).hasSize(2);
    }

    @Test
    void shouldMigrateClientAndMarkAsMigrated() {
        Client migrated = service.migrateClient(1L);

        assertThat(migrated.isMigrated()).isTrue();
        assertThat(service.getNewClients()).extracting(Client::getId).contains(1L);
        assertThat(service.getLegacyClients()).filteredOn(Client::getId, 1L)
                .first()
                .extracting(Client::isMigrated)
                .isEqualTo(true);
    }

    @Test
    void shouldNotMigrateSameClientTwice() {
        service.migrateClient(2L);

        assertThrows(ClientAlreadyMigratedException.class, () -> service.migrateClient(2L));
    }
}

