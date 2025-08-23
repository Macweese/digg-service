<script setup lang="ts">
import { watch, onBeforeUnmount } from 'vue';

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits<{ (e: 'cancel'): void; (e: 'confirm'): void }>();

function onKeydown(e: KeyboardEvent) {
  if (!props.visible) return;
  if (e.key === 'Escape') {
    e.preventDefault();
    emit('cancel');
  }
}

// Attach/detach the key listener only when visible
watch(() => props.visible, (vis) => {
  if (vis) document.addEventListener('keydown', onKeydown);
  else document.removeEventListener('keydown', onKeydown);
}, { immediate: true });

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown);
});
</script>

<template>
  <!-- Backdrop -->
  <Transition name="overlay">
    <div
      v-if="visible"
      class="fixed inset-0 z-50 grid place-items-center p-4 bg-black/40 backdrop-blur-[1px]"
      @click="emit('cancel')"
      aria-hidden="true"
    >
      <!-- Modal panel -->
      <Transition name="modal">
        <div
          v-if="visible"
          class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-sm transform"
          role="dialog"
          aria-modal="true"
          @click.stop
        >
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
      </Transition>
    </div>
  </Transition>
</template>

<style>
/* Backdrop fade */
.overlay-enter-active,
.overlay-leave-active {
  transition: opacity 200ms ease;
}
.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

/* Modal panel ease/slide/scale */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 220ms ease, transform 220ms ease;
  will-change: opacity, transform;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
</style>