import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import GenerateView from '../../views/GenerateView.vue'
import { useSeedStore } from '../../stores/useSeedStore'

// Mock the router
const mockRouter = {
  push: vi.fn()
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter
}))

describe('GenerateView Component', () => {
  let wrapper
  let pinia

  beforeEach(() => {
    pinia = createPinia()
    wrapper = mount(GenerateView, {
      global: {
        plugins: [pinia]
      }
    })
    vi.clearAllMocks()
  })

  describe('Component Rendering', () => {
    it('should render the generate form', () => {
      expect(wrapper.find('form').exists()).toBe(true)
    })

    it('should render seed input field', () => {
      const seedInput = wrapper.find('input[type="text"]')
      expect(seedInput.exists()).toBe(true)
      expect(seedInput.attributes('placeholder')).toBe('Leave blank for random seed')
    })

    it('should render algorithm select', () => {
      const algorithmSelect = wrapper.find('select')
      expect(algorithmSelect.exists()).toBe(true)
    })

    it('should render difficulty select', () => {
      const difficultySelect = wrapper.findAll('select')[1]
      expect(difficultySelect.exists()).toBe(true)
    })

    it('should render enable spoiler checkbox', () => {
      const checkboxes = wrapper.findAll('input[type="checkbox"]')
      expect(checkboxes[0].exists()).toBe(true)
    })

    it('should render quality validation checkbox', () => {
      const checkboxes = wrapper.findAll('input[type="checkbox"]')
      expect(checkboxes[1].exists()).toBe(true)
    })

    it('should render generate button', () => {
      const button = wrapper.find('button[type="submit"]')
      expect(button.exists()).toBe(true)
      expect(button.text()).toBe('Generate Seed')
    })

    it('should display title', () => {
      expect(wrapper.text()).toContain('Generate Randomized Seed')
    })
  })

  describe('Form Initialization', () => {
    it('should initialize with default form values', () => {
      expect(wrapper.vm.form.algorithm).toBe('foresight')
      expect(wrapper.vm.form.difficulty).toBe('normal')
      expect(wrapper.vm.form.enableSpoiler).toBe(true)
      expect(wrapper.vm.form.qualityValidation).toBe(true)
      expect(wrapper.vm.form.seed).toBe('')
    })

    it('should not be loading initially', () => {
      expect(wrapper.vm.loading).toBe(false)
    })

    it('should not have error initially', () => {
      expect(wrapper.vm.error).toBe('')
    })
  })

  describe('User Input Handling', () => {
    it('should update seed input', async () => {
      const seedInput = wrapper.find('input[type="text"]')
      await seedInput.setValue('custom-seed-123')
      expect(wrapper.vm.form.seed).toBe('custom-seed-123')
    })

    it('should update algorithm selection', async () => {
      const algorithmSelect = wrapper.find('select')
      await algorithmSelect.setValue('basic')
      expect(wrapper.vm.form.algorithm).toBe('basic')
    })

    it('should update difficulty selection', async () => {
      const difficultySelect = wrapper.findAll('select')[1]
      await difficultySelect.setValue('hard')
      expect(wrapper.vm.form.difficulty).toBe('hard')
    })

    it('should toggle enable spoiler checkbox', async () => {
      const checkbox = wrapper.findAll('input[type="checkbox"]')[0]
      await checkbox.setChecked(false)
      expect(wrapper.vm.form.enableSpoiler).toBe(false)
    })

    it('should toggle quality validation checkbox', async () => {
      const checkbox = wrapper.findAll('input[type="checkbox"]')[1]
      await checkbox.setChecked(false)
      expect(wrapper.vm.form.qualityValidation).toBe(false)
    })
  })

  describe('Form Submission', () => {
    it('should call generateSeed on form submit', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      await wrapper.find('form').trigger('submit.prevent')

      expect(consoleSpy).toHaveBeenCalled()
      consoleSpy.mockRestore()
    })

    it('should set loading to true during submission', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button[type="submit"]')
      expect(button.text()).toBe('Generating...')
      expect(button.attributes('disabled')).toBeDefined()
    })

    it('should navigate to seed details after generation', async () => {
      await wrapper.vm.generateSeed()

      expect(mockRouter.push).toHaveBeenCalledWith('/seed/mock-seed-id')
    })
  })

  describe('Error Handling', () => {
    it('should display error message when generation fails', async () => {
      wrapper.vm.error = 'Generation failed: Test error'
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Generation failed: Test error')
    })

    it('should clear error before new generation', async () => {
      wrapper.vm.error = 'Previous error'

      await wrapper.vm.generateSeed()

      expect(wrapper.vm.error).toBe('')
    })
  })

  describe('Loading States', () => {
    it('should disable button while loading', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button[type="submit"]')
      expect(button.attributes('disabled')).toBeDefined()
    })

    it('should show loading text while loading', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button[type="submit"]')
      expect(button.text()).toBe('Generating...')
    })

    it('should enable button when not loading', async () => {
      wrapper.vm.loading = false
      await wrapper.vm.$nextTick()

      const button = wrapper.find('button[type="submit"]')
      expect(button.attributes('disabled')).toBeUndefined()
    })
  })

  describe('Form Validation', () => {
    it('should allow empty seed (auto-generate)', () => {
      wrapper.vm.form.seed = ''
      expect(wrapper.vm.form.seed).toBe('')
    })

    it('should accept custom seed value', () => {
      wrapper.vm.form.seed = 'my-custom-seed'
      expect(wrapper.vm.form.seed).toBe('my-custom-seed')
    })

    it('should accept all valid algorithms', () => {
      const validAlgorithms = ['foresight', 'basic']
      validAlgorithms.forEach(algo => {
        wrapper.vm.form.algorithm = algo
        expect(wrapper.vm.form.algorithm).toBe(algo)
      })
    })

    it('should accept all valid difficulties', () => {
      const validDifficulties = ['casual', 'normal', 'hard', 'expert', 'nightmare']
      validDifficulties.forEach(diff => {
        wrapper.vm.form.difficulty = diff
        expect(wrapper.vm.form.difficulty).toBe(diff)
      })
    })
  })
})