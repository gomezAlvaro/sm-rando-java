import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import { seedApi } from '../seedApi'

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: vi.fn(),
      post: vi.fn()
    }))
  }
}))

describe('seedApi', () => {
  let mockAxiosInstance

  beforeEach(() => {
    // Create mock axios instance
    mockAxiosInstance = {
      get: vi.fn(),
      post: vi.fn()
    }

    axios.create = vi.fn(() => mockAxiosInstance)

    // Reset mocks
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('generateSeed', () => {
    it('should generate seed with POST request', async () => {
      const mockRequest = {
        seed: 'test-seed',
        algorithm: 'foresight',
        difficulty: 'normal',
        enableSpoiler: true,
        qualityValidation: true
      }

      const mockResponse = {
        data: {
          seedId: '2024-03-test123',
          seed: 'test-seed',
          successful: true,
          algorithmUsed: 'foresight',
          qualityMetrics: {
            rating: 'Good',
            overallScore: 7.5,
            reachablePercentage: 92.3,
            difficultyAssessment: 'Moderate'
          },
          warnings: [],
          timestamp: '2024-03-19T10:30:00'
        }
      }

      mockAxiosInstance.post.mockResolvedValue(mockResponse)

      const result = await seedApi.generateSeed(mockRequest)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/seeds/generate', mockRequest)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle generation errors', async () => {
      const mockRequest = {
        seed: 'test-seed',
        algorithm: 'foresight',
        difficulty: 'normal',
        enableSpoiler: true,
        qualityValidation: true
      }

      const mockError = new Error('Generation failed')
      mockAxiosInstance.post.mockRejectedValue(mockError)

      await expect(seedApi.generateSeed(mockRequest)).rejects.toThrow('Generation failed')
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/seeds/generate', mockRequest)
    })

    it('should send request with correct content type', async () => {
      const mockRequest = { algorithm: 'foresight' }
      const mockResponse = { data: { seedId: 'test-id' } }

      mockAxiosInstance.post.mockResolvedValue(mockResponse)

      await seedApi.generateSeed(mockRequest)

      expect(axios.create).toHaveBeenCalledWith expect.objectContaining({
        headers: {
          'Content-Type': 'application/json'
        }
      })
    })
  })

  describe('getSeed', () => {
    it('should get seed details with GET request', async () => {
      const seedId = '2024-03-test123'

      const mockResponse = {
        data: {
          seedId: seedId,
          seed: 'test-seed',
          algorithmUsed: 'foresight',
          timestamp: '2024-03-19T10:30:00',
          qualityMetrics: {
            rating: 'Good',
            overallScore: 7.5,
            reachablePercentage: 92.3,
            difficultyAssessment: 'Moderate'
          },
          spoilerAvailable: true
        }
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.getSeed(seedId)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seeds/${seedId}`)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle get seed errors', async () => {
      const seedId = 'non-existent'

      const mockError = new Error('Seed not found')
      mockAxiosInstance.get.mockRejectedValue(mockError)

      await expect(seedApi.getSeed(seedId)).rejects.toThrow('Seed not found')
      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seeds/${seedId}`)
    })

    it('should handle special characters in seed ID', async () => {
      const seedId = '2024-03-test-123_special'

      const mockResponse = { data: { seedId: seedId } }
      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      await seedApi.getSeed(seedId)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seeds/${seedId}`)
    })
  })

  describe('getRecentSeeds', () => {
    it('should get recent seeds with GET request', async () => {
      const limit = 10

      const mockResponse = {
        data: [
          {
            seedId: '2024-03-seed1',
            seed: 'seed1',
            timestamp: '2024-03-19T10:30:00',
            algorithmUsed: 'foresight',
            rating: 'Good'
          },
          {
            seedId: '2024-03-seed2',
            seed: 'seed2',
            timestamp: '2024-03-19T10:25:00',
            algorithmUsed: 'basic',
            rating: 'Fair'
          }
        ]
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.getRecentSeeds(limit)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/seeds/recent', { params: { limit } })
      expect(result).toEqual(mockResponse.data)
    })

    it('should use default limit of 10 if not specified', async () => {
      const mockResponse = { data: [] }
      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      await seedApi.getRecentSeeds()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/seeds/recent', { params: { limit: 10 } })
    })

    it('should handle different limit values', async () => {
      const limits = [5, 10, 20, 50]

      for (const limit of limits) {
        const mockResponse = { data: [] }
        mockAxiosInstance.get.mockResolvedValue(mockResponse)

        await seedApi.getRecentSeeds(limit)

        expect(mockAxiosInstance.get).toHaveBeenCalledWith('/seeds/recent', { params: { limit } })
      }
    })

    it('should handle get recent seeds errors', async () => {
      const mockError = new Error('Failed to fetch recent seeds')
      mockAxiosInstance.get.mockRejectedValue(mockError)

      await expect(seedApi.getRecentSeeds()).rejects.toThrow('Failed to fetch recent seeds')
    })
  })

  describe('hasSpoiler', () => {
    it('should check spoiler availability with GET request', async () => {
      const seedId = '2024-03-test123'

      const mockResponse = {
        data: true
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.hasSpoiler(seedId)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seeds/${seedId}/spoiler`)
      expect(result).toEqual(true)
    })

    it('should return false when spoiler does not exist', async () => {
      const seedId = '2024-03-nospoiler'

      const mockResponse = {
        data: false
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.hasSpoiler(seedId)

      expect(result).toEqual(false)
    })

    it('should handle hasSpoiler errors', async () => {
      const seedId = 'non-existent'

      const mockError = new Error('Seed not found')
      mockAxiosInstance.get.mockRejectedValue(mockError)

      await expect(seedApi.hasSpoiler(seedId)).rejects.toThrow('Seed not found')
    })
  })

  describe('downloadSpoiler', () => {
    it('should download spoiler with GET request', async () => {
      const seedId = '2024-03-test123'

      const mockBlob = new Blob(['Test spoiler content'], { type: 'text/plain' })

      const mockResponse = {
        data: mockBlob
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.downloadSpoiler(seedId)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seed/${seedId}/spoiler`, {
        responseType: 'blob'
      })
      expect(result).toEqual(mockBlob)
    })

    it('should request blob response type', async () => {
      const seedId = 'test-id'

      const mockResponse = {
        data: new Blob([])
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      await seedApi.downloadSpoiler(seedId)

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(`/seed/${seedId}/spoiler`, {
        responseType: 'blob'
      })
    })

    it('should handle download errors', async () => {
      const seedId = 'non-existent'

      const mockError = new Error('Spoiler not found')
      mockAxiosInstance.get.mockRejectedValue(mockError)

      await expect(seedApi.downloadSpoiler(seedId)).rejects.toThrow('Spoiler not found')
    })

    it('should handle large spoiler files', async () => {
      const seedId = 'test-id'

      const largeContent = 'x'.repeat(1000000) // 1MB of data
      const mockBlob = new Blob([largeContent], { type: 'text/plain' })

      const mockResponse = {
        data: mockBlob
      }

      mockAxiosInstance.get.mockResolvedValue(mockResponse)

      const result = await seedApi.downloadSpoiler(seedId)

      expect(result).toEqual(mockBlob)
      expect(result.size).toBeGreaterThan(0)
    })
  })

  describe('API Configuration', () => {
    it('should create axios instance with correct base URL', () => {
      // The API should be created with the base URL from environment or default
      expect(axios.create).toHaveBeenCalled()

      const createCall = axios.create.mock.calls[0][0]
      expect(createCall.baseURL).toBeDefined()
    })

    it('should set default headers', () => {
      expect(axios.create).toHaveBeenCalledWith expect.objectContaining({
        headers: {
          'Content-Type': 'application/json'
        }
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle network errors gracefully', async () => {
      const networkError = new Error('Network Error')
      mockAxiosInstance.get.mockRejectedValue(networkError)

      await expect(seedApi.getSeed('test-id')).rejects.toThrow('Network Error')
    })

    it('should handle timeout errors', async () => {
      const timeoutError = new Error('Request timeout')
      mockAxiosInstance.post.mockRejectedValue(timeoutError)

      await expect(seedApi.generateSeed({ algorithm: 'foresight' })).rejects.toThrow('Request timeout')
    })

    it('should handle server errors (5xx)', async () => {
      const serverError = new Error('Internal Server Error')
      serverError.response = { status: 500 }
      mockAxiosInstance.get.mockRejectedValue(serverError)

      await expect(seedApi.getSeed('test-id')).rejects.toThrow('Internal Server Error')
    })

    it('should handle client errors (4xx)', async () => {
      const clientError = new Error('Not Found')
      clientError.response = { status: 404 }
      mockAxiosInstance.get.mockRejectedValue(clientError)

      await expect(seedApi.getSeed('non-existent')).rejects.toThrow('Not Found')
    })
  })

  describe('Request/Response Data Integrity', () => {
    it('should preserve request data format', async () => {
      const request = {
        seed: 'test-seed',
        algorithm: 'foresight',
        difficulty: 'hard',
        enableSpoiler: false,
        qualityValidation: true
      }

      const mockResponse = { data: { seedId: 'test-id' } }
      mockAxiosInstance.post.mockResolvedValue(mockResponse)

      await seedApi.generateSeed(request)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/seeds/generate', request)
    })

    it('should preserve response data format', async () => {
      const mockResponseData = {
        seedId: 'test-id',
        seed: 'test-seed',
        successful: true,
        algorithmUsed: 'foresight',
        qualityMetrics: {
          rating: 'Excellent',
          overallScore: 9.0,
          reachablePercentage: 100.0,
          difficultyAssessment: 'Hard'
        },
        warnings: [],
        timestamp: '2024-03-19T10:30:00'
      }

      mockAxiosInstance.get.mockResolvedValue({ data: mockResponseData })

      const result = await seedApi.getSeed('test-id')

      expect(result).toEqual(mockResponseData)
    })
  })
})