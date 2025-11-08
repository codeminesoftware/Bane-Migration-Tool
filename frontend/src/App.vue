<template>
  <v-app>
    <v-app-bar color="primary" elevation="2" class="app-bar">
      <template #prepend>
        <v-icon size="32" class="ml-4">mdi-database-sync</v-icon>
      </template>
      <v-toolbar-title class="text-h5 font-weight-bold">
        Bane Migration Dashboard
      </v-toolbar-title>
      <template #append>
        <v-btn icon="mdi-refresh" variant="text" :loading="isRefreshing" @click="refreshData" class="mr-2">
          <v-icon>mdi-refresh</v-icon>
          <v-tooltip activator="parent" location="bottom">Refresh data</v-tooltip>
        </v-btn>
      </template>
    </v-app-bar>

    <v-main class="main-gradient">
      <v-container class="py-8" max-width="1400px">
        <!-- Stats Cards -->
        <v-row class="mb-6">
          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="4">
              <v-card-text class="text-center pa-6">
                <v-icon size="48" color="primary" class="mb-3">mdi-account-group</v-icon>
                <div class="text-h4 font-weight-bold mb-1">{{ legacyClients.length }}</div>
                <div class="text-body-2 text-medium-emphasis">Total Legacy Clients</div>
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="4">
              <v-card-text class="text-center pa-6">
                <v-icon size="48" color="success" class="mb-3">mdi-check-circle</v-icon>
                <div class="text-h4 font-weight-bold mb-1 text-success">{{ newClients.length }}</div>
                <div class="text-body-2 text-medium-emphasis">Migrated Clients</div>
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="4">
              <v-card-text class="text-center pa-6">
                <v-icon size="48" color="warning" class="mb-3">mdi-clock-outline</v-icon>
                <div class="text-h4 font-weight-bold mb-1 text-warning">{{ pendingCount }}</div>
                <div class="text-body-2 text-medium-emphasis">Pending Migration</div>
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="4">
              <v-card-text class="text-center pa-6">
                <v-icon size="48" color="info" class="mb-3">mdi-chart-line</v-icon>
                <div class="text-h4 font-weight-bold mb-1 text-info">{{ migrationRate }}%</div>
                <div class="text-body-2 text-medium-emphasis">Migration Rate</div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <v-row>
          <v-col cols="12" lg="8">
            <v-card elevation="6" class="content-card">
              <v-card-title class="d-flex align-center pa-5 bg-surface-variant">
                <v-icon class="mr-3" color="primary">mdi-database</v-icon>
                <span class="text-h5">Legacy Clients</span>
              </v-card-title>
              <v-divider></v-divider>
              <v-card-text class="pa-0">
                <v-data-table
                  :headers="legacyHeaders"
                  :items="legacyTableItems"
                  :loading="legacyLoading"
                  density="comfortable"
                  item-key="id"
                  :items-per-page="10"
                  class="legacy-table"
                >
                  <template #item.id="{ item }">
                    <v-chip size="small" variant="tonal" color="primary">
                      #{{ item.id }}
                    </v-chip>
                  </template>
                  <template #item.name="{ item }">
                    <div class="d-flex align-center">
                      <v-avatar size="32" :color="item.migrated ? 'success' : 'grey'" class="mr-3">
                        <span class="text-caption">{{ initials(item.name) }}</span>
                      </v-avatar>
                      <span class="font-weight-medium">{{ item.name }}</span>
                    </div>
                  </template>
                  <template #item.status="{ item }">
                    <v-chip
                      :color="item.migrated ? 'success' : 'warning'"
                      :prepend-icon="item.migrated ? 'mdi-check-circle' : 'mdi-clock-outline'"
                      variant="flat"
                      size="small"
                      class="status-chip"
                    >
                      {{ item.migrated ? 'Migrated' : 'Pending' }}
                    </v-chip>
                  </template>
                  <template #item.actions="{ item }">
                    <v-btn
                      :color="item.migrated ? 'success' : 'primary'"
                      :variant="item.migrated ? 'tonal' : 'flat'"
                      :disabled="item.migrated || isMigrating(item.id)"
                      :loading="isMigrating(item.id)"
                      :prepend-icon="item.migrated ? 'mdi-check' : 'mdi-arrow-right-bold'"
                      @click="migrate(item.id)"
                      size="small"
                      class="migrate-btn"
                    >
                      {{ item.migrated ? 'Done' : 'Migrate' }}
                    </v-btn>
                  </template>
                  <template #no-data>
                    <v-alert type="info" variant="tonal" class="ma-4">
                      No legacy clients found.
                    </v-alert>
                  </template>
                </v-data-table>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" lg="4">
            <v-card elevation="6" class="content-card sticky-card">
              <v-card-title class="d-flex align-center pa-5 bg-success">
                <v-icon class="mr-3" color="white">mdi-cloud-check</v-icon>
                <span class="text-h5 text-white">NewConnect System</span>
              </v-card-title>
              <v-divider></v-divider>
              <v-card-text class="pa-5">
                <div class="text-center mb-5">
                  <v-progress-circular
                    :model-value="migrationRate"
                    :size="120"
                    :width="12"
                    color="success"
                    class="progress-circle"
                  >
                    <div class="text-h4 font-weight-bold">{{ migrationRate }}%</div>
                  </v-progress-circular>
                  <div class="text-body-1 mt-3 text-medium-emphasis">Migration Progress</div>
                </div>

                <v-divider class="my-4"></v-divider>

                <transition-group name="list" tag="div">
                  <v-card
                    v-for="client in newClients"
                    :key="client.id"
                    class="mb-3 client-card"
                    variant="tonal"
                    color="success"
                  >
                    <v-card-text class="pa-4">
                      <div class="d-flex align-center">
                        <v-avatar size="48" color="success" class="mr-4">
                          <span class="text-h6">{{ initials(client.name) }}</span>
                        </v-avatar>
                        <div class="flex-grow-1">
                          <div class="text-subtitle-1 font-weight-bold">{{ client.name }}</div>
                          <div class="text-caption text-medium-emphasis">{{ client.email }}</div>
                        </div>
                        <v-icon color="success">mdi-check-circle</v-icon>
                      </div>
                    </v-card-text>
                  </v-card>
                </transition-group>

                <v-alert v-if="!newClients.length" variant="tonal" type="info" class="mt-4">
                  <template #prepend>
                    <v-icon>mdi-information</v-icon>
                  </template>
                  No clients migrated yet. Start migrating from the Legacy Clients table!
                </v-alert>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>

      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.color"
        :timeout="3500"
        location="top"
        class="custom-snackbar"
      >
        <div class="d-flex align-center">
          <v-icon class="mr-3">
            {{ snackbar.color === 'success' ? 'mdi-check-circle' : 'mdi-alert-circle' }}
          </v-icon>
          <span>{{ snackbar.message }}</span>
        </div>
        <template #actions>
          <v-btn icon="mdi-close" variant="text" @click="snackbar.show = false"></v-btn>
        </template>
      </v-snackbar>

      <!-- Confetti Animation Container -->
      <div ref="confettiContainer" class="confetti-container"></div>
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
const confettiContainer = ref(null);

