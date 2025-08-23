<script setup lang="ts">
const props = defineProps<{ message: string }>();
</script>

<template>
  <div v-if="message" class="fixed bottom-5 text-justify md:align-middle font-350 right-5 bg-green-700/40 text-white py-2 px-4 rounded-lg shadow-lg flex items-center fade-in-out" :style="{ userSelect: 'none', WebkitUserSelect: 'none' }">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-5 h-5 mr-2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
    {{ message }}
  </div>
</template>

<style>
.fade-in-out {
  animation: fadeInOut 3s ease-in-out forwards;
}
@keyframes fadeInOut {
  0% { opacity: 0; transform: translateY(20px); }
  15% { opacity: 1; transform: translateY(0); }
  85% { opacity: 1; transform: translateY(0); }
  100% { opacity: 0; transform: translateY(20px); }
}
</style>