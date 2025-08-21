<template>
  <div class="font-sans text-slate-800 dark:text-slate-200">
    <!-- Header -->
    <header class="bg-white dark:bg-slate-800/50 backdrop-blur-sm shadow-sm sticky top-0 z-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-4">
          <h1 class="text-2xl font-bold text-slate-900 dark:text-white">Customer Dashboard</h1>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white dark:bg-slate-800 p-4 sm:p-6 rounded-lg shadow-sm">
        <!-- Controls: Search and Add Button -->
        <div class="flex flex-col sm:flex-row justify-between items-center gap-4 mb-6">
          <div class="relative w-full sm:max-w-xs">
            <div class="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400">
              <SearchIcon />
            </div>
            <input
              type="text"
              placeholder="Search customers..."
              v-model="searchTerm"
              class="w-full pl-10 pr-4 py-2 border border-slate-300 dark:border-slate-600 rounded-md bg-white dark:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <button
            @click="handleAddCustomer"
            class="w-full sm:w-auto flex items-center justify-center px-4 py-2 bg-indigo-600 text-white font-semibold rounded-md shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-slate-50 dark:focus:ring-offset-slate-900 transition-colors"
          >
            <UserPlusIcon class="h-4 w-4 mr-2" />
            Add Customer
          </button>
        </div>

        <!-- Customer Table Area -->
        <div class="overflow-x-auto">
          <!-- Loading State -->
          <div v-if="isLoading" class="text-center py-12">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500 mx-auto"></div>
            <p class="mt-4 text-slate-500 dark:text-slate-400">Loading customers...</p>
          </div>

          <!-- Table View -->
          <table v-else-if="paginatedCustomers.length > 0" class="w-full text-sm text-left text-slate-500 dark:text-slate-400">
            <thead class="text-xs text-slate-700 uppercase bg-slate-50 dark:bg-slate-700 dark:text-slate-300">
              <tr>
                <th scope="col" class="px-6 py-3">Name</th>
                <th scope="col" class="px-6 py-3 hidden md:table-cell">Contact</th>
                <th scope="col" class="px-6 py-3 hidden lg:table-cell">Address</th>
                <th scope="col" class="px-6 py-3">Status</th>
                <th scope="col" class="px-6 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="customer in paginatedCustomers"
                :key="customer.id"
                class="bg-white dark:bg-slate-800 border-b dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors"
              >
                <td class="px-6 py-4 font-medium text-slate-900 dark:text-white whitespace-nowrap">
                  {{ customer.name }}
                </td>
                <td class="px-6 py-4 hidden md:table-cell">
                  <div>{{ customer.email }}</div>
                  <div class="text-xs text-slate-500">{{ customer.phone }}</div>
                </td>
                <td class="px-6 py-4 hidden lg:table-cell">{{ customer.address }}</td>
                <td class="px-6 py-4">
                  <span
                    class="px-2 py-1 text-xs font-medium rounded-full inline-block"
                    :class="getStatusClasses(customer.status)"
                  >
                    {{ customer.status }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <div class="flex justify-end items-center gap-2">
                    <button
                      @click="handleEditCustomer(customer)"
                      class="p-2 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    >
                      <EditIcon class="h-4 w-4" />
                    </button>
                    <button
                      @click="handleDeleteCustomer(customer.id)"
                      class="p-2 text-slate-500 hover:text-red-600 dark:hover:text-red-400 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
                    >
                      <TrashIcon class="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Empty State -->
          <div v-else class="text-center py-12">
            <h3 class="text-lg font-semibold text-slate-700 dark:text-slate-300">No Customers Found</h3>
            <p class="mt-1 text-slate-500 dark:text-slate-400">No customers match your search criteria.</p>
          </div>
        </div>

        <!-- Pagination -->
        <nav v-if="totalPages > 1" class="flex items-center justify-between pt-4" aria-label="Table navigation">
          <span class="text-sm font-normal text-slate-500 dark:text-slate-400">
            Showing
            <span class="font-semibold text-slate-900 dark:text-white">
              {{ (currentPage - 1) * ITEMS_PER_PAGE + 1 }}-{{ Math.min(currentPage * ITEMS_PER_PAGE, filteredCustomers.length) }}
            </span>
            of
            <span class="font-semibold text-slate-900 dark:text-white">{{ filteredCustomers.length }}</span>
          </span>
          <ul class="inline-flex items-center -space-x-px">
            <li>
              <button
                @click="currentPage = Math.max(1, currentPage - 1)"
                :disabled="currentPage === 1"
                class="px-3 py-2 ml-0 leading-tight text-slate-500 bg-white border border-slate-300 rounded-l-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
            </li>
            <li>
              <button
                @click="currentPage = Math.min(totalPages, currentPage + 1)"
                :disabled="currentPage === totalPages"
                class="px-3 py-2 leading-tight text-slate-500 bg-white border border-slate-300 rounded-r-lg hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-white disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </main>

    <!-- Add/Edit Customer Modal -->
    <CustomerModal
      v-if="isModalOpen"
      :editing-customer="editingCustomer"
      :customer-form="customerForm"
      :customer-statuses="CUSTOMER_STATUSES"
      @close="handleCloseModal"
      @save="handleSaveCustomer"
    />

    <!-- Delete Confirmation Dialog -->
    <DeleteConfirmDialog
      v-if="isDeleteConfirmOpen"
      @close="isDeleteConfirmOpen = false"
      @confirm="confirmDelete"
    />

    <!-- Toast Notification -->
    <ToastNotification
      v-if="toastMessage"
      :message="toastMessage"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import CustomerModal from './CustomerModal.vue'
import DeleteConfirmDialog from './DeleteConfirmDialog.vue'
import ToastNotification from './ToastNotification.vue'
import { SearchIcon, UserPlusIcon, EditIcon, TrashIcon } from './icons'

export default {
  name: 'CustomerDashboard',
  components: {
    CustomerModal,
    DeleteConfirmDialog,
    ToastNotification,
    SearchIcon,
    UserPlusIcon,
    EditIcon,
    TrashIcon
  },
  props: {
    customers: {
      type: Array,
      default: () => []
    }
  },
  emits: ['add-customer', 'edit-customer', 'delete-customer'],
  setup(props, { emit }) {
    // Constants
    const CUSTOMER_STATUSES = ['Active', 'Inactive', 'Lead']
    const ITEMS_PER_PAGE = 8

    // State
    const isLoading = ref(true)
    const isModalOpen = ref(false)
    const editingCustomer = ref(null)
    const searchTerm = ref('')
    const currentPage = ref(1)
    const isDeleteConfirmOpen = ref(false)
    const customerToDelete = ref(null)
    const toastMessage = ref('')
    const customerForm = reactive({
      id: null,
      name: '',
      email: '',
      phone: '',
      address: '',
      status: 'Lead'
    })

    // Status classes mapping
    const statusClassesMap = {
      'Active': 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300',
      'Inactive': 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300',
      'Lead': 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300',
    }

    // Computed properties
    const filteredCustomers = computed(() => {
      if (searchTerm.value) currentPage.value = 1

      const lowerCaseSearchTerm = searchTerm.value.toLowerCase()
      return props.customers.filter(customer => {
        return Object.values(customer).some(value =>
          String(value).toLowerCase().includes(lowerCaseSearchTerm)
        )
      })
    })

    const totalPages = computed(() => Math.ceil(filteredCustomers.value.length / ITEMS_PER_PAGE))

    const paginatedCustomers = computed(() => {
      const startIndex = (currentPage.value - 1) * ITEMS_PER_PAGE
      return filteredCustomers.value.slice(startIndex, startIndex + ITEMS_PER_PAGE)
    })

    // Methods
    const getStatusClasses = (status) => statusClassesMap[status] || ''

    const resetCustomerForm = () => {
      Object.assign(customerForm, {
        id: null,
        name: '',
        email: '',
        phone: '',
        address: '',
        status: 'Lead'
      })
    }

    const showToast = (message) => {
      toastMessage.value = message
      setTimeout(() => toastMessage.value = '', 3000)
    }

    const handleAddCustomer = () => {
      editingCustomer.value = null
      resetCustomerForm()
      isModalOpen.value = true
    }

    const handleEditCustomer = (customer) => {
      editingCustomer.value = customer
      Object.assign(customerForm, customer)
      isModalOpen.value = true
    }

    const handleCloseModal = () => {
      isModalOpen.value = false
      editingCustomer.value = null
    }

    const handleSaveCustomer = () => {
      const customerData = { ...customerForm }

      if (editingCustomer.value) {
        emit('edit-customer', customerData)
        showToast('Customer updated successfully!')
      } else {
        emit('add-customer', customerData)
        showToast('Customer added successfully!')
      }
      handleCloseModal()
    }

    const handleDeleteCustomer = (customerId) => {
      customerToDelete.value = customerId
      isDeleteConfirmOpen.value = true
    }

    const confirmDelete = () => {
      emit('delete-customer', customerToDelete.value)
      isDeleteConfirmOpen.value = false
      customerToDelete.value = null
      showToast('Customer deleted successfully!')
    }

    // Lifecycle
    onMounted(() => {
      // Simulate loading state
      setTimeout(() => {
        isLoading.value = false
      }, 1000)
    })

    return {
      // State
      isLoading,
      isModalOpen,
      editingCustomer,
      searchTerm,
      currentPage,
      isDeleteConfirmOpen,
      customerToDelete,
      toastMessage,
      customerForm,
      // Constants
      CUSTOMER_STATUSES,
      ITEMS_PER_PAGE,
      // Computed
      filteredCustomers,
      totalPages,
      paginatedCustomers,
      // Methods
      getStatusClasses,
      handleAddCustomer,
      handleEditCustomer,
      handleCloseModal,
      handleSaveCustomer,
      handleDeleteCustomer,
      confirmDelete,
    }
  }
}
</script>