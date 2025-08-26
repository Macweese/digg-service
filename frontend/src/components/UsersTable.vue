<script lang="ts" setup>
import type {User} from '../types';

defineProps<{
  users: User[]
}>();
const emit = defineEmits<{
  (e: 'edit', user: User): void
  (e: 'delete', id: User['id']): void
}>();
</script>

<template>
  <table class="w-full text-sm text-left text-slate-500 dark:text-slate-400 fixed-table">
    <thead :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
           class="text-xs text-slate-700 uppercase bg-slate-50 dark:bg-slate-700 dark:text-slate-300">
    <tr>
      <th class="px-6 py-3" scope="col">Name</th>
      <th class="px-6 py-3 hidden md:table-cell" scope="col">Contact</th>
      <th class="px-6 py-3 hidden lg:table-cell" scope="col">Address</th>
      <th class="px-6 py-3 text-right" scope="col">Actions</th>
    </tr>
    </thead>
    <tbody>
    <tr
        v-for="user in users"
        :key="user.id"
        class="bg-white dark:bg-slate-800 border-b dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
    >
      <td class="px-6 py-4 font-medium text-slate-900 dark:text-white whitespace-nowrap">{{ user.name }}</td>
      <td class="px-6 py-4 hidden md:table-cell">
        <div>{{ user.email }}</div>
        <div class="text-xs text-slate-500">{{ user.telephone }}</div>
      </td>
      <td class="px-6 py-4 hidden lg:table-cell">{{ user.address }}</td>
      <td class="px-6 py-4">
        <div class="flex justify-end items-center gap-2">
          <button class="p-2 text-slate-500 hover:text-blue-600 dark:hover:text-blue-400 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  @click="emit('edit', user)">
            <svg class="h-4 w-4" fill="none" height="24" stroke="currentColor" stroke-linecap="round"
                 stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
              <path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"></path>
            </svg>
          </button>
          <button class="p-2 text-slate-500 hover:text-red-600 dark:hover:text-red-400 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
                  @click="emit('delete', user.id)">
            <svg class="h-4 w-4" fill="none" height="24" stroke="currentColor" stroke-linecap="round"
                 stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
              <path d="M3 6h18"></path>
              <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
              <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
            </svg>
          </button>
        </div>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<style>
.fixed-table {
  table-layout: fixed;
  width: 100%;
}

.fixed-table th:nth-child(1), .fixed-table td:nth-child(1) {
  width: 20%;
}

.fixed-table th:nth-child(2), .fixed-table td:nth-child(2) {
  width: 30%;
}

.fixed-table th:nth-child(3), .fixed-table td:nth-child(3) {
  width: 35%;
}

.fixed-table th:nth-child(4), .fixed-table td:nth-child(4) {
  width: 15%;
}

.fixed-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fixed-table td:nth-child(3) {
  white-space: normal;
  word-wrap: break-word;
}
</style>