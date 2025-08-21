<template>
  <div id="app">

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
        if (error.response?.status === 404) {
          this.error = 'The requested resource was not found. Please check the URL.';
        } else if (error.response?.status === 500) {
          this.error = 'Server error. Please try again later.';
        } else if (error.response?.status === 403) {
          this.error = 'Access forbidden. Please check your permissions.';
        } else {
          this.error = 'Error loading customers: ' + (error.response?.data?.message || error.message);
        }
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
@import './style.css';
</style>