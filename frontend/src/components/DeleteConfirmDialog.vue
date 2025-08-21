<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4"
    @click="$emit('close')"
  >
    <div
      class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-sm"
      @click.stop
    >
      <div class="p-6 text-center">
        <AlertTriangleIcon class="h-12 w-12 text-red-500 mx-auto" />
        <h3 class="mt-5 mb-2 text-lg font-semibold text-slate-900 dark:text-white">
          Delete Customer
        </h3>
        <p class="text-sm text-slate-500 dark:text-slate-400">
          Are you sure you want to delete this customer? This action cannot be undone.
        </p>
      </div>
      <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-center gap-3 rounded-b-lg">
        <button
          @click="$emit('close')"
          class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
        >
          Cancel
        </button>
        <button
          @click="$emit('confirm')"
          class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
        >
          Confirm Delete
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { AlertTriangleIcon } from './icons'

export default {
  name: 'DeleteConfirmDialog',
  components: {
    AlertTriangleIcon
  },
  emits: ['close', 'confirm']
}
</script>