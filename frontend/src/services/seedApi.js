import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * Seed API service for communicating with the backend
 */
export const seedApi = {
  /**
   * Generate a new seed
   */
  async generateSeed(request) {
    const response = await api.post('/seeds/generate', request)
    return response.data
  },

  /**
   * Get seed details by ID
   */
  async getSeed(seedId) {
    const response = await api.get(`/seeds/${seedId}`)
    return response.data
  },

  /**
   * Get recent seeds
   */
  async getRecentSeeds(limit = 10) {
    const response = await api.get('/seeds/recent', { params: { limit } })
    return response.data
  },

  /**
   * Check if spoiler exists for a seed
   */
  async hasSpoiler(seedId) {
    const response = await api.get(`/seeds/${seedId}/spoiler`)
    return response.data
  },

  /**
   * Download spoiler log for a seed
   */
  async downloadSpoiler(seedId) {
    const response = await api.get(`/seed/${seedId}/spoiler`, {
      responseType: 'blob'
    })
    return response.data
  }
}

export default seedApi