import { createApp } from 'vue'
import App from './App.vue'
import axios from 'axios'

// Configure axios base URL
axios.defaults.baseURL = 'http://localhost:8080/api'

const app = createApp(App)
app.config.globalProperties.$axios = axios
app.mount('#app')