import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import SeedDetailsView from '../../views/SeedDetailsView.vue'
import { useSeedStore } from '../../stores/useSeedStore'

// Mock vue-router
const mockRoute = {
  params: { id: 'test-seed-id' }
}

const mockRouter = {
  push: vi.fn()
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => mockRouter
}))

describe('SeedDetailsView Component', () => {
  let wrapper
  let pinia
  let seedStore

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    seedStore = useSeedStore()

    wrapper = mount(SeedDetailsView, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true
        }
      }
    })

    vi.clearAllMocks()
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  describe('Component Rendering', () => {
    it('should render the seed details container', () => {
      expect(wrapper.find('.seed-details').exists()).toBe(true)
    })

    it('should display title', () => {
      expect(wrapper.text()).toContain('Seed Details')
    })

    it('should render download spoiler button', () => {
      const button = wrapper.find('button')
      expect(button.exists()).toBe(true)
      expect(button.text()).toContain('Download Spoiler Log')
    })
  })

  describe('Loading State', () => {
    it('should show loading indicator when loading', async () => {
      seedStore.loading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Loading seed details...')
      expect(wrapper.find('.animate-spin').exists()).toBe(true)
    })

    it('should hide loading indicator when not loading', async () => {
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).not.toContain('Loading seed details...')
    })
  })

  describe('Error State', () => {
    it('should display error message when present', async () => {
      seedStore.error = 'Failed to load seed'
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Failed to load seed')
    })

    it('should not display error when no error', async () => {
      seedStore.error = ''
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).not.toContain('Failed')
    })
  })

  describe('Seed Display', () => {
    beforeEach(async () => {
      // Mock seed data
      seedStore.currentSeed = {
        seedId: 'test-seed-id',
        seed: 'test-seed-string',
        algorithmUsed: 'foresight',
        timestamp: '2024-03-19T10:30:00',
        qualityMetrics: {
          rating: 'Good',
          overallScore: 7.5,
          reachablePercentage: 92.3,
          difficultyAssessment: 'Moderate'
        },
        warnings: []
      }
      seedStore.loading = false
      seedStore.error = ''
      await wrapper.vm.$nextTick()
    })

    it('should display seed ID', () => {
      expect(wrapper.text()).toContain('test-seed-id')
    })

    it('should display seed string', () => {
      expect(wrapper.text()).toContain('test-seed-string')
    })

    it('should display algorithm used', () => {
      expect(wrapper.text()).toContain('foresight')
    })

    it('should display formatted timestamp', () => {
      expect(wrapper.text()).toContain('3/19/2024')
    })

    it('should display quality metrics section', () => {
      expect(wrapper.text()).toContain('Quality Metrics')
    })

    it('should display quality rating', () => {
      expect(wrapper.text()).toContain('Good')
    })

    it('should display overall score', () => {
      expect(wrapper.text()).toContain('7.5')
    })

    it('should display reachable percentage', () => {
      expect(wrapper.text()).toContain('92.3%')
    })

    it('should display difficulty assessment', () => {
      expect(wrapper.text()).toContain('Moderate')
    })
  })

  describe('Quality Metrics Display', () => {
    beforeEach(async () => {
      seedStore.currentSeed = {
        seedId: 'test-seed-id',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        timestamp: new Date().toISOString(),
        qualityMetrics: {
          rating: 'Excellent',
          overallScore: 9.0,
          reachablePercentage: 100.0,
          difficultyAssessment: 'Easy'
        },
        warnings: []
      }
      seedStore.loading = false
      await wrapper.vm.$nextTick()
    })

    it('should display Excellent rating in green', () => {
      const ratingElement = wrapper.find('.text-green-500')
      expect(ratingElement.exists()).toBe(true)
      expect(ratingElement.text()).toBe('Excellent')
    })

    it('should display Good rating in blue', async () => {
      seedStore.currentSeed.qualityMetrics.rating = 'Good'
      await wrapper.vm.$nextTick()

      const ratingElement = wrapper.find('.text-blue-500')
      expect(ratingElement.exists()).toBe(true)
      expect(ratingElement.text()).toBe('Good')
    })

    it('should display Fair rating in yellow', async () => {
      seedStore.currentSeed.qualityMetrics.rating = 'Fair'
      await wrapper.vm.$nextTick()

      const ratingElement = wrapper.find('.text-yellow-500')
      expect(ratingElement.exists()).toBe(true)
      expect(ratingElement.text()).toBe('Fair')
    })

    it('should display Poor rating in red', async () => {
      seedStore.currentSeed.qualityMetrics.rating = 'Poor'
      await wrapper.vm.$nextTick()

      const ratingElement = wrapper.find('.text-red-500')
      expect(ratingElement.exists()).toBe(true)
      expect(ratingElement.text()).toBe('Poor')
    })
  })

  describe('Warnings Display', () => {
    it('should display warnings when present', async () => {
      seedStore.currentSeed = {
        seedId: 'test-seed-id',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        timestamp: new Date().toISOString(),
        qualityMetrics: {
          rating: 'Fair',
          overallScore: 5.0,
          reachablePercentage: 85.0,
          difficultyAssessment: 'Moderate'
        },
        warnings: [
          'Some locations may be unreachable',
          'Consider using quality validation'
        ]
      }
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Warnings')
      expect(wrapper.text()).toContain('Some locations may be unreachable')
      expect(wrapper.text()).toContain('Consider using quality validation')
    })

    it('should not display warnings section when no warnings', async () => {
      seedStore.currentSeed = {
        seedId: 'test-seed-id',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        timestamp: new Date().toISOString(),
        qualityMetrics: {
          rating: 'Good',
          overallScore: 7.5,
          reachablePercentage: 100.0,
          difficultyAssessment: 'Moderate'
        },
        warnings: []
      }
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).not.toContain('Warnings')
    })
  })

  describe('Spoiler Download', () => {
    beforeEach(async () => {
      seedStore.currentSeed = {
        seedId: 'test-seed-id',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        timestamp: new Date().toISOString(),
        qualityMetrics: {
          rating: 'Good',
          overallScore: 7.5,
          reachablePercentage: 92.3,
          difficultyAssessment: 'Moderate'
        },
        warnings: []
      }
      seedStore.loading = false
      await wrapper.vm.$nextTick()
    })

    it('should call downloadSpoiler when button clicked', async () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      await wrapper.find('button').trigger('click')

      expect(consoleSpy).toHaveBeenCalledWith('Downloading spoiler for seed:', 'test-seed-id')
      consoleSpy.mockRestore()
    })
  })

  describe('Lifecycle Methods', () => {
    it('should call loadSeed on mount', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})

      wrapper = mount(SeedDetailsView, {
        global: {
          plugins: [pinia],
          stubs: {
            'router-link': true
          }
        }
      })

      expect(consoleSpy).toHaveBeenCalledWith('Loading seed:', 'test-seed-id')
      consoleSpy.mockRestore()
    })
  })

  describe('Empty State', () => {
    it('should not crash when seed is null', async () => {
      seedStore.currentSeed = null
      seedStore.loading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Seed Details')
      expect(wrapper.find('button').exists()).toBe(true)
    })
  })
})