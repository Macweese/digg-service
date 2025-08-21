import axios from 'axios'

// Create axios instance with base config
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/digg',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 seconds timeout
})

// Request interceptor for adding auth tokens, etc.
// Not needed in this demo though
apiClient.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor for handling errors globally
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('authToken')
      // If this was gated behind login page e.g. admin login
      // we would redirect to login or handle as needed
    }
    return Promise.reject(error)
  }
)

export const customerService = {
  // Get all customers
  getAllCustomers() {
    return apiClient.get('/customers')
  },

  // Get customer by ID
  getCustomerById(id) {
    return apiClient.get(`/customers/${id}`)
  },

  // Create new customer
  createCustomer(customerData) {
    return apiClient.post('/customers', customerData)
  },

  // Update existing customer
  updateCustomer(id, customerData) {
    return apiClient.put(`/customers/${id}`, customerData)
  },

  // Delete customer
  deleteCustomer(id) {
    return apiClient.delete(`/customers/${id}`)
  },

  // Search customers ~~ not added in backend for demo
  searchCustomers(query) {
    return apiClient.get('/customers/search', {
      params: { q: query }
    })
  },

  // Get customers with pagination
  getCustomersPaginated(page = 1, size = 10, sortBy = 'name', sortDir = 'asc') {
    return apiClient.get('/customers', {
      params: {
        page: page - 1, // Spring boot 0-indexed
        size,
        sort: `${sortBy},${sortDir}`
      }
    })
  }
}

export default customerService