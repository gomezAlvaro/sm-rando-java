<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="text-center py-12">
      <h1 class="text-4xl md:text-5xl font-bold mb-6">
        <span class="bg-gradient-to-r from-orange-400 to-orange-600 bg-clip-text text-transparent">
          Welcome to Super Metroid Map Randomizer
        </span>
      </h1>

      <p class="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
        Generate randomized seeds with intelligent item placement and quality metrics
      </p>

      <div class="flex flex-col sm:flex-row items-center justify-center gap-4 mb-12">
        <router-link
          to="/generate"
          class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white font-bold py-3 px-8 rounded-xl shadow-lg hover:shadow-xl transition-all inline-flex items-center space-x-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
          </svg>
          <span>Generate Seed</span>
        </router-link>

        <a
          href="https://github.com"
          target="_blank"
          class="bg-white/10 hover:bg-white/20 text-white font-semibold py-3 px-8 rounded-xl transition-all inline-flex items-center space-x-2"
        >
          <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          <span>GitHub</span>
        </a>
      </div>
    </div>

    <!-- Features Section -->
    <div class="max-w-6xl mx-auto">
      <h2 class="text-3xl font-bold text-center mb-12 text-white">
        Powerful Features
      </h2>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
        <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20 hover:bg-white/15 transition-all">
          <div class="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-orange-500 to-orange-700 rounded-xl mb-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">Intelligent Placement</h3>
          <p class="text-gray-300">
            Advanced algorithms ensure seeds are beatable with proper progression paths
          </p>
        </div>

        <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20 hover:bg-white/15 transition-all">
          <div class="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-green-500 to-green-700 rounded-xl mb-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">Quality Metrics</h3>
          <p class="text-gray-300">
            Detailed analysis including reachability percentage and difficulty assessment
          </p>
        </div>

        <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20 hover:bg-white/15 transition-all">
          <div class="flex items-center justify-center w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-700 rounded-xl mb-4">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
            </svg>
          </div>
          <h3 class="text-xl font-bold text-white mb-2">Spoiler Logs</h3>
          <p class="text-gray-300">
            Optional spoiler logs show complete item placement for testing and verification
          </p>
        </div>
      </div>
    </div>

    <!-- Stats Section -->
    <div class="max-w-4xl mx-auto">
      <div class="bg-white/10 backdrop-blur-lg rounded-2xl p-8 border border-white/20">
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
          <div>
            <div class="text-3xl font-bold text-orange-400 mb-1">2</div>
            <div class="text-sm text-gray-300">Algorithms</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-orange-400 mb-1">5</div>
            <div class="text-sm text-gray-300">Difficulties</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-orange-400 mb-1">100%</div>
            <div class="text-sm text-gray-300">Free & Open</div>
          </div>
          <div>
            <div class="text-3xl font-bold text-orange-400 mb-1">∞</div>
            <div class="text-sm text-gray-300">Possibilities</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Home page component
</script>