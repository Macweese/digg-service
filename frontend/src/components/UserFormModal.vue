<script setup lang="ts">
import { reactive, watch, onBeforeUnmount, ref } from 'vue';
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

type Field = 'name' | 'email' | 'telephone' | 'address';

const fields: Field[] = ['name', 'email', 'telephone', 'address'];

const errors = reactive<Record<Field, string | null>>({
  name: null,
  email: null,
  telephone: null,
  address: null
});

const touched = reactive<Record<Field, boolean>>({
  name: false,
  email: false,
  telephone: false,
  address: false
});

const submitAttempted = ref(false);

// element refs to focus first invalid
const inputRefs: Record<Field, any> = {
  name: ref<HTMLInputElement | null>(null),
  email: ref<HTMLInputElement | null>(null),
  telephone: ref<HTMLInputElement | null>(null),
  address: ref<HTMLInputElement | null>(null)
};

// Reset form values (used when modal closes or user prop cleared)
function reset(): void {
  form.id = undefined;
  form.name = '';
  form.address = '';
  form.email = '';
  form.telephone = '';

  fields.forEach(f => {
    errors[f] = null;
    touched[f] = false;
  });
  submitAttempted.value = false;
}

watch(() => props.user, (u) => {
  if (u) {
    Object.assign(form, u);
    // clear errors/touched when loading an existing user
    fields.forEach(f => {
      errors[f] = null;
      touched[f] = false;
    });
    submitAttempted.value = false;
  } else {
    reset();
  }
}, { immediate: true });

// Validation rules
function validateField(field: Field): string | null {
  const val = String((form as any)[field] ?? '').trim();

  switch (field) {
    case 'name':
      if (!val) return 'Name is required';
      if (val.length < 2) return 'Name must be at least 2 characters';
      return null;

    case 'email': {
      if (!val) return 'Email is required';
      // simple email pattern
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!re.test(val)) return 'Enter a valid email address';
      return null;
    }

    case 'telephone': {
      if (!val) return 'Telephone is required';
      // allow digits, spaces, hyphen, parentheses, leading +
      const re = /^\+?[0-9\s\-().]{6,}$/;
      if (!re.test(val)) return 'Enter a valid phone number';
      return null;
    }

    case 'address':
      if (!val) return 'Address is required';
      if (val.length < 4) return 'Address looks too short';
      return null;
  }
}

function validateAll(): boolean {
  let ok = true;
  fields.forEach(f => {
    const msg = validateField(f);
    errors[f] = msg;
    if (msg) ok = false;
  });
  return ok;
}

function showError(field: Field): boolean {
  return !!errors[field] && (touched[field] || submitAttempted.value);
}

function onBlur(field: Field) {
  touched[field] = true;
  errors[field] = validateField(field);
}

function onInput(field: Field) {
  // Live re-validate once touched or after a submit attempt
  if (touched[field] || submitAttempted.value) {
    errors[field] = validateField(field);
  }
}

function focusFirstError() {
  for (const f of fields) {
    if (errors[f]) {
      const el = inputRefs[f].value as HTMLInputElement | null;
      if (el) el.focus();
      break;
    }
  }
}

function onSubmit(): void {
  submitAttempted.value = true;
  const ok = validateAll();
  if (!ok) {
    focusFirstError();
    return;
  }
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
    !(e as any).isComposing &&
    !(e.target instanceof HTMLTextAreaElement)
  ) {
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
          <form @submit.prevent="onSubmit" novalidate>
            <div class="p-6">
              <h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
                {{ form.id ? 'Edit User' : 'Add New User' }}
              </h2>

              <div class="space-y-4">
                <!-- Name -->
                <div>
                  <label for="name" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Name</label>
                  <input
                    ref="name"
                    :ref="inputRefs.name"
                    type="text"
                    id="name"
                    v-model="form.name"
                    required
                    @blur="onBlur('name')"
                    @input="onInput('name')"
                    :aria-invalid="showError('name')"
                    aria-describedby="name-hint name-error"
                    class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                    :class="{ 'border-red-500 focus:ring-red-500/80': showError('name') }"
                  />
                  <p id="name-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400" v-if="!showError('name')">
                    Enter the user’s full name (min 2 characters).
                  </p>
                  <p id="name-error" class="mt-1 text-xs text-red-600" role="alert" v-else>
                    {{ errors.name }}
                  </p>
                </div>

                <!-- Email -->
                <div>
                  <label for="email" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Email</label>
                  <input
                    ref="email"
                    :ref="inputRefs.email"
                    type="email"
                    id="email"
                    v-model="form.email"
                    required
                    @blur="onBlur('email')"
                    @input="onInput('email')"
                    :aria-invalid="showError('email')"
                    aria-describedby="email-hint email-error"
                    class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                    :class="{ 'border-red-500 focus:ring-red-500/80': showError('email') }"
                  />
                  <p id="email-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400" v-if="!showError('email')">
                    We’ll use this to contact the user.
                  </p>
                  <p id="email-error" class="mt-1 text-xs text-red-600" role="alert" v-else>
                    {{ errors.email }}
                  </p>
                </div>

                <!-- Telephone -->
                <div>
                  <label for="phone" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Telephone</label>
                  <input
                    ref="telephone"
                    :ref="inputRefs.telephone"
                    type="tel"
                    id="phone"
                    v-model="form.telephone"
                    required
                    @blur="onBlur('telephone')"
                    @input="onInput('telephone')"
                    :aria-invalid="showError('telephone')"
                    aria-describedby="telephone-hint telephone-error"
                    class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                    :class="{ 'border-red-500 focus:ring-red-500/80': showError('telephone') }"
                  />
                  <p id="telephone-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400" v-if="!showError('telephone')">
                    Digits, spaces, parentheses, dashes and “+” are allowed.
                  </p>
                  <p id="telephone-error" class="mt-1 text-xs text-red-600" role="alert" v-else>
                    {{ errors.telephone }}
                  </p>
                </div>

                <!-- Address -->
                <div>
                  <label for="address" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Address</label>
                  <input
                    ref="address"
                    :ref="inputRefs.address"
                    type="text"
                    id="address"
                    v-model="form.address"
                    required
                    @blur="onBlur('address')"
                    @input="onInput('address')"
                    :aria-invalid="showError('address')"
                    aria-describedby="address-hint address-error"
                    class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                    :class="{ 'border-red-500 focus:ring-red-500/80': showError('address') }"
                  />
                  <p id="address-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400" v-if="!showError('address')">
                    Street, number and any additional info.
                  </p>
                  <p id="address-error" class="mt-1 text-xs text-red-600" role="alert" v-else>
                    {{ errors.address }}
                  </p>
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
}
.modal-enter-from {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}
.modal-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}
</style>