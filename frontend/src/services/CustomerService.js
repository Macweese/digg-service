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

export const userService = {
  // Get all users
  getAllUsers() {
    return apiClient.get('/users')
  },

  // Get user by ID
  getUserById(id) {
    return apiClient.get(`/users/${id}`)
  },

  // Create new user
  createUser(userData) {
    return apiClient.post('/users', userData)
  },

  // Update existing user
  updateUser(id, userData) {
    return apiClient.put(`/users/${id}`, userData)
  },

  // Delete user
  deleteUser(id) {
    return apiClient.delete(`/users/${id}`)
  },

  // Search users ~~ not added in backend for demo
  searchUsers(query) {
    return apiClient.get('/users/search', {
      params: { q: query }
    })
  },

  // Get users with pagination
  getUsersPaginated(page = 1, size = 10, sortBy = 'name', sortDir = 'asc') {
    return apiClient.get('/users', {
      params: {
        page: page - 1, // Spring boot 0-indexed
        size,
        sort: `${sortBy},${sortDir}`
      }
    })
  }
}

export default userService