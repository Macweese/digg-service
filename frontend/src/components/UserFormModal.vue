<script lang="ts" setup>
import {onBeforeUnmount, reactive, ref, watch} from 'vue';
import type {User} from '../types';

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
}, {immediate: true});

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
  emit('save', {...form});
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
}, {immediate: true});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown);
});
</script>

<template>
  <!-- Backdrop -->
  <Transition name="overlay">
    <div
        v-if="visible"
        aria-hidden="true"
        class="fixed inset-0 z-50 grid place-items-center p-4 bg-black/40 backdrop-blur-[1px]"
        @click="emit('close')"
    >
      <!-- Modal panel -->
      <Transition name="modal">
        <div
            v-if="visible"
            aria-modal="true"
            class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md transform"
            role="dialog"
            @click.stop
        >
          <form novalidate @submit.prevent="onSubmit">
            <div class="p-6">
              <h2 :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
                  class="text-2xl font-bold text-slate-800 dark:text-white mb-6">
                {{ form.id ? 'Edit User' : 'Add New User' }}
              </h2>

              <div class="space-y-4">
                <!-- Name -->
                <div>
                  <label :style="{ userSelect: 'none', WebkitUserSelect: 'none' }" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
                         for="name">Name</label>
                  <input
                      id="name"
                      :ref="inputRefs.name"
                      ref="name"
                      v-model="form.name"
                      :aria-invalid="showError('name')"
                      :class="{ 'border-red-500 focus:ring-red-500/80': showError('name') }"
                      aria-describedby="name-hint name-error"
                      class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                      required
                      type="text"
                      @blur="onBlur('name')"
                      @input="onInput('name')"
                  />
                  <p v-if="!showError('name')" id="name-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    Enter the user’s full name (min 2 characters).
                  </p>
                  <p v-else id="name-error" class="mt-1 text-xs text-red-600" role="alert">
                    {{ errors.name }}
                  </p>
                </div>

                <!-- Email -->
                <div>
                  <label :style="{ userSelect: 'none', WebkitUserSelect: 'none' }" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
                         for="email">Email</label>
                  <input
                      id="email"
                      :ref="inputRefs.email"
                      ref="email"
                      v-model="form.email"
                      :aria-invalid="showError('email')"
                      :class="{ 'border-red-500 focus:ring-red-500/80': showError('email') }"
                      aria-describedby="email-hint email-error"
                      class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                      required
                      type="email"
                      @blur="onBlur('email')"
                      @input="onInput('email')"
                  />
                  <p v-if="!showError('email')" id="email-hint" class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    We’ll use this to contact the user.
                  </p>
                  <p v-else id="email-error" class="mt-1 text-xs text-red-600" role="alert">
                    {{ errors.email }}
                  </p>
                </div>

                <!-- Telephone -->
                <div>
                  <label :style="{ userSelect: 'none', WebkitUserSelect: 'none' }" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
                         for="phone">Telephone</label>
                  <input
                      id="phone"
                      :ref="inputRefs.telephone"
                      ref="telephone"
                      v-model="form.telephone"
                      :aria-invalid="showError('telephone')"
                      :class="{ 'border-red-500 focus:ring-red-500/80': showError('telephone') }"
                      aria-describedby="telephone-hint telephone-error"
                      class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                      required
                      type="tel"
                      @blur="onBlur('telephone')"
                      @input="onInput('telephone')"
                  />
                  <p v-if="!showError('telephone')" id="telephone-hint"
                     class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    Digits, spaces, parentheses, dashes and “+” are allowed.
                  </p>
                  <p v-else id="telephone-error" class="mt-1 text-xs text-red-600" role="alert">
                    {{ errors.telephone }}
                  </p>
                </div>

                <!-- Address -->
                <div>
                  <label :style="{ userSelect: 'none', WebkitUserSelect: 'none' }" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
                         for="address">Address</label>
                  <input
                      id="address"
                      :ref="inputRefs.address"
                      ref="address"
                      v-model="form.address"
                      :aria-invalid="showError('address')"
                      :class="{ 'border-red-500 focus:ring-red-500/80': showError('address') }"
                      aria-describedby="address-hint address-error"
                      class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 dark:text-white transition delay-50 duration-150 ease-in-out"
                      required
                      type="text"
                      @blur="onBlur('address')"
                      @input="onInput('address')"
                  />
                  <p v-if="!showError('address')" id="address-hint"
                     class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    Street, number and any additional info.
                  </p>
                  <p v-else id="address-error" class="mt-1 text-xs text-red-600" role="alert">
                    {{ errors.address }}
                  </p>
                </div>
              </div>
            </div>

            <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-end gap-3 rounded-b-lg">
              <button :style="{ userSelect: 'none', WebkitUserSelect: 'none' }" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80 transition delay-50 duration-150 ease-in-out"
                      type="button"
                      @click="emit('close')">
                Cancel
              </button>
              <button :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
                      class="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-teal-500/40 from-10% via-sky-500/30 via-30% to-emerald-500/30 to-90% bg-indigo-600 border border-transparent rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2  focus:ring-opacity-30 focus:ring-blue-500/80 transition delay-50 duration-150 ease-in-out"
                      type="submit">
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