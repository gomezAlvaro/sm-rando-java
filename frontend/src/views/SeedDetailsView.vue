<template>
  <div class="seed-details">
    <div class="max-w-6xl mx-auto">
      <!-- Loading State -->
      <div v-if="loading" class="text-center py-20">
        <div class="w-16 h-16 mx-auto mb-6 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
        <p class="text-xl text-gray-400">Loading seed details...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-white/20">
        <div class="text-center py-12">
          <svg class="w-20 h-20 mx-auto mb-6 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <h2 class="text-2xl font-bold text-red-400 mb-2">Error Loading Seed</h2>
          <p class="text-gray-400 mb-6">{{ error }}</p>
          <button @click="loadSeed" class="bg-orange-500 hover:bg-orange-600 text-white font-bold py-3 px-8 rounded-xl transition-colors">
            Try Again
          </button>
        </div>
      </div>

      <!-- Seed Details -->
      <div v-else-if="seed" class="space-y-8">
        <!-- Header -->
        <div class="text-center mb-12">
          <h1 class="text-4xl md:text-5xl font-bold mb-4">
            <span class="bg-gradient-to-r from-orange-400 to-orange-600 bg-clip-text text-transparent">
              Seed Generated Successfully!
            </span>
          </h1>
          <p class="text-xl text-gray-400">
            Your randomized seed is ready
          </p>
        </div>

        <!-- Seed Info Card -->
        <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-white/20">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-white flex items-center space-x-3">
              <svg class="w-8 h-8 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <span>Seed Information</span>
            </h2>
            <div class="flex items-center space-x-2">
              <span class="bg-green-500/20 text-green-400 px-3 py-1 rounded-full text-sm font-semibold">Complete</span>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Seed ID -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="flex items-center space-x-3 mb-3">
                <div class="w-10 h-10 bg-gradient-to-br from-orange-500 to-orange-700 rounded-lg flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"/>
                  </svg>
                </div>
                <div class="text-sm text-gray-400">Seed ID</div>
              </div>
              <div class="font-mono text-lg text-white">{{ seed.seedId }}</div>
            </div>

            <!-- Seed String -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="flex items-center space-x-3 mb-3">
                <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-700 rounded-lg flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"/>
                  </svg>
                </div>
                <div class="text-sm text-gray-400">Seed String</div>
              </div>
              <div class="font-mono text-lg text-white">{{ seed.seed }}</div>
            </div>

            <!-- Algorithm -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="flex items-center space-x-3 mb-3">
                <div class="w-10 h-10 bg-gradient-to-br from-green-500 to-green-700 rounded-lg flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                  </svg>
                </div>
                <div class="text-sm text-gray-400">Algorithm</div>
              </div>
              <div class="font-semibold text-white capitalize">{{ seed.algorithmUsed }}</div>
            </div>

            <!-- Generated At -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="flex items-center space-x-3 mb-3">
                <div class="w-10 h-10 bg-gradient-to-br from-purple-500 to-purple-700 rounded-lg flex items-center justify-center">
                  <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                </div>
                <div class="text-sm text-gray-400">Generated</div>
              </div>
              <div class="font-semibold text-white">{{ formatDate(seed.timestamp) }}</div>
            </div>
          </div>
        </div>

        <!-- Quality Metrics Card -->
        <div v-if="seed.qualityMetrics" class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-white/20">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-white flex items-center space-x-3">
              <svg class="w-8 h-8 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
              </svg>
              <span>Quality Metrics</span>
            </h2>
            <div :class="['px-3 py-1 rounded-full text-sm font-semibold', getRatingBadgeClass(seed.qualityMetrics.rating)]">
              {{ seed.qualityMetrics.rating }}
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- Overall Score -->
            <div class="text-center">
              <div class="relative pt-2">
                <div class="w-32 h-32 mx-auto mb-4 relative">
                  <svg class="w-32 h-32 transform -rotate-90">
                    <circle cx="64" cy="64" r="56" stroke="currentColor" stroke-width="8" fill="transparent" class="text-slate-700"/>
                    <circle
                      cx="64" cy="64" r="56"
                      :stroke="getScoreColor(seed.qualityMetrics.overallScore)"
                      stroke-width="8"
                      fill="transparent"
                      :stroke-dasharray="351.86"
                      :stroke-dashoffset="351.86 - (351.86 * seed.qualityMetrics.overallScore / 10)"
                      class="transition-all duration-1000"
                    />
                  </svg>
                  <div class="absolute inset-0 flex items-center justify-center">
                    <div class="text-center">
                      <div class="text-3xl font-bold text-white">{{ seed.qualityMetrics.overallScore?.toFixed(1) || 'N/A' }}</div>
                      <div class="text-xs text-gray-400">Score</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="font-semibold text-white">Overall Score</div>
              <div class="text-sm text-gray-400">Out of 10</div>
            </div>

            <!-- Reachable Percentage -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="text-center mb-4">
                <div class="text-4xl font-bold mb-2" :class="getPercentageColorClass(seed.qualityMetrics.reachablePercentage)">
                  {{ seed.qualityMetrics.reachablePercentage?.toFixed(1) || 'N/A' }}%
                </div>
                <div class="text-sm text-gray-400">Reachable</div>
              </div>
              <div class="w-full bg-slate-700 rounded-full h-3">
                <div
                  class="h-3 rounded-full transition-all duration-1000"
                  :class="getPercentageBarColor(seed.qualityMetrics.reachablePercentage)"
                  :style="{ width: (seed.qualityMetrics.reachablePercentage || 0) + '%' }"
                ></div>
              </div>
              <p class="text-xs text-gray-500 mt-3">
                {{ getReachabilityText(seed.qualityMetrics.reachablePercentage) }}
              </p>
            </div>

            <!-- Difficulty -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="flex items-center justify-center mb-4">
                <div class="text-5xl">
                  {{ getDifficultyEmoji(seed.qualityMetrics.difficultyAssessment) }}
                </div>
              </div>
              <div class="text-center">
                <div class="text-xl font-bold text-white mb-1">
                  {{ seed.qualityMetrics.difficultyAssessment || 'N/A' }}
                </div>
                <div class="text-sm text-gray-400">Difficulty</div>
              </div>
            </div>

            <!-- Backtracking -->
            <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
              <div class="text-center mb-4">
                <div class="text-4xl font-bold text-orange-400 mb-2">
                  {{ seed.qualityMetrics.backtrackingCount || 0 }}
                </div>
                <div class="text-sm text-gray-400">Backtracking Events</div>
              </div>
              <div class="flex justify-center space-x-1">
                <div
                  v-for="i in 10"
                  :key="i"
                  class="w-3 h-3 rounded-full"
                  :class="i <= (seed.qualityMetrics.backtrackingCount || 0) ? 'bg-orange-500' : 'bg-slate-700'"
                ></div>
              </div>
              <p class="text-xs text-gray-500 mt-3 text-center">
                {{ getBacktrackingText(seed.qualityMetrics.backtrackingCount) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Warnings -->
        <div v-if="seed.warnings && seed.warnings.length > 0" class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-yellow-500/30">
          <div class="flex items-start space-x-4">
            <div class="w-12 h-12 bg-yellow-500/20 rounded-xl flex items-center justify-center flex-shrink-0">
              <svg class="w-6 h-6 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-bold text-yellow-400 mb-3">Warnings</h3>
              <ul class="space-y-2">
                <li v-for="(warning, index) in seed.warnings" :key="index" class="flex items-start space-x-2 text-yellow-300">
                  <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                  </svg>
                  <span class="text-sm">{{ warning }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <button
            @click="downloadSpoiler"
            class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white font-bold p-6 rounded-xl shadow-lg hover:shadow-xl transition-all text-left group"
          >
            <div class="flex items-center space-x-4">
              <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
                </svg>
              </div>
              <div class="flex-1">
                <div class="font-bold text-white text-lg mb-1">Download Spoiler Log</div>
                <div class="text-sm text-gray-300">Get complete item placement information</div>
              </div>
              <svg class="w-6 h-6 text-white/50 group-hover:text-white group-hover:translate-x-1 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </div>
          </button>

          <router-link
            to="/generate"
            class="bg-white/10 hover:bg-white/20 text-white font-bold p-6 rounded-xl shadow-lg hover:shadow-xl transition-all text-left group"
          >
            <div class="flex items-center space-x-4">
              <div class="w-14 h-14 bg-white/10 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
                </svg>
              </div>
              <div class="flex-1">
                <div class="font-bold text-white text-lg mb-1">Generate Another Seed</div>
                <div class="text-sm text-gray-300">Create a new randomized seed</div>
              </div>
              <svg class="w-6 h-6 text-white/50 group-hover:text-white group-hover:translate-x-1 transition-all" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Spoiler Viewer Section -->
      <div v-if="seed" class="mt-12">
        <SpoilerViewer :seed-id="seed.seedId" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import SpoilerViewer from '@/components/SpoilerViewer.vue'

const route = useRoute()
const seed = ref(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  await loadSeed()
})

const loadSeed = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch(`http://localhost:8080/api/seeds/${route.params.id}`)
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }
    seed.value = await response.json()
  } catch (err) {
    error.value = 'Failed to load seed: ' + err.message
    console.error('Error loading seed:', err)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getRatingBadgeClass = (rating) => {
  const classes = {
    Excellent: 'bg-green-500/20 text-green-400',
    Good: 'bg-blue-500/20 text-blue-400',
    Fair: 'bg-yellow-500/20 text-yellow-400',
    Poor: 'bg-red-500/20 text-red-400'
  }
  return classes[rating] || 'bg-blue-500/20 text-blue-400'
}

const getScoreColor = (score) => {
  if (score >= 9) return 'text-green-500'
  if (score >= 7) return 'text-blue-500'
  if (score >= 5) return 'text-yellow-500'
  return 'text-red-500'
}

const getPercentageColorClass = (percentage) => {
  if (percentage >= 95) return 'text-green-400'
  if (percentage >= 85) return 'text-blue-400'
  if (percentage >= 70) return 'text-yellow-400'
  return 'text-red-400'
}

const getPercentageBarColor = (percentage) => {
  if (percentage >= 95) return 'bg-green-500'
  if (percentage >= 85) return 'bg-blue-500'
  if (percentage >= 70) return 'bg-yellow-500'
  return 'bg-red-500'
}

const getReachabilityText = (percentage) => {
  if (percentage >= 95) return 'Excellent reachability'
  if (percentage >= 85) return 'Very good reachability'
  if (percentage >= 70) return 'Good reachability'
  return 'Some areas may be unreachable'
}

const getDifficultyEmoji = (difficulty) => {
  const emojis = {
    'Easy': '😊',
    'Casual': '😌',
    'Moderate': '😐',
    'Normal': '🙂',
    'Hard': '😓',
    'Expert': '😰',
    'Nightmare': '😱'
  }
  return emojis[difficulty] || '❓'
}

const getBacktrackingText = (count) => {
  if (count <= 2) return 'Minimal backtracking'
  if (count <= 5) return 'Moderate backtracking'
  if (count <= 8) return 'Significant backtracking'
  return 'Heavy backtracking'
}

const downloadSpoiler = () => {
  const seedId = route.params.id
  window.open(`http://localhost:8080/seed/${seedId}/spoiler`, '_blank')
}
</script>