<template>
  <div id="app">
    <div class="container">
      <div class="header">
        <h1>Management System</h1>
      </div>
      <div v-if="error" class="error">
        {{ error }}
        <button @click="error = null" style="float: right; background: none; color: inherit; padding: 5px;">×</button>
      </div>

      <div v-if="success" class="success">
        {{ success }}
        <button @click="success = null" style="float: right; background: none; color: inherit; padding: 5px;">×</button>
      </div>

      <div class="controls">
        <div class="pagination">
          <button @click="previousPage" :disabled="currentPage === 0">
            ← Previous
          </button>
          <span class="pagination-info">
            Page {{ currentPage + 1 }} of {{ totalPages }}
            ({{ totalElements }} total customers)
          </span>
          <button @click="nextPage" :disabled="currentPage >= totalPages - 1">
            Next →
          </button>
        </div>
        <button class="add-customer-btn" @click="showAddModal">
          + Add Customer
        </button>
      </div>

      <div v-if="loading" class="loading">
        Loading customers...
      </div>

      <div v-else class="customer-table-container">
        <table class="customer-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Address</th>
              <th>Email</th>
              <th>Telephone</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(customer, index) in customers" :key="customer.id">
              <td>{{ (currentPage * pageSize) + index + 1 }}</td>
              <td>
                <div class="customer-name">{{ customer.name }}</div>
              </td>
              <td>{{ customer.address }}</td>
              <td>{{ customer.email }}</td>
              <td>{{ customer.telephone }}</td>
              <td>
                <div class="customer-actions">
                  <button class="edit-btn" @click="editCustomer(customer)" title="Edit customer">
                    Edit
                  </button>
                  <button class="delete-btn" @click="deleteCustomer(customer.id)" title="Delete customer">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="customers.length === 0">
              <td colspan="6" style="text-align: center; padding: 2rem; color: #666;">
                No customers found.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Add/Edit Modal -->
      <div class="modal" :class="{ active: showModal }">
        <div class="modal-content">
          <h2>{{ isEditing ? 'Edit Customer' : 'Add New Customer' }}</h2>
          <form @submit.prevent="saveCustomer">
            <div class="form-group">
              <label for="name">Name *</label>
              <input type="text" id="name" v-model="customerForm.name" required>
            </div>
            <div class="form-group">
              <label for="address">Address *</label>
              <input type="text" id="address" v-model="customerForm.address" required>
            </div>
            <div class="form-group">
              <label for="email">Email *</label>
              <input type="email" id="email" v-model="customerForm.email" required>
            </div>
            <div class="form-group">
              <label for="telephone">Telephone *</label>
              <input type="tel" id="telephone" v-model="customerForm.telephone" required>
            </div>
            <div class="form-actions">
              <button type="button" class="cancel-btn" @click="closeModal">
                Cancel
              </button>
              <button type="submit" :disabled="saving">
                {{ saving ? 'Saving...' : (isEditing ? 'Update' : 'Add') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'App',
  data() {
    return {
      customers: [],
      loading: false,
      saving: false,
      error: null,
      success: null,
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      totalElements: 0,
      showModal: false,
      isEditing: false,
      customerForm: {
        id: null,
        name: '',
        address: '',
        email: '',
        telephone: ''
      },
      baseUrl: 'http://localhost:8080/digg/user'
    }
  },
  mounted() {
    this.loadCustomers();
  },
  methods: {
    async loadCustomers() {
      this.loading = true;
      this.error = null;

      try {
        const response = await axios.get(
          `${this.baseUrl}/paginated?page=${this.currentPage}&size=${this.pageSize}`
        );

        this.customers = response.data.customers;
        this.totalPages = response.data.totalPages;
        this.totalElements = response.data.totalElements;
      } catch (error) {
        this.error = 'Error loading customers: ' + error.message;
        console.error('Error:', error);
      } finally {
        this.loading = false;
      }
    },

    async saveCustomer() {
      this.saving = true;
      this.error = null;

      try {
        const url = this.isEditing
          ? `${this.baseUrl}/${this.customerForm.id}`
          : this.baseUrl;

        const method = this.isEditing ? 'put' : 'post';

        await axios[method](url, {
          name: this.customerForm.name,
          address: this.customerForm.address,
          email: this.customerForm.email,
          telephone: this.customerForm.telephone
        });

        this.success = this.isEditing
          ? 'Customer updated successfully!'
          : 'Customer added successfully!';

        this.closeModal();
        await this.loadCustomers();

        setTimeout(() => {
          this.success = null;
        }, 3000);

      } catch (error) {
        this.error = 'Error saving customer: ' + error.message;
        console.error('Error:', error);
      } finally {
        this.saving = false;
      }
    },

    async deleteCustomer(customerId) {
      if (!confirm('Are you sure you want to delete this customer?')) {
        return;
      }

      this.error = null;

      try {
        await axios.delete(`${this.baseUrl}/${customerId}`);

        this.success = 'Customer deleted successfully!';
        await this.loadCustomers();

        setTimeout(() => {
          this.success = null;
        }, 3000);

      } catch (error) {
        this.error = 'Error deleting customer: ' + error.message;
        console.error('Error:', error);
      }
    },

    showAddModal() {
      this.isEditing = false;
      this.customerForm = {
        id: null,
        name: '',
        address: '',
        email: '',
        telephone: ''
      };
      this.showModal = true;
    },

    editCustomer(customer) {
      this.isEditing = true;
      this.customerForm = {
        id: customer.id,
        name: customer.name,
        address: customer.address,
        email: customer.email,
        telephone: customer.telephone
      };
      this.showModal = true;
    },

    closeModal() {
      this.showModal = false;
      this.saving = false;
    },

    async nextPage() {
      if (this.currentPage < this.totalPages - 1) {
        this.currentPage++;
        await this.loadCustomers();
      }
    },

    async previousPage() {
      if (this.currentPage > 0) {
        this.currentPage--;
        await this.loadCustomers();
      }
    }
  }
}
</script>

<style>
/* Copy the content from your old style.css here */
/* Or keep it as a separate file and import it */
@import './style.css';
</style>