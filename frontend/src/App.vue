<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { Menu, MenuButton, MenuItem, MenuItems } from '@headlessui/vue';
import { ChevronDownIcon } from '@heroicons/vue/20/solid';

// --- STATE MANAGEMENT (Reactivity) ---
const customers = ref([]);
const isLoading = ref(true);
const isModalOpen = ref(false);
const firstInput = ref(null);
const editingCustomer = ref(null);
const searchTerm = ref('');
const currentPage = ref(0);
const isDeleteConfirmOpen = ref(false);
const customerToDelete = ref(null);
const toastMessage = ref('');
const errorMessage = ref('');
const itemsPerPage = ref(10);

// The form is a reactive object
const customerForm = reactive({
  id: null,
  name: '',
  address: '',
  email: '',
  telephone: ''
});

// --- CONSTANTS ---
const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50];
const BASE_API_URL = 'http://localhost:8080/digg/user';

// --- API METHODS ---
// Fetch ALL customers from the backend
async function loadCustomers() {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await fetch(BASE_API_URL);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    customers.value = data;
  } catch (error) {
    console.error("Failed to load customers:", error);
    errorMessage.value = "Could not connect to the server or failed to load customers.";
    customers.value = [];
  } finally {
    isLoading.value = false;
  }
}

// Save new or existing customer
async function handleSaveCustomer() {
  const isEditing = !!editingCustomer.value;
  const url = isEditing ? `${BASE_API_URL}/${customerForm.id}` : BASE_API_URL;
  const method = isEditing ? 'PUT' : 'POST';

  try {
    const response = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(customerForm),
    });
    if (!response.ok) {
      throw new Error('Failed to save the customer.');
    }
    await loadCustomers();
    showToast(`Customer ${isEditing ? 'updated' : 'added'} successfully!`);
    handleCloseModal();
  } catch (error) {
    console.error("Failed to save customer:", error);
    errorMessage.value = "Failed to save customer. Please try again.";
  }
}

// Delete customer after confirmation
async function confirmDelete() {
  try {
    const response = await fetch(`${BASE_API_URL}/${customerToDelete.value}`, {
      method: 'DELETE',
    });
    if (!response.ok) {
      throw new Error('Failed to delete the customer.');
    }

    if (paginatedCustomers.value.length === 1 && currentPage.value > 0) {
      currentPage.value--;
    }

    await loadCustomers();
    showToast('Customer deleted successfully!');
  } catch (error) {
    console.error("Failed to delete customer:", error);
    errorMessage.value = "Failed to delete customer. Please try again.";
  } finally {
    isDeleteConfirmOpen.value = false;
    customerToDelete.value = null;
  }
}

// --- COMPUTED PROPERTIES (Client-Side Logic) ---

// Filter customers based on search term
const filteredCustomers = computed(() => {
  if (!searchTerm.value) {
    return customers.value;
  }
  const lowerCaseSearch = searchTerm.value.toLowerCase();
  return customers.value.filter(customer =>
    Object.values(customer).some(val =>
      String(val).toLowerCase().includes(lowerCaseSearch)
    )
  );
});

// Calculate total elements and pages based on the *filtered* list
const totalElements = computed(() => filteredCustomers.value.length);
const totalPages = computed(() => Math.ceil(totalElements.value / itemsPerPage.value));

// Paginate the filtered list for display
const paginatedCustomers = computed(() => {
  if (currentPage.value >= totalPages.value) {
    currentPage.value = Math.max(0, totalPages.value - 1);
  }

  const start = currentPage.value * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredCustomers.value.slice(start, end);
});

// Calculate dynamic height based on items per page
const tableContainerHeight = computed(() => {
  const baseHeight = 600;
  const additionalHeightPerItem = 40;
  return `${baseHeight + (itemsPerPage.value - 10) * additionalHeightPerItem}px`;
});

// --- WATCHERS ---
watch(searchTerm, () => {
  // When a new search is performed, always go back to the first page
  currentPage.value = 0;
});

// Reset to first page when items per page changes
watch(itemsPerPage, () => {
  currentPage.value = 0;
});

watch(isModalOpen, (newValue) => {
  if (newValue) {
    document.addEventListener('keydown', handleKeydown);
  } else {
    document.removeEventListener('keydown', handleKeydown);
  }
});

watch(isDeleteConfirmOpen, (newValue) => {
  if (newValue) {
    document.addEventListener('keydown', handleKeydown);
  } else {
    document.removeEventListener('keydown', handleKeydown);
  }
});