const snackbar = reactive({
  show: false,
  message: '',
  color: 'success'
});

const legacyHeaders = [
  { title: 'ID', key: 'id', sortable: false, width: '80px' },
  { title: 'Name', key: 'name' },
  { title: 'Email', key: 'email' },
  { title: 'Status', key: 'status', sortable: false, width: '140px' },
  { title: 'Actions', key: 'actions', sortable: false, width: '140px' }
];

const legacyTableItems = computed(() =>
  legacyClients.value.map((client) => ({
    ...client,
    status: client.migrated ? 'Migrated' : 'Pending'
  }))
);

const pendingCount = computed(() =>
  legacyClients.value.filter((client) => !client.migrated).length
);

const migrationRate = computed(() => {
  if (legacyClients.value.length === 0) return 0;
  return Math.round((newClients.value.length / legacyClients.value.length) * 100);
});

const showSnackbar = (message, color = 'success') => {
  snackbar.show = false;
  snackbar.message = message;
  snackbar.color = color;
  requestAnimationFrame(() => {
    snackbar.show = true;
  });
};

const createConfetti = () => {
  if (!confettiContainer.value) return;

  const colors = ['#4CAF50', '#2196F3', '#FF9800', '#E91E63', '#9C27B0'];
  const confettiCount = 50;

  for (let i = 0; i < confettiCount; i++) {
    const confetti = document.createElement('div');
    confetti.className = 'confetti';
    confetti.style.left = Math.random() * 100 + '%';
    confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
    confetti.style.animationDelay = Math.random() * 0.5 + 's';
    confetti.style.animationDuration = Math.random() * 2 + 2 + 's';
    confettiContainer.value.appendChild(confetti);

    setTimeout(() => confetti.remove(), 4000);
  }
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
    createConfetti();
    showSnackbar(`✨ Client ${data.name} migrated successfully!`);
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
.main-gradient {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.app-bar {
  backdrop-filter: blur(10px);
}

.stat-card {
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15) !important;
}

.content-card {
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

.sticky-card {
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

.progress-circle {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.client-card {
  transition: all 0.3s ease;
  border-radius: 12px;
}

.client-card:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.migrate-btn {
  transition: all 0.3s ease;
}

.migrate-btn:hover {
  transform: scale(1.05);
}

.status-chip {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.custom-snackbar {
  font-weight: 500;
}

/* List transition animations */
.list-enter-active {
  animation: slideIn 0.5s ease-out;
}

.list-leave-active {
  animation: slideOut 0.3s ease-in;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  to {
    opacity: 0;
    transform: translateX(30px) scale(0.9);
  }
}

/* Confetti animation */
.confetti-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
  overflow: hidden;
}

.confetti {
  position: absolute;
  width: 10px;
  height: 10px;
  top: -10px;
  opacity: 0;
  animation: confettiFall 3s ease-out forwards;
}

@keyframes confettiFall {
  0% {
    opacity: 1;
    transform: translateY(0) rotateZ(0deg);
  }
  100% {
    opacity: 0;
    transform: translateY(100vh) rotateZ(720deg);
  }
}

/* Table hover effects */
:deep(.legacy-table tbody tr) {
  transition: all 0.2s ease;
}

:deep(.legacy-table tbody tr:hover) {
  background-color: rgba(103, 126, 234, 0.05);
  transform: scale(1.01);
}

/* Smooth scrollbar */
.sticky-card::-webkit-scrollbar {
  width: 8px;
}

.sticky-card::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.sticky-card::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.sticky-card::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* Loading shimmer effect */
@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

/* Responsive adjustments */
@media (max-width: 960px) {
  .sticky-card {
    position: relative;
    top: 0;
    max-height: none;
  }
}
</style>

