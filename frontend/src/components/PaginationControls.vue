<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
}>();
const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
}>();
</script>

<template>
  <nav class="flex items-center justify-between pt-4" aria-label="Table navigation">
    <span class="text-sm font-normal text-slate-500 dark:text-slate-400" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
      Page <span class="font-semibold text-slate-900 dark:text-white">{{ currentPage + 1 }}</span>
      of <span class="font-semibold text-slate-900 dark:text-white">{{ totalPages }}</span>
      (<span class="font-semibold text-slate-900 dark:text-white">{{ totalElements }}</span> entries)
    </span>
    <ul class="inline-flex items-center -space-x-px">
      <li>
        <button @click="emit('prev')" :disabled="currentPage === 0" class="px-3 py-2 ml-0 leading-tight text-slate-500 bg-white border border-slate-300 rounded-l-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-20 disabled:cursor-not-allowed" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Back
        </button>
      </li>
      <li>
        <button @click="emit('next')" :disabled="currentPage >= totalPages - 1" class="px-3 py-2 leading-tight text-slate-500 bg-white border border-slate-300 rounded-r-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-20 disabled:cursor-not-allowed" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Next
        </button>
      </li>
    </ul>
  </nav>
</template>