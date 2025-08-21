<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4"
    @click="$emit('close')"
  >
    <div
      class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md"
      @click.stop
    >
      <form @submit.prevent="$emit('save')">
        <div class="p-6">
          <h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6">
            {{ editingCustomer ? 'Edit Customer' : 'Add New Customer' }}
          </h2>
          <div class="space-y-4">
            <div>
              <label
                for="name"
                class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
              >
                Name
              </label>
              <input
                type="text"
                id="name"
                v-model="customerForm.name"
                required
                class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white"
              />
            </div>
            <div>
              <label
                for="email"
                class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
              >
                Email
              </label>
              <input
                type="email"
                id="email"
                v-model="customerForm.email"
                required
                class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white"
              />
            </div>
            <div>
              <label
                for="phone"
                class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
              >
                Phone
              </label>
              <input
                type="tel"
                id="phone"
                v-model="customerForm.phone"
                required
                class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white"
              />
            </div>
            <div>
              <label
                for="address"
                class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
              >
                Address
              </label>
              <input
                type="text"
                id="address"
                v-model="customerForm.address"
                required
                class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white"
              />
            </div>
            <div>
              <label
                for="status"
                class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1"
              >
                Status
              </label>
              <select
                id="status"
                v-model="customerForm.status"
                class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white"
              >
                <option
                  v-for="status in customerStatuses"
                  :key="status"
                  :value="status"
                >
                  {{ status }}
                </option>
              </select>
            </div>
          </div>
        </div>
        <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-end gap-3 rounded-b-lg">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Cancel
          </button>
          <button
            type="submit"
            class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            Save Customer
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomerModal',
  props: {
    editingCustomer: {
      type: Object,
      default: null
    },
    customerForm: {
      type: Object,
      required: true
    },
    customerStatuses: {
      type: Array,
      required: true
    }
  },
  emits: ['close', 'save']
}
</script>