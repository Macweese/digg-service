<script setup lang="ts">
const props = defineProps<{ visible: boolean }>();
const emit = defineEmits<{ (e: 'cancel'): void; (e: 'confirm'): void }>();
</script>

<template>
  <div v-if="visible" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4" @click="emit('cancel')">
    <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-sm" @click.stop>
      <div class="p-6 text-center" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-12 w-12 text-red-500 mx-auto"><path d="m21.73 18-8-14a2 2 0 0 0-3.46 0l-8 14A2 2 0 0 0 4 21h16a 2 2 0 0 0 1.73-3Z"></path><path d="M12 9v4"></path><path d="M12 17h.01"></path></svg>
        <h3 class="mt-5 mb-2 text-lg font-semibold text-slate-900 dark:text-white">Delete User</h3>
        <p class="text-sm text-slate-500 dark:text-slate-400">Are you sure you want to delete this user? This action cannot be undone.</p>
      </div>
      <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-center gap-3 rounded-b-lg">
        <button @click="emit('cancel')" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2  focus:ring-blue-500/80" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Cancel
        </button>
        <button @click="emit('confirm')" class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2  focus:ring-red-500/60" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Confirm Delete
        </button>
      </div>
    </div>
  </div>
</template>