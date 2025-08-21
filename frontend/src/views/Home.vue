<template>
  <div class="min-h-screen bg-slate-50 dark:bg-slate-900 flex items-center justify-center">
    <div class="bg-white dark:bg-slate-800 p-8 rounded-lg shadow-lg text-center">
      <h1 class="text-3xl font-bold text-slate-900 dark:text-white mb-6">Customer Management System</h1>
      <p class="text-slate-600 dark:text-slate-300 mb-8">Choose your preferred interface style</p>

      <div class="space-y-4">
        <router-link
          to="/modern"
          class="block w-full px-6 py-3 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
        >
          Modern Dashboard
        </router-link>

        <router-link
          to="/minimal"
          class="block w-full px-6 py-3 bg-slate-600 text-white rounded-md hover:bg-slate-700 transition-colors"
        >
          Minimal View
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>