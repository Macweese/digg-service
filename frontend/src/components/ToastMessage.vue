<script lang="ts" setup>
defineProps<{ message: string }>();
</script>

<template>
  <div v-if="message"
       :style="{ userSelect: 'none', WebkitUserSelect: 'none' }"
       class="fixed bottom-5 text-justify md:align-middle font-350 right-5 bg-green-700/40 text-white py-2 px-4 rounded-lg shadow-lg flex items-center fade-in-out">
    <svg class="w-5 h-5 mr-2" fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
         stroke-width="2" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
      <polyline points="22 4 12 14.01 9 11.01"></polyline>
    </svg>
    {{ message }}
  </div>
</template>

<style>
.fade-in-out {
  animation: fadeInOut 3s ease-in-out forwards;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }
  15% {
    opacity: 1;
    transform: translateY(0);
  }
  85% {
    opacity: 1;
    transform: translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateY(20px);
  }
}
</style>