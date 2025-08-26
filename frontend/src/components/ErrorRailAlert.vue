<script lang="ts" setup>
import {nextTick, ref, watch} from 'vue';

const props = defineProps<{
  message: string
}>();
const emit = defineEmits<{
  (e: 'cleared'): void
}>();

// Single collapsing wrapper for rail + alert panel
const showGroup = ref(false);
const showAlert = ref(false);

// replay animation on new message
watch(() => props.message, async (val) => {
  if (val) {
    if (showGroup.value) {
      showGroup.value = false;
      showAlert.value = false;
      await nextTick();
    }
    showAlert.value = false;
    showGroup.value = true;
  }
});

const RAIL_MS = 350;
const ALERT_MS = 280;

function beforeEnter(el: HTMLElement) {
  el.style.overflow = 'hidden';
  el.style.height = '0px';
  el.classList.remove('leaving');
}

function enter(el: HTMLElement, done: () => void) {
  requestAnimationFrame(() => {
    el.style.transition = `height ${RAIL_MS}ms ease`;
    el.style.height = '3px';
  });

  setTimeout(async () => {
    showAlert.value = true;
    await nextTick();
    const target = el.scrollHeight;
    el.style.transition = `height ${ALERT_MS}ms ease`;
    void el.offsetHeight;
    el.style.height = target + 'px';
    const onEnd = (e: TransitionEvent) => {
      if (e.propertyName === 'height') {
        el.removeEventListener('transitionend', onEnd);
        done();
      }
    };
    el.addEventListener('transitionend', onEnd);
  }, RAIL_MS);
}

function afterEnter(el: HTMLElement) {
  el.style.height = '';
  el.style.overflow = '';
  el.style.transition = '';
}

function beforeLeave(el: HTMLElement) {
  el.classList.add('leaving');
  el.style.overflow = 'hidden';
  el.style.height = el.scrollHeight + 'px';
}

function leave(el: HTMLElement, done: () => void) {
  requestAnimationFrame(() => {
    el.style.transition = `height ${ALERT_MS}ms ease`;
    void el.offsetHeight;
    el.style.height = '0px';
    const onEnd = (e: TransitionEvent) => {
      if (e.propertyName === 'height') {
        el.removeEventListener('transitionend', onEnd);
        done();
      }
    };
    el.addEventListener('transitionend', onEnd);
  });
}

function afterLeave() {
  showAlert.value = false;
  emit('cleared');
}

function dismiss() {
  showGroup.value = false;
}
</script>

<template>
  <div class="m-1 mb-5">
    <Transition
        @enter="enter"
        @leave="leave"
        @before-enter="beforeEnter"
        @after-enter="afterEnter"
        @before-leave="beforeLeave"
        @after-leave="afterLeave"
    >
      <div v-if="showGroup" class="error-group-wrapper">
        <div class="relative">
          <div class="error-rail"></div>
          <span aria-hidden="true" class="rail-dot"></span>
        </div>

        <div
            :class="{ shown: showAlert }"
            class="border-s-4 border-red-400/50 bg-red-800/30 p-4 rounded-sm alert-panel"
            role="alert"
        >
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-2 text-red-400">
              <svg class="h-6 w-6 shrink-0 stroke-current" fill="none" viewBox="0 0 24 24"
                   xmlns="http://www.w3.org/2000/svg">
                <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke-linecap="round" stroke-linejoin="round"
                      stroke-width="2"/>
              </svg>
              <strong class="font-normal">Something went wrong</strong>
            </div>
            <button
                aria-label="Dismiss error"
                class="flex justify-self-end text-red-400 cursor-pointer hover:text-red-300 transition-colors"
                title="Close"
                type="button"
                @click="dismiss"
            >
              <svg class="fill-current h-6 w-6 text-red-400" role="button" viewBox="0 0 20 20"
                   xmlns="http://www.w3.org/2000/svg">
                <title>Close</title>
                <path
                    d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"/>
              </svg>
            </button>
          </div>
          <p class="mt-2 text-sm text-red-300">
            <strong class="font-medium">Error&nbsp;</strong> {{ message }}
          </p>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style>
.error-group-wrapper {
  will-change: height;
}

/* Rail */
.error-rail {
  position: relative;
  height: 3px;
  width: 100%;
  border-radius: 9999px;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0) 0%, #fca5a5 15%, #ef4444 50%, #fca5a5 85%, rgba(0, 0, 0, 0) 100%);
  transform-origin: center;
  transform: scaleX(0);
  animation: rail-grow 350ms ease-out forwards;
}

.rail-dot {
  position: absolute;
  top: -3px;
  left: 50%;
  width: 8px;
  height: 8px;
  background-color: #ef4444;
  border-radius: 9999px;
  transform: translateX(-50%);
  animation: rail-dot-fade 350ms ease-out forwards;
}

@keyframes rail-grow {
  0% {
    transform: scaleX(0);
    opacity: 0.6;
  }
  60% {
    transform: scaleX(0.9);
    opacity: 1;
  }
  100% {
    transform: scaleX(1);
    opacity: 1;
  }
}

@keyframes rail-dot-fade {
  0% {
    opacity: 1;
  }
  60% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

/* Alert fade/slide */
.alert-panel {
  border-left-width: 4px;
  opacity: 0;
  transform: translateY(-8px);
  transition: opacity 220ms ease, transform 220ms ease;
  will-change: opacity, transform;
}

.alert-panel.shown {
  opacity: 1;
  transform: translateY(0);
}

/* Leaving: fade children while wrapper collapses */
.error-group-wrapper.leaving .alert-panel,
.error-group-wrapper.leaving .error-rail,
.error-group-wrapper.leaving .rail-dot {
  opacity: 0;
  transform: translateY(-6px);
  transition: opacity 220ms ease, transform 220ms ease;
}
</style>