<script setup lang="ts">
import { onMounted } from 'vue';
import type { User } from './types';

import AppHeader from './components/AppHeader.vue';
import ErrorRailAlert from './components/ErrorRailAlert.vue';
import UsersTable from './components/UsersTable.vue';
import PaginationControls from './components/PaginationControls.vue';
import UserFormModal from './components/UserFormModal.vue';
import DeleteConfirmModal from './components/DeleteConfirmModal.vue';
import ToastMessage from './components/ToastMessage.vue';

import { useUsers } from './composables/useUsers';
import { useWebSocketUsers } from './composables/useWebSocketUsers';
import { saveUser, deleteUser as apiDeleteUser } from './services/UserService';

// Core state via composable
const {
  usersPage,
  searchTerm,
  itemsPerPage,
  currentPage,
  isLoading,
  showLoading,
  errorMessage,
  toastMessage,
  loadUsers,
  nextPage,
  prevPage,
  isModalOpen,
  editingUser,
  openAddModal,
  openEditModal,
  closeModal,
  isDeleteConfirmOpen,
  userToDelete,
  openDeleteConfirm,
  closeDeleteConfirm,
} = useUsers();

async function handleSaveUser(user: User) {
  try {
    await saveUser(user);
    toastMessage.value = `User ${user.id ? 'updated' : 'added'}`;
    closeModal();
    await loadUsers();
  } catch (e: any) {
    errorMessage.value = e?.message ? 'Failed to save user. ' + e.message : 'Failed to save user.';
  }
}

async function confirmDelete() {
  try {
    if (userToDelete.value == null) return;
    await apiDeleteUser(userToDelete.value);
    toastMessage.value = 'User deleted';
    await loadUsers();
  } catch (e: any) {
    errorMessage.value = e?.message ? 'Failed to delete user. ' + e.message : 'Failed to delete user.';
  } finally {
    closeDeleteConfirm();
  }
}

// WebSocket that reloads users on events
useWebSocketUsers(async () => {
  await loadUsers();
});

onMounted(() => {
  loadUsers();
});
</script>

<template>
  <header class="bg-white dark:bg-slate-800/50 backdrop-blur-sm shadow-sm sticky top-0 z-10">
    <AppHeader />
  </header>

  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-white dark:bg-slate-800 p-4 sm:p-6 rounded-lg shadow-sm">
      <ErrorRailAlert
        :message="errorMessage"
        @cleared="errorMessage = ''"
      />

      <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6">
        <div class="relative w-full sm:max-w-xs">
          <div class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="11" cy="11" r="8"></circle><line x1="21" x2="16.65" y1="21" y2="16.65"></line></svg>
          </div>
          <input
            v-model="searchTerm"
            type="text"
            placeholder="Search"
            class="w-full pl-10 pr-4 py-2 border border-slate-300 dark:border-slate-600 rounded-md bg-slate-100 dark:bg-slate-700/50 focus:outline-none focus-visible:bg-slate-900/30 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-500 focus-visible:ring-offset-0"
            :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
          />
        </div>

        <div class="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
          <div class="relative">
            <select
              v-model.number="itemsPerPage"
              class="px-3 py-2 text-sm rounded-md bg-slate-100 dark:bg-slate-700/50 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-200"
              :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
            >
              <option :value="10">10 per page</option>
              <option :value="25">25 per page</option>
              <option :value="50">50 per page</option>
            </select>
          </div>

          <button
            @click="openAddModal"
            class="flex items-center justify-center px-4 py-1 bg-gradient-to-r from-teal-500/40 from-10% via-sky-500/30 via-30% to-emerald-500/30 to-90% bg-indigo-600 text-white font-semibold rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-slate-50 dark:focus:ring-offset-slate-900 transition-colors"
            :style="{ userSelect: 'none', WebkitUserSelect: 'none'}"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <line x1="12" y1="5" x2="12" y2="19" stroke-width="2"></line>
              <line x1="5" y1="12" x2="19" y2="12" stroke-width="2"></line>
            </svg>
            <span class="ml-2">Add User</span>
          </button>
        </div>
      </div>

      <div class="overflow-x-auto" :style="{ minHeight: '600px' }">
        <div v-if="showLoading" class="text-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500 mx-auto"></div>
          <p class="mt-4 text-slate-500 dark:text-slate-400">Loading users...</p>
        </div>

        <UsersTable
          v-else-if="usersPage.content.length > 0"
          :users="usersPage.content"
          @edit="openEditModal"
          @delete="openDeleteConfirm"
        />

        <div v-else-if="!errorMessage" class="text-center py-12">
          <h3 class="text-lg font-semibold text-slate-700 dark:text-slate-300">
            {{ searchTerm ? `No results for "${searchTerm}"` : 'No Users Found' }}
          </h3>
          <p class="mt-1 text-slate-500 dark:text-slate-400">
            {{ searchTerm ? 'Try searching for something else.' : 'Click "Add User" to get started.' }}
          </p>
        </div>
      </div>

      <PaginationControls
        v-if="usersPage.totalPages > 1"
        :current-page="currentPage"
        :total-pages="usersPage.totalPages"
        :total-elements="usersPage.totalElements"
        @prev="prevPage"
        @next="nextPage"
      />
    </div>
  </main>

  <UserFormModal
    :visible="isModalOpen"
    :user="editingUser"
    @close="closeModal"
    @save="handleSaveUser"
  />

  <DeleteConfirmModal
    :visible="isDeleteConfirmOpen"
    @cancel="closeDeleteConfirm"
    @confirm="confirmDelete"
  />

  <ToastMessage :message="toastMessage" />
</template>

<style>
select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.5rem center;
  background-size: 1em;
}
</style>