<template>
  <div id="app" class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- Header -->
    <header class="bg-white/10 backdrop-blur-lg border-b border-white/20 sticky top-0 z-50">
      <div class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-gradient-to-br from-orange-500 to-orange-700 rounded-lg flex items-center justify-center shadow-lg">
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
              </svg>
            </div>
            <div>
              <h1 class="text-xl font-bold text-white">Super Metroid Map Randomizer</h1>
            </div>
          </div>

          <nav class="flex items-center space-x-4">
            <router-link to="/" class="text-gray-300 hover:text-orange-400 transition-colors px-3 py-2 rounded-lg hover:bg-white/10">
              Home
            </router-link>
            <router-link to="/generate" class="text-gray-300 hover:text-orange-400 transition-colors px-3 py-2 rounded-lg hover:bg-white/10">
              Generate
            </router-link>
          </nav>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="container mx-auto px-4 py-8">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="bg-white/5 border-t border-white/10 mt-16">
      <div class="container mx-auto px-4 py-8">
        <div class="text-center text-gray-400 text-sm">
          <p>Super Metroid Map Randomizer - MVP Edition v1.0.0</p>
          <p class="mt-2">© 2024 Map Randomizer. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
// App root component
</script>