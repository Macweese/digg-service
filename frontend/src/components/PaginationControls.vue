<script lang="ts" setup>
defineProps<{
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
  <nav aria-label="Table navigation" class="flex items-center justify-between pt-4">
    <span :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
          class="text-sm font-normal text-slate-500 dark:text-slate-400">
      Page <span class="font-semibold text-slate-900 dark:text-white">{{ currentPage + 1 }}</span>
      of <span class="font-semibold text-slate-900 dark:text-white">{{ totalPages }}</span>
      (<span class="font-semibold text-slate-900 dark:text-white">{{ totalElements }}</span> entries)
    </span>
    <ul class="inline-flex items-center -space-x-px">
      <li>
        <button :disabled="currentPage === 0" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
                class="px-3 py-2 ml-0 leading-tight text-slate-500 bg-white border border-slate-300 rounded-l-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-20 disabled:cursor-not-allowed transition delay-50 duration-150 ease-in-out"
                @click="emit('prev')">
          Back
        </button>
      </li>
      <li>
        <button :disabled="currentPage >= totalPages - 1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
                class="px-3 py-2 leading-tight text-slate-500 bg-white border border-slate-300 rounded-r-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-20 disabled:cursor-not-allowed transition delay-50 duration-150 ease-in-out"
                @click="emit('next')">
          Next
        </button>
      </li>
    </ul>
  </nav>
</template>