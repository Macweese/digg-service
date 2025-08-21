<template>
  <div class="fixed bottom-5 right-5 bg-green-500 text-white py-2 px-4 rounded-lg shadow-lg flex items-center fade-in-out">
    <CheckCircleIcon class="w-5 h-5 mr-2" />
    {{ message }}
  </div>
</template>

<script>
import { CheckCircleIcon } from './icons'

export default {
  name: 'ToastNotification',
  components: {
    CheckCircleIcon
  },
  props: {
    message: {
      type: String,
      required: true
    }
  }
}
</script>

<style scoped>
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