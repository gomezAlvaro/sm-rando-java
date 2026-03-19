<template>
  <div class="generate">
    <div class="max-w-3xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold mb-2">
          <span class="bg-gradient-to-r from-orange-400 to-orange-600 bg-clip-text text-transparent">
            Generate Randomized Seed
          </span>
        </h1>
        <p class="text-gray-400">Customize your seed generation options</p>
      </div>

      <!-- Main Form Card -->
      <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-white/20 mb-6">
        <form @submit.prevent="generateSeed" class="space-y-6">
          <!-- Seed Input -->
          <div>
            <label class="block text-sm font-medium text-white mb-2">Seed (Optional)</label>
            <div class="relative">
              <input
                v-model="form.seed"
                type="text"
                placeholder="Leave blank for random seed"
                class="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
            </div>
            <p class="text-xs text-gray-500 mt-2">💡 Enter a custom seed or leave blank for automatic generation</p>
          </div>

          <!-- Algorithm Selection -->
          <div>
            <label class="block text-sm font-medium text-white mb-2">Algorithm</label>
            <div class="grid grid-cols-2 gap-4">
              <button
                type="button"
                @click="form.algorithm = 'foresight'"
                :class="[
                  'p-4 rounded-xl border-2 transition-all text-left',
                  form.algorithm === 'foresight'
                    ? 'border-orange-500 bg-orange-500/20'
                    : 'border-slate-700 hover:border-slate-600'
                ]"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="font-bold text-white">Foresight</span>
                  <span v-if="form.algorithm === 'foresight'" class="text-xs bg-green-500/20 text-green-400 px-2 py-1 rounded">
                    Recommended
                  </span>
                </div>
                <p class="text-sm text-gray-400">Advanced algorithm with reachability analysis</p>
              </button>

              <button
                type="button"
                @click="form.algorithm = 'basic'"
                :class="[
                  'p-4 rounded-xl border-2 transition-all text-left',
                  form.algorithm === 'basic'
                    ? 'border-orange-500 bg-orange-500/20'
                    : 'border-slate-700 hover:border-slate-600'
                ]"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="font-bold text-white">Basic</span>
                  <span v-if="form.algorithm === 'basic'" class="text-xs bg-blue-500/20 text-blue-400 px-2 py-1 rounded">
                    Simple
                  </span>
                </div>
                <p class="text-sm text-gray-400">Fast basic randomization</p>
              </button>
            </div>
          </div>

          <!-- Difficulty Selection -->
          <div>
            <label class="block text-sm font-medium text-white mb-2">Difficulty</label>
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="diff in difficulties"
                :key="diff.value"
                type="button"
                @click="form.difficulty = diff.value"
                :class="[
                  'p-3 rounded-xl border-2 transition-all text-center',
                  form.difficulty === diff.value
                    ? 'border-orange-500 bg-orange-500/20'
                    : 'border-slate-700 hover:border-slate-600'
                ]"
              >
                <div class="text-2xl mb-1">{{ diff.icon }}</div>
                <div class="text-xs font-semibold text-white">{{ diff.label }}</div>
              </button>
            </div>
          </div>

          <!-- Options -->
          <div>
            <label class="block text-sm font-medium text-white mb-2">Options</label>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <!-- Enable Spoiler -->
              <label class="bg-white/5 rounded-xl p-4 border border-white/10 flex items-center justify-between cursor-pointer hover:bg-white/10">
                <div class="flex items-center space-x-3">
                  <div class="w-10 h-10 bg-gradient-to-br from-blue-500 to-blue-700 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="font-semibold text-white text-sm">Enable Spoiler</div>
                    <div class="text-xs text-gray-400">Generate spoiler log</div>
                  </div>
                </div>
                <div class="w-12 h-6 bg-slate-700 rounded-full relative">
                  <input
                    v-model="form.enableSpoiler"
                    type="checkbox"
                    class="sr-only"
                  />
                  <div :class="[
                    'absolute top-1 left-1 w-4 h-4 bg-white rounded-full transition-transform duration-300',
                    form.enableSpoiler ? 'translate-x-6 bg-orange-500' : ''
                  ]"></div>
                </div>
              </label>

              <!-- Quality Validation -->
              <label class="bg-white/5 rounded-xl p-4 border border-white/10 flex items-center justify-between cursor-pointer hover:bg-white/10">
                <div class="flex items-center space-x-3">
                  <div class="w-10 h-10 bg-gradient-to-br from-green-500 to-green-700 rounded-lg flex items-center justify-center">
                    <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                  </div>
                  <div>
                    <div class="font-semibold text-white text-sm">Quality Validation</div>
                    <div class="text-xs text-gray-400">Validate seed quality</div>
                  </div>
                </div>
                <div class="w-12 h-6 bg-slate-700 rounded-full relative">
                  <input
                    v-model="form.qualityValidation"
                    type="checkbox"
                    class="sr-only"
                  />
                  <div :class="[
                    'absolute top-1 left-1 w-4 h-4 bg-white rounded-full transition-transform duration-300',
                    form.qualityValidation ? 'translate-x-6 bg-orange-500' : ''
                  ]"></div>
                </div>
              </label>
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="p-4 bg-red-500/10 border border-red-500/30 rounded-xl">
            <p class="text-red-300 text-sm">{{ error }}</p>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 disabled:from-slate-600 disabled:to-slate-700 text-white font-bold py-4 px-6 rounded-xl shadow-lg hover:shadow-xl transition-all flex items-center justify-center space-x-3 disabled:opacity-50"
          >
            <svg v-if="loading" class="w-5 h-5 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
            <span>{{ loading ? 'Generating...' : 'Generate Seed' }}</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = ref({
  seed: '',
  algorithm: 'foresight',
  difficulty: 'normal',
  enableSpoiler: true,
  qualityValidation: true
})

const loading = ref(false)
const error = ref('')

const difficulties = [
  { value: 'casual', label: 'Casual', icon: '😊' },
  { value: 'normal', label: 'Normal', icon: '😐' },
  { value: 'hard', label: 'Hard', icon: '😓' },
  { value: 'expert', label: 'Expert', icon: '😰' },
  { value: 'nightmare', label: 'Nightmare', icon: '😱' }
]

const generateSeed = async () => {
  loading.value = true
  error.value = ''

  try {
    console.log('Generating seed with:', form.value)
    await new Promise(resolve => setTimeout(resolve, 1500))
    router.push('/seed/demo-' + Date.now())
  } catch (err) {
    error.value = 'Failed to generate seed: ' + err.message
  } finally {
    loading.value = false
  }
}
</script>