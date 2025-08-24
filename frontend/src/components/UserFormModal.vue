<script setup lang="ts">
import { reactive, watch, onBeforeUnmount } from 'vue';
import type { User } from '../types';

const props = defineProps<{
  visible: boolean
  user?: User | null
}>();
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'save', user: User): void
}>();

const form = reactive<User>({
  id: undefined,
  name: '',
  address: '',
  email: '',
  telephone: ''
});

function reset(): void {
  form.id = undefined;
  form.name = '';
  form.address = '';
  form.email = '';
  form.telephone = '';
}

watch(() => props.user, (u) => {
  if (u) {
    Object.assign(form, u);
  } else {
    reset();
  }
}, { immediate: true });

function onSubmit(): void {
  emit('save', { ...form });
}

// Keyboard handlers: ESC closes, ENTER submits (with guards)
function onKeydown(e: KeyboardEvent) {
  if (!props.visible) return;

  if (e.key === 'Escape') {
    e.preventDefault();
    emit('close');
    return;
  }

  if (
    e.key === 'Enter' &&
    !e.shiftKey && !e.ctrlKey && !e.altKey && !e.metaKey &&
    // avoid IME confirm
    !(e as any).isComposing &&
    // don't intercept multiline inputs
    !(e.target instanceof HTMLTextAreaElement)
  ) {
    // prevent default form auto-submit to avoid double submit
    e.preventDefault();
    onSubmit();
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
      @click="emit('close')"
      aria-hidden="true"
    >
      <!-- Modal panel -->
      <Transition name="modal">
        <div
          v-if="visible"
          class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md transform"
          role="dialog"
          aria-modal="true"
          @click.stop
        >
          <form @submit.prevent="onSubmit">
            <div class="p-6">
              <h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
                {{ form.id ? 'Edit User' : 'Add New User' }}
              </h2>
              <div class="space-y-4">
                <div>
                  <label for="name" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Name</label>
                  <input type="text" id="name" v-model="form.name" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white transition delay-50 duration-150 ease-in-out" />
                </div>
                <div>
                  <label for="email" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Email</label>
                  <input type="email" id="email" v-model="form.email" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white transition delay-50 duration-150 ease-in-out" />
                </div>
                <div>
                  <label for="phone" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Telephone</label>
                  <input type="tel" id="phone" v-model="form.telephone" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white transition delay-50 duration-150 ease-in-out" />
                </div>
                <div>
                  <label for="address" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Address</label>
                  <input type="text" id="address" v-model="form.address" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white transition delay-50 duration-150 ease-in-out" />
                </div>
              </div>
            </div>
            <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-end gap-3 rounded-b-lg">
              <button type="button" @click="emit('close')" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80 transition delay-50 duration-150 ease-in-out" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
                Cancel
              </button>
              <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-teal-500/40 from-10% via-sky-500/30 via-30% to-emerald-500/30 to-90% bg-indigo-600 border border-transparent rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80 transition delay-50 duration-150 ease-in-out" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
                Save User
              </button>
            </div>
          </form>
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