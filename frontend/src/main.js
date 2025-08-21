import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// Configure axios base URL
axios.defaults.baseURL = 'http://localhost:8080/digg'

const app = createApp(App)
app.use(router)
app.config.globalProperties.$axios = axios
app.mount('#app')