// Ensures a DOM exists even if the environment isn't jsdom for any reason...
if (typeof document === 'undefined') {
  const { JSDOM } = await import('jsdom');
  const dom = new JSDOM('<!doctype html><html><body></body></html>', {
    url: 'http://localhost/',
  });

  const win = dom.window as unknown as Window & typeof globalThis;
  globalThis.window = win;
  globalThis.document = win.document;
  globalThis.HTMLElement = win.HTMLElement;
  globalThis.Node = win.Node;
  globalThis.customElements = win.customElements;
  globalThis.navigator = win.navigator;

  win.matchMedia ??= () => ({
    matches: false,
    media: '',
    onchange: null,
    addListener() {},
    removeListener() {},
    addEventListener() {},
    removeEventListener() {},
    dispatchEvent() { return false; },
  });
}