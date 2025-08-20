const { createApp } = Vue;

createApp({
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
                const response = await fetch(
                    `${this.baseUrl}/paginated?page=${this.currentPage}&size=${this.pageSize}`
                );

                if (!response.ok) {
                    throw new Error('Failed to load customers');
                }

                const data = await response.json();
                this.customers = data.customers;
                this.totalPages = data.totalPages;
                this.totalElements = data.totalElements;
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

                const method = this.isEditing ? 'PUT' : 'POST';

                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        name: this.customerForm.name,
                        address: this.customerForm.address,
                        email: this.customerForm.email,
                        telephone: this.customerForm.telephone
                    })
                });

                if (!response.ok) {
                    throw new Error('Failed to save customer');
                }

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
                const response = await fetch(`${this.baseUrl}/${customerId}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    throw new Error('Failed to delete customer');
                }

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
}).mount('#app');