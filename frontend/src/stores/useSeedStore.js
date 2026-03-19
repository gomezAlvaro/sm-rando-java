import { defineStore } from 'pinia'
import { ref } from 'vue'
import { seedApi } from '../services/seedApi'

export const useSeedStore = defineStore('seed', () => {
  const currentSeed = ref(null)
  const recentSeeds = ref([])
  const loading = ref(false)
  const error = ref(null)

  /**
   * Generate a new seed
   */
  async function generateSeed(request) {
    loading.value = true
    error.value = null

    try {
      const response = await seedApi.generateSeed(request)
      currentSeed.value = response

      // Add to recent seeds
      recentSeeds.value.unshift(response)
      if (recentSeeds.value.length > 10) {
        recentSeeds.value.pop()
      }

      return response
    } catch (err) {
      error.value = err.message || 'Failed to generate seed'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load seed details
   */
  async function loadSeed(seedId) {
    loading.value = true
    error.value = null

    try {
      const response = await seedApi.getSeed(seedId)
      currentSeed.value = response
      return response
    } catch (err) {
      error.value = err.message || 'Failed to load seed'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Load recent seeds
   */
  async function loadRecentSeeds(limit = 10) {
    loading.value = true
    error.value = null

    try {
      const response = await seedApi.getRecentSeeds(limit)
      recentSeeds.value = response
      return response
    } catch (err) {
      error.value = err.message || 'Failed to load recent seeds'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Download spoiler log
   */
  async function downloadSpoiler(seedId) {
    try {
      const blob = await seedApi.downloadSpoiler(seedId)

      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${seedId}-spoiler.txt`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch (err) {
      error.value = err.message || 'Failed to download spoiler'
      throw err
    }
  }

  /**
   * Clear current seed
   */
  function clearCurrentSeed() {
    currentSeed.value = null
  }

  /**
   * Clear error
   */
  function clearError() {
    error.value = null
  }

  return {
    currentSeed,
    recentSeeds,
    loading,
    error,
    generateSeed,
    loadSeed,
    loadRecentSeeds,
    downloadSpoiler,
    clearCurrentSeed,
    clearError
  }
})