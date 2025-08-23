<script setup lang="ts">
import { reactive, watch } from 'vue';
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
</script>

<template>
  <div v-if="visible" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4" @click="emit('close')">
    <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md" @click.stop>
      <form @submit.prevent="onSubmit">
        <div class="p-6">
          <h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            {{ form.id ? 'Edit User' : 'Add New User' }}
          </h2>
          <div class="space-y-4">
            <div>
              <label for="name" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Name</label>
              <input type="text" id="name" v-model="form.name" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white" />
            </div>
            <div>
              <label for="email" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Email</label>
              <input type="email" id="email" v-model="form.email" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white" />
            </div>
            <div>
              <label for="phone" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Telephone</label>
              <input type="tel" id="phone" v-model="form.telephone" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white" />
            </div>
            <div>
              <label for="address" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Address</label>
              <input type="text" id="address" v-model="form.address" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/80 dark:text-white" />
            </div>
          </div>
        </div>
        <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-end gap-3 rounded-b-lg">
          <button type="button" @click="emit('close')" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            Cancel
          </button>
          <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-teal-500/40 from-10% via-sky-500/30 via-30% to-emerald-500/30 to-90% bg-indigo-600 border border-transparent rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            Save User
          </button>
        </div>
      </form>
    </div>
  </div>
</template>