import {config} from '@vue/test-utils';

// Render teleported content in place and skip transitions in tests
config.global.stubs = {
    teleport: true,
    transition: true,
};