// Reset to first page when items per page changes
watch(itemsPerPage, () => {
  currentPage.value = 0;
});

function handleKeydown(event) {
  if (event.key === 'Escape' && (isModalOpen.value || isDeleteConfirmOpen.value)) {
    handleCloseModal();
  }
}

// --- HELPER METHODS ---

function showToast(message) {
  toastMessage.value = message;
  setTimeout(() => { toastMessage.value = '' }, 3000);
}

function resetCustomerForm() {
  customerForm.id = null;
  customerForm.name = '';
  customerForm.address = '';
  customerForm.email = '';
  customerForm.telephone = '';
}

function handleAddCustomer() {
  editingCustomer.value = null;
  resetCustomerForm();
  isModalOpen.value = true;
  nextTick(() => {
    firstInput.value?.focus({ preventScroll: true });
  });
}

function handleEditCustomer(customer) {
  editingCustomer.value = customer;
  Object.assign(customerForm, customer);
  isModalOpen.value = true;
  nextTick(() => {
    firstInput.value?.focus({ preventScroll: true });
  });
}

function handleCloseModal() {
  isModalOpen.value = false;
  editingCustomer.value = null;
  isDeleteConfirmOpen.value = null;
}

function handleDeleteCustomer(customerId) {
  customerToDelete.value = customerId;
  isDeleteConfirmOpen.value = true;
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
  }
}

function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--;
  }
}

// --- LIFECYCLE HOOK ---
onMounted(() => {
  loadCustomers();
});
</script>

<template>
  <header class="bg-white dark:bg-slate-800/50 backdrop-blur-sm shadow-sm sticky top-0 z-10">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center py-4">
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Management dashboard</h1>
      </div>
    </div>
  </header>

  <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="bg-white dark:bg-slate-800 p-4 sm:p-6 rounded-lg shadow-sm">
      <div v-if="errorMessage" class="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg relative" role="alert">
        <strong class="font-bold">Error: </strong>
        <span class="block sm:inline">{{ errorMessage }}</span>
        <span class="absolute top-0 bottom-0 right-0 px-4 py-3" @click="errorMessage = ''">
          <svg class="fill-current h-6 w-6 text-red-500" role="button" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><title>Close</title><path d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"/></svg>
        </span>
      </div>

      <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6">
        <div class="relative w-full sm:max-w-xs">
          <div class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="11" cy="11" r="8"></circle><line x1="21" x2="16.65" y1="21" y2="16.65"></line></svg>
          </div>
          <input
              v-model="searchTerm"
              type="text"
              placeholder="Search"
              class="w-full pl-10 pr-4 py-2 border border-slate-300 dark:border-slate-600 rounded-md bg-slate-100 dark:bg-slate-700/50 focus:outline-none" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
          />
        </div>

        <div class="flex flex-col sm:flex-row gap-20 w-full sm:w-auto">
          <Menu as="div" class="relative inline-block text-left">
            <MenuButton class="inline-flex w-full justify-center gap-x-1.5 rounded-md bg-slate-700 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-700/90">
              {{ itemsPerPage }} per page
              <ChevronDownIcon class="-mr-1 h-5 w-5 text-indigo-200" aria-hidden="true" />
            </MenuButton>

            <transition enter-active-class="transition ease-out duration-100" enter-from-class="transform opacity-0 scale-95" enter-to-class="transform opacity-100 scale-100" leave-active-class="transition ease-in duration-75" leave-from-class="transform opacity-100 scale-100" leave-to-class="transform opacity-0 scale-95">
              <MenuItems class="absolute right-0 z-10 mt-2 w-40 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none dark:bg-slate-800 dark:ring-white/10">
                <div class="py-1">
                  <MenuItem v-for="option in ITEMS_PER_PAGE_OPTIONS" :key="option" v-slot="{ active }">
                    <button
                      @click="itemsPerPage = option"
                      :class="[active ? 'bg-indigo-100 text-indigo-900 dark:bg-slate-700 dark:text-white' : 'text-gray-700 dark:text-gray-300', 'block px-4 py-2 text-sm w-full text-left']"
                    >
                      {{ option }} per page
                    </button>
                  </MenuItem>
                </div>
              </MenuItems>
            </transition>
          </Menu>

          <button @click="handleAddCustomer" class="flex items-center justify-center px-4 py-2 bg-indigo-600 text-white font-semibold rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-slate-50 dark:focus:ring-offset-slate-900 transition-colors" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4 mr-2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><line x1="19" x2="19" y1="8" y2="14"></line><line x1="22" x2="16" y1="11" y2="11"></line></svg>
            Add Customer
          </button>
        </div>
      </div>

      <div class="overflow-x-auto" :style="{ minHeight: tableContainerHeight }">
        <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500 mx-auto"></div>
          <p class="mt-4 text-slate-500 dark:text-slate-400">Loading customers...</p>
        </div>
        <table v-else-if="paginatedCustomers.length > 0" class="w-full text-sm text-left text-slate-500 dark:text-slate-400 fixed-table">
          <thead class="text-xs text-slate-700 uppercase bg-slate-50 dark:bg-slate-700 dark:text-slate-300" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          <tr>
            <th scope="col" class="px-6 py-3">Name</th>
            <th scope="col" class="px-6 py-3 hidden md:table-cell">Contact</th>
            <th scope="col" class="px-6 py-3 hidden lg:table-cell">Address</th>
            <th scope="col" class="px-6 py-3 text-right">Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="customer in paginatedCustomers" :key="customer.id" class="bg-white dark:bg-slate-800 border-b dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
            <td class="px-6 py-4 font-medium text-slate-900 dark:text-white whitespace-nowrap">{{ customer.name }}</td>
            <td class="px-6 py-4 hidden md:table-cell">
              <div>{{ customer.email }}</div>
              <div class="text-xs text-slate-500">{{ customer.telephone }}</div>
            </td>
            <td class="px-6 py-4 hidden lg:table-cell">{{ customer.address }}</td>
            <td class="px-6 py-4">
              <div class="flex justify-end items-center gap-2">
                <button @click="handleEditCustomer(customer)" class="p-2 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"></path></svg>
                </button>
                <button @click="handleDeleteCustomer(customer.id)" class="p-2 text-slate-500 hover:text-red-600 dark:hover:text-red-400 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4"><path d="M3 6h18"></path><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path></svg>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
        <div v-else-if="!errorMessage" class="text-center py-12">
           <h3 class="text-lg font-semibold text-slate-700 dark:text-slate-300">
            {{ searchTerm ? `No results for "${searchTerm}"` : 'No Customers Found' }}
          </h3>
          <p class="mt-1 text-slate-500 dark:text-slate-400">
            {{ searchTerm ? 'Try searching for something else.' : 'Click "Add Customer" to get started.' }}
          </p>
        </div>
      </div>

      <nav v-if="totalPages > 1" class="flex items-center justify-between pt-4" aria-label="Table navigation">
        <span class="text-sm font-normal text-slate-500 dark:text-slate-400" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            Page <span class="font-semibold text-slate-900 dark:text-white">{{ currentPage + 1 }}</span>
            of <span class="font-semibold text-slate-900 dark:text-white">{{ totalPages }}</span>
            (<span class="font-semibold text-slate-900 dark:text-white">{{ totalElements }}</span> entries)
        </span>
          <Menu as="div" class="relative inline-block text-left">
            <MenuButton class="inline-flex w-full justify-center gap-x-1.5 rounded-md bg-slate-700 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-slate-700/90">
              {{ itemsPerPage }} per page
              <ChevronDownIcon class="-mr-1 h-5 w-5 text-indigo-200" aria-hidden="true" />
            </MenuButton>

            <transition enter-active-class="transition ease-out duration-100" enter-from-class="transform opacity-0 scale-95" enter-to-class="transform opacity-100 scale-100" leave-active-class="transition ease-in duration-75" leave-from-class="transform opacity-100 scale-100" leave-to-class="transform opacity-0 scale-95">
              <MenuItems class="absolute right-0 z-10 mt-2 w-40 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none dark:bg-slate-800 dark:ring-white/10">
                <div class="py-1">
                  <MenuItem v-for="option in ITEMS_PER_PAGE_OPTIONS" :key="option" v-slot="{ active }">
                    <button
                      @click="itemsPerPage = option"
                      :class="[active ? 'bg-indigo-100 text-indigo-900 dark:bg-slate-700 dark:text-white' : 'text-gray-700 dark:text-gray-300', 'block px-4 py-2 text-sm w-full text-left']"
                    >
                      {{ option }} per page
                    </button>
                  </MenuItem>
                </div>
              </MenuItems>
            </transition>
          </Menu>
        <ul class="inline-flex items-center -space-x-px">
          <li>
            <button @click="prevPage()" :disabled="currentPage === 0" class="px-3 py-2 ml-0 leading-tight text-slate-500 bg-white border border-slate-300 rounded-l-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
              Previous
            </button>
          </li>
          <li>
            <button @click="nextPage()" :disabled="currentPage >= totalPages - 1" class="px-3 py-2 leading-tight text-slate-500 bg-white border border-slate-300 rounded-r-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
              Next
            </button>
          </li>
        </ul>
      </nav>
    </div>
  </main>

  <div v-if="isModalOpen" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4" @click="handleCloseModal">
    <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md" @click.stop>
      <form @submit.prevent="handleSaveCustomer">
        <div class="p-6">
          <h2 class="text-2xl font-bold text-slate-800 dark:text-white mb-6" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            {{ editingCustomer ? 'Edit Customer' : 'Add New Customer' }}
          </h2>
          <div class="space-y-4">
            <div>
              <label for="name" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Name</label>
              <input ref="firstInput" type="text" id="name" v-model="customerForm.name" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white" />
            </div>
            <div>
              <label for="email" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Email</label>
              <input type="email" id="email" v-model="customerForm.email" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white" />
            </div>
            <div>
              <label for="phone" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Telephone</label>
              <input type="tel" id="phone" v-model="customerForm.telephone" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white" />
            </div>
            <div>
              <label for="address" class="block text-sm font-medium text-slate-600 dark:text-slate-300 mb-1" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">Address</label>
              <input type="text" id="address" v-model="customerForm.address" required class="w-full px-3 py-2 bg-slate-50 dark:bg-slate-700 border border-slate-300 dark:border-slate-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:text-white" />
            </div>
          </div>
        </div>
        <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-end gap-3 rounded-b-lg">
          <button type="button" @click="handleCloseModal" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            Cancel
          </button>
          <button type="submit" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 border border-transparent rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
            Save Customer
          </button>
        </div>
      </form>
    </div>
  </div>

  <div v-if="isDeleteConfirmOpen" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex justify-center items-center p-4" @click="isDeleteConfirmOpen = false">
    <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-sm" @click.stop>
      <div class="p-6 text-center" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-12 w-12 text-red-500 mx-auto"><path d="m21.73 18-8-14a2 2 0 0 0-3.46 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"></path><path d="M12 9v4"></path><path d="M12 17h.01"></path></svg>
        <h3 class="mt-5 mb-2 text-lg font-semibold text-slate-900 dark:text-white">Delete Customer</h3>
        <p class="text-sm text-slate-500 dark:text-slate-400">Are you sure you want to delete this customer? This action cannot be undone.</p>
      </div>
      <div class="bg-slate-50 dark:bg-slate-700 px-6 py-4 flex justify-center gap-3 rounded-b-lg">
        <button @click="isDeleteConfirmOpen = false" class="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-600 border border-slate-300 dark:border-slate-500 rounded-md hover:bg-slate-50 dark:hover:bg-slate-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Cancel
        </button>
        <button @click="confirmDelete" class="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
          Confirm Delete
        </button>
      </div>
    </div>
  </div>

  <div v-if="toastMessage" class="fixed bottom-5 right-5 bg-green-500 text-white py-2 px-4 rounded-lg shadow-lg flex items-center fade-in-out">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-5 h-5 mr-2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
    {{ toastMessage }}
  </div>
</template>

<style>
/* Toast animation */
.fade-in-out {
    animation: fadeInOut 3s ease-in-out forwards;
}

@keyframes fadeInOut {
    0% {
        opacity: 0;
        transform: translateY(20px);
    }
    15% {
        opacity: 1;
        transform: translateY(0);
    }
    85% {
        opacity: 1;
        transform: translateY(0);
    }
    100% {
        opacity: 0;
        transform: translateY(20px);
    }
}

/* Fixed table column widths */
.fixed-table {
  table-layout: fixed;
  width: 100%;
}

.fixed-table th:nth-child(1),
.fixed-table td:nth-child(1) {
  width: 20%; /* Name column */
}

.fixed-table th:nth-child(2),
.fixed-table td:nth-child(2) {
  width: 30%; /* Contact column */
}

.fixed-table th:nth-child(3),
.fixed-table td:nth-child(3) {
  width: 35%; /* Address column */
}

.fixed-table th:nth-child(4),
.fixed-table td:nth-child(4) {
  width: 15%; /* Actions column */
}

/* Ensure text doesn't overflow */
.fixed-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Allow wrapping for specific columns if needed */
.fixed-table td:nth-child(3) { /* Address column */
  white-space: normal;
  word-wrap: break-word;
}

/* Custom select styling */
select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.5rem center;
  background-size: 1em;
}
</style>