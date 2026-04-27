import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { computed } from 'vue'
import ColorModeSelect from '../../components/ColorModeSelect.vue'

const mockColorMode = {
  preference: 'system'
}

const mockAppConfig = {
  ui: {
    icons: {
      system: 'i-heroicons-computer-desktop',
      light: 'i-heroicons-sun',
      dark: 'i-heroicons-moon'
    }
  }
}

vi.stubGlobal('useColorMode', () => mockColorMode)
vi.stubGlobal('useAppConfig', () => mockAppConfig)
vi.stubGlobal('computed', computed)

const ClientOnly = {
  name: 'ClientOnly',
  template: '<div><slot /></div>'
}

const UButton = {
  name: 'UButton',
  template: '<button class="u-button" :aria-label="ariaLabel" @click="onClick"><slot /></button>',
  props: ['icon', 'variant', 'color', 'ariaLabel'],
  emits: ['click'],
  methods: {
    onClick() {
      this.$emit('click')
    }
  }
}

describe('ColorModeSelect', () => {
  beforeEach(() => {
    mockColorMode.preference = 'system'
  })

  it('renders correctly with system icon initially', () => {
    const wrapper = mount(ColorModeSelect, {
      global: {
        stubs: {
          ClientOnly,
          UButton
        }
      }
    })
    
    const button = wrapper.findComponent(UButton)
    expect(button.props('icon')).toBe('i-heroicons-computer-desktop')
  })

  it('cycles to light mode when clicked on system', async () => {
    const wrapper = mount(ColorModeSelect, {
      global: {
        stubs: {
          ClientOnly,
          UButton
        }
      }
    })
    
    await wrapper.find('.u-button').trigger('click')
    expect(mockColorMode.preference).toBe('light')
  })

  it('cycles to dark mode when clicked on light', async () => {
    mockColorMode.preference = 'light'
    const wrapper = mount(ColorModeSelect, {
      global: {
        stubs: {
          ClientOnly,
          UButton
        }
      }
    })
    
    await wrapper.find('.u-button').trigger('click')
    expect(mockColorMode.preference).toBe('dark')
  })

  it('cycles back to system mode when clicked on dark', async () => {
    mockColorMode.preference = 'dark'
    const wrapper = mount(ColorModeSelect, {
      global: {
        stubs: {
          ClientOnly,
          UButton
        }
      }
    })
    
    await wrapper.find('.u-button').trigger('click')
    expect(mockColorMode.preference).toBe('system')
  })
})
