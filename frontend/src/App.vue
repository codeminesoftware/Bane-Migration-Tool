<template>
  <v-app>
    <v-main>
      <v-container class="py-8" max-width="1200px">
        <v-row>
          <v-col cols="12">
            <v-sheet elevation="2" class="pa-6">
              <v-row align="center" no-gutters class="mb-4">
                <v-col>
                  <h1 class="text-h4 mb-1">Bane Migration Dashboard</h1>
                  <p class="text-body-2 mb-0">
                    Migrate clients from LegacyCRM to NewConnect with a single click.
                  </p>
                </v-col>
                <v-col cols="auto">
                  <v-btn icon="mdi-refresh" variant="text" :loading="isRefreshing" @click="refreshData">
                    <v-tooltip activator="parent" location="bottom">Refresh data</v-tooltip>
                  </v-btn>
                </v-col>
              </v-row>

              <v-divider class="mb-6"></v-divider>

              <v-row>
                <v-col cols="12" md="8">
                  <h2 class="text-h5 mb-3">Legacy Clients</h2>
                  <v-data-table
                    :headers="legacyHeaders"
                    :items="legacyTableItems"
                    :loading="legacyLoading"
                    density="comfortable"
                    item-key="id"
                    class="elevation-1"
                    :items-per-page="5"
                  >
                    <template #item.status="{ item }">
                      <v-chip
                        :color="item.migrated ? 'success' : 'warning'"
                        :variant="item.migrated ? 'flat' : 'tonal'"
                        size="small"
                      >
                        {{ item.migrated ? 'Migrated' : 'Pending' }}
                      </v-chip>
                    </template>
                    <template #item.actions="{ item }">
                      <v-btn
                        color="primary"
                        variant="flat"
                        :disabled="item.migrated || isMigrating(item.id)"
                        :loading="isMigrating(item.id)"
                        @click="migrate(item.id)"
                      >
                        Migrate
                      </v-btn>
                    </template>
                    <template #no-data>
                      <v-alert type="info" variant="tonal">
                        No legacy clients found.
                      </v-alert>
                    </template>
                  </v-data-table>
                </v-col>

                <v-col cols="12" md="4">
                  <h2 class="text-h5 mb-3">New System Clients</h2>
                  <v-card class="mb-3" variant="outlined">
                    <v-card-text class="text-center">
                      <div class="text-h4 font-weight-bold">{{ newClients.length }}</div>
                      <div class="text-body-2">clients migrated</div>
                    </v-card-text>
                  </v-card>
                  <v-list
                    v-if="newClients.length"
                    lines="two"
                    border
                    rounded
                    density="comfortable"
                    class="overflow-auto"
                    style="max-height: 360px"
                  >
                    <v-list-item v-for="client in newClients" :key="client.id">
                      <template #prepend>
                        <v-avatar color="primary" variant="tonal">
                          {{ initials(client.name) }}
                        </v-avatar>
                      </template>
                      <v-list-item-title>{{ client.name }}</v-list-item-title>
                      <v-list-item-subtitle>{{ client.email }}</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                  <v-alert v-else :loading="newLoading" variant="tonal" type="info">
                    No clients migrated yet.
                  </v-alert>
                </v-col>
              </v-row>
            </v-sheet>
          </v-col>
        </v-row>
      </v-container>

      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3500">
        {{ snackbar.message }}
        <template #actions>
          <v-btn icon="mdi-close" variant="text" @click="snackbar.show = false"></v-btn>
        </template>
      </v-snackbar>
    </v-main>
  </v-app>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import {
  fetchLegacyClients,
  fetchNewClients,
  migrateClient as migrateClientApi
} from './api/clientApi';

const legacyClients = ref([]);
const newClients = ref([]);
const legacyLoading = ref(false);
const newLoading = ref(false);
const migratingIds = ref([]);
const isRefreshing = ref(false);

const snackbar = reactive({
  show: false,
  message: '',
  color: 'success'
});

const legacyHeaders = [
  { title: 'ID', key: 'id', sortable: false, width: '70px' },
  { title: 'Name', key: 'name' },
  { title: 'Email', key: 'email' },
  { title: 'Status', key: 'status', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, width: '160px' }
];

const legacyTableItems = computed(() =>
  legacyClients.value.map((client) => ({
    ...client,
    status: client.migrated ? 'Migrated' : 'Pending'
  }))
);

const showSnackbar = (message, color = 'success') => {
  snackbar.show = false;
  snackbar.message = message;
  snackbar.color = color;
  requestAnimationFrame(() => {
    snackbar.show = true;
  });
};

const loadLegacyClients = async () => {
  legacyLoading.value = true;
  try {
    const { data } = await fetchLegacyClients();
    legacyClients.value = data;
  } catch (error) {
    showSnackbar(extractErrorMessage(error, 'Failed to load legacy clients'), 'error');
  } finally {
    legacyLoading.value = false;
  }
};

const loadNewClients = async () => {
  newLoading.value = true;
  try {
    const { data } = await fetchNewClients();
    newClients.value = data;
  } catch (error) {
    showSnackbar(extractErrorMessage(error, 'Failed to load migrated clients'), 'error');
  } finally {
    newLoading.value = false;
  }
};

const refreshData = async () => {
  isRefreshing.value = true;
  await Promise.all([loadLegacyClients(), loadNewClients()]);
  isRefreshing.value = false;
};

const setMigrating = (id, active) => {
  if (active) {
    if (!migratingIds.value.includes(id)) {
      migratingIds.value = [...migratingIds.value, id];
    }
  } else {
    migratingIds.value = migratingIds.value.filter((currentId) => currentId !== id);
  }
};

const migrate = async (id) => {
  setMigrating(id, true);
  try {
    const { data } = await migrateClientApi(id);
    legacyClients.value = legacyClients.value.map((client) =>
      client.id === id ? { ...client, migrated: true } : client
    );
    const alreadyExists = newClients.value.some((client) => client.id === id);
    if (!alreadyExists) {
      newClients.value = [...newClients.value, data];
    }
    showSnackbar(`Client ${id} migrated successfully`);
  } catch (error) {
    showSnackbar(extractErrorMessage(error, `Failed to migrate client ${id}`), 'error');
  } finally {
    setMigrating(id, false);
  }
};

const isMigrating = (id) => migratingIds.value.includes(id);

const initials = (name) =>
  name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

const extractErrorMessage = (error, fallback) =>
  error?.response?.data?.message ?? error?.message ?? fallback;

onMounted(() => {
  refreshData();
});
</script>

<style scoped>
h1,
h2 {
  font-weight: 500;
}
</style>

