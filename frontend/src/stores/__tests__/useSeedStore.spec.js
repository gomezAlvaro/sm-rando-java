import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useSeedStore } from '../useSeedStore'

// Mock the seed API
vi.mock('../services/seedApi', () => ({
  seedApi: {
    generateSeed: vi.fn(),
    getSeed: vi.fn(),
    getRecentSeeds: vi.fn(),
    downloadSpoiler: vi.fn()
  }
}))

import { seedApi } from '../services/seedApi'

describe('useSeedStore', () => {
  let seedStore

  beforeEach(() => {
    // Create fresh pinia instance for each test
    setActivePinia(createPinia())
    seedStore = useSeedStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should have empty initial state', () => {
      expect(seedStore.currentSeed).toBe(null)
      expect(seedStore.recentSeeds).toEqual([])
      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe(null)
    })
  })

  describe('generateSeed', () => {
    it('should generate seed successfully', async () => {
      const mockResponse = {
        seedId: '2024-03-test123',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        qualityMetrics: {
          rating: 'Good',
          overallScore: 7.5,
          reachablePercentage: 92.3,
          difficultyAssessment: 'Moderate'
        },
        warnings: []
      }

      seedApi.generateSeed.mockResolvedValue(mockResponse)

      const request = {
        seed: 'test-seed',
        algorithm: 'foresight',
        difficulty: 'normal',
        enableSpoiler: true,
        qualityValidation: true
      }

      const result = await seedStore.generateSeed(request)

      expect(result).toEqual(mockResponse)
      expect(seedStore.currentSeed).toEqual(mockResponse)
      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe(null)
      expect(seedApi.generateSeed).toHaveBeenCalledWith(request)
    })

    it('should add generated seed to recent seeds', async () => {
      const mockResponse = {
        seedId: '2024-03-test123',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        qualityMetrics: { rating: 'Good' },
        warnings: []
      }

      seedApi.generateSeed.mockResolvedValue(mockResponse)

      await seedStore.generateSeed({ algorithm: 'foresight' })

      expect(seedStore.recentSeeds.length).toBe(1)
      expect(seedStore.recentSeeds[0]).toEqual(mockResponse)
    })

    it('should limit recent seeds to 10', async () => {
      // Generate 11 seeds
      for (let i = 0; i < 11; i++) {
        const mockResponse = {
          seedId: `2024-03-seed${i}`,
          seed: `seed${i}`,
          algorithmUsed: 'foresight',
          qualityMetrics: { rating: 'Good' },
          warnings: []
        }

        seedApi.generateSeed.mockResolvedValue(mockResponse)
        await seedStore.generateSeed({ algorithm: 'foresight' })
      }

      expect(seedStore.recentSeeds.length).toBe(10)
      expect(seedStore.recentSeeds[0].seedId).toBe('2024-03-seed10') // Most recent
      expect(seedStore.recentSeeds[9].seedId).toBe('2024-03-seed1') // Oldest kept
    })

    it('should handle generation errors', async () => {
      const error = new Error('Generation failed')
      seedApi.generateSeed.mockRejectedValue(error)

      const request = { algorithm: 'foresight' }

      await expect(seedStore.generateSeed(request)).rejects.toThrow('Generation failed')

      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe('Generation failed')
      expect(seedStore.currentSeed).toBe(null)
    })

    it('should set loading to true during generation', async () => {
      let resolveGeneration
      const generationPromise = new Promise(resolve => {
        resolveGeneration = resolve
      })

      seedApi.generateSeed.mockReturnValue(generationPromise)

      const request = { algorithm: 'foresight' }
      const generationCall = seedStore.generateSeed(request)

      expect(seedStore.loading).toBe(true)

      resolveGeneration({
        seedId: 'test-id',
        seed: 'test',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      })

      await generationCall

      expect(seedStore.loading).toBe(false)
    })

    it('should clear previous error before new generation', async () => {
      seedStore.error = 'Previous error'

      const mockResponse = {
        seedId: 'test-id',
        seed: 'test',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      }

      seedApi.generateSeed.mockResolvedValue(mockResponse)

      await seedStore.generateSeed({ algorithm: 'foresight' })

      expect(seedStore.error).toBe(null)
    })
  })

  describe('loadSeed', () => {
    it('should load seed successfully', async () => {
      const mockResponse = {
        seedId: '2024-03-test123',
        seed: 'test-seed',
        algorithmUsed: 'foresight',
        qualityMetrics: {
          rating: 'Good',
          overallScore: 7.5,
          reachablePercentage: 92.3,
          difficultyAssessment: 'Moderate'
        },
        warnings: []
      }

      seedApi.getSeed.mockResolvedValue(mockResponse)

      const result = await seedStore.loadSeed('2024-03-test123')

      expect(result).toEqual(mockResponse)
      expect(seedStore.currentSeed).toEqual(mockResponse)
      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe(null)
      expect(seedApi.getSeed).toHaveBeenCalledWith('2024-03-test123')
    })

    it('should handle load errors', async () => {
      const error = new Error('Seed not found')
      seedApi.getSeed.mockRejectedValue(error)

      await expect(seedStore.loadSeed('non-existent')).rejects.toThrow('Seed not found')

      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe('Seed not found')
      expect(seedStore.currentSeed).toBe(null)
    })

    it('should set loading to true during load', async () => {
      let resolveLoad
      const loadPromise = new Promise(resolve => {
        resolveLoad = resolve
      })

      seedApi.getSeed.mockReturnValue(loadPromise)

      const loadCall = seedStore.loadSeed('test-id')

      expect(seedStore.loading).toBe(true)

      resolveLoad({
        seedId: 'test-id',
        seed: 'test',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      })

      await loadCall

      expect(seedStore.loading).toBe(false)
    })
  })

  describe('loadRecentSeeds', () => {
    it('should load recent seeds successfully', async () => {
      const mockSeeds = [
        { seedId: 'seed1', seed: 'test1', algorithmUsed: 'foresight', qualityMetrics: {}, warnings: [] },
        { seedId: 'seed2', seed: 'test2', algorithmUsed: 'basic', qualityMetrics: {}, warnings: [] }
      ]

      seedApi.getRecentSeeds.mockResolvedValue(mockSeeds)

      const result = await seedStore.loadRecentSeeds(10)

      expect(result).toEqual(mockSeeds)
      expect(seedStore.recentSeeds).toEqual(mockSeeds)
      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe(null)
      expect(seedApi.getRecentSeeds).toHaveBeenCalledWith(10)
    })

    it('should use default limit of 10', async () => {
      seedApi.getRecentSeeds.mockResolvedValue([])

      await seedStore.loadRecentSeeds()

      expect(seedApi.getRecentSeeds).toHaveBeenCalledWith(10)
    })

    it('should handle load errors', async () => {
      const error = new Error('Failed to load recent seeds')
      seedApi.getRecentSeeds.mockRejectedValue(error)

      await expect(seedStore.loadRecentSeeds()).rejects.toThrow('Failed to load recent seeds')

      expect(seedStore.loading).toBe(false)
      expect(seedStore.error).toBe('Failed to load recent seeds')
    })
  })

  describe('downloadSpoiler', () => {
    it('should download spoiler successfully', async () => {
      const mockBlob = new Blob(['Test spoiler content'], { type: 'text/plain' })
      seedApi.downloadSpoiler.mockResolvedValue(mockBlob)

      // Mock DOM methods
      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
        style: {}
      }

      const originalCreateElement = document.createElement
      document.createElement = vi.fn(() => mockLink)
      const originalCreateObjectURL = window.URL.createObjectURL
      window.URL.createObjectURL = vi.fn(() => 'blob:test-url')
      const originalRevokeObjectURL = window.URL.revokeObjectURL

      await seedStore.downloadSpoiler('test-seed-id')

      expect(seedApi.downloadSpoiler).toHaveBeenCalledWith('test-seed-id')
      expect(window.URL.createObjectURL).toHaveBeenCalledWith(mockBlob)
      expect(mockLink.click).toHaveBeenCalled()

      // Restore original methods
      document.createElement = originalCreateElement
      window.URL.createObjectURL = originalCreateObjectURL
      window.URL.revokeObjectURL = originalRevokeObjectURL
    })

    it('should handle download errors', async () => {
      const error = new Error('Download failed')
      seedApi.downloadSpoiler.mockRejectedValue(error)

      await expect(seedStore.downloadSpoiler('test-seed-id')).rejects.toThrow('Download failed')

      expect(seedStore.error).toBe('Download failed')
    })
  })

  describe('clearCurrentSeed', () => {
    it('should clear current seed', () => {
      seedStore.currentSeed = {
        seedId: 'test-id',
        seed: 'test',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      }

      seedStore.clearCurrentSeed()

      expect(seedStore.currentSeed).toBe(null)
    })
  })

  describe('clearError', () => {
    it('should clear error', () => {
      seedStore.error = 'Test error'

      seedStore.clearError()

      expect(seedStore.error).toBe(null)
    })
  })

  describe('State Persistence', () => {
    it('should maintain state across operations', async () => {
      // Generate seed
      const generateResponse = {
        seedId: 'seed1',
        seed: 'test1',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      }

      seedApi.generateSeed.mockResolvedValue(generateResponse)
      await seedStore.generateSeed({ algorithm: 'foresight' })

      expect(seedStore.recentSeeds.length).toBe(1)

      // Load another seed
      const loadResponse = {
        seedId: 'seed2',
        seed: 'test2',
        algorithmUsed: 'basic',
        qualityMetrics: {},
        warnings: []
      }

      seedApi.getSeed.mockResolvedValue(loadResponse)
      await seedStore.loadSeed('seed2')

      // Recent seeds should still contain first seed
      expect(seedStore.recentSeeds.length).toBe(1)
      expect(seedStore.recentSeeds[0].seedId).toBe('seed1')
      expect(seedStore.currentSeed.seedId).toBe('seed2')
    })
  })

  describe('Concurrent Operations', () => {
    it('should handle multiple concurrent operations', async () => {
      const response1 = {
        seedId: 'seed1',
        seed: 'test1',
        algorithmUsed: 'foresight',
        qualityMetrics: {},
        warnings: []
      }

      const response2 = {
        seedId: 'seed2',
        seed: 'test2',
        algorithmUsed: 'basic',
        qualityMetrics: {},
        warnings: []
      }

      seedApi.generateSeed.mockResolvedValue(response1)
      seedApi.getSeed.mockResolvedValue(response2)

      // Start both operations concurrently
      const generatePromise = seedStore.generateSeed({ algorithm: 'foresight' })
      const loadPromise = seedStore.loadSeed('seed2')

      await Promise.all([generatePromise, loadPromise])

      expect(seedStore.currentSeed).toBeDefined()
    })
  })
})