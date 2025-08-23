import { afterEach, vi } from 'vitest';
// import { cleanup } from '@testing-library/vue';

// afterEach(() => cleanup());

// Some components may call scrollTo; add a no-op to avoid jsdom errors.
global.scrollTo = () => {};

// Reset all mocks between tests.
afterEach(() => {
  vi.clearAllMocks();
});