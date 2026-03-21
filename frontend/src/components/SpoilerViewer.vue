<template>
  <div class="spoiler-viewer">
    <!-- Tabs -->
    <div class="flex space-x-2 mb-6">
      <button
        @click="activeTab = 'list'"
        :class="[
          'px-6 py-3 rounded-xl font-semibold transition-all',
          activeTab === 'list'
            ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/50'
            : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
        ]"
      >
        <span class="flex items-center space-x-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
          </svg>
          <span>Item List</span>
        </span>
      </button>

      <button
        @click="activeTab = 'map'"
        :class="[
          'px-6 py-3 rounded-xl font-semibold transition-all',
          activeTab === 'map'
            ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/50'
            : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
        ]"
      >
        <span class="flex items-center space-x-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"/>
          </svg>
          <span>Map View</span>
        </span>
      </button>

      <button
        @click="activeTab = 'progression'"
        :class="[
          'px-6 py-3 rounded-xl font-semibold transition-all',
          activeTab === 'progression'
            ? 'bg-orange-500 text-white shadow-lg shadow-orange-500/50'
            : 'bg-slate-700 text-gray-300 hover:bg-slate-600'
        ]"
      >
        <span class="flex items-center space-x-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/>
          </svg>
          <span>Play Order</span>
        </span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-20">
      <div class="w-16 h-16 mx-auto mb-6 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
      <p class="text-xl text-gray-400">Loading spoiler data...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-500/10 backdrop-blur-lg rounded-2xl p-8 border border-red-500/30">
      <div class="text-center py-12">
        <svg class="w-20 h-20 mx-auto mb-6 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <h2 class="text-2xl font-bold text-red-400 mb-2">Error Loading Spoiler</h2>
        <p class="text-gray-400">{{ error }}</p>
      </div>
    </div>

    <!-- List View -->
    <div v-else-if="spoilerData && activeTab === 'list'" class="space-y-6">
      <div v-for="(placements, region) in spoilerData.placementsByRegion" :key="region" class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20">
        <h3 class="text-xl font-bold text-white mb-4 flex items-center space-x-3">
          <span :class="getRegionBadgeClass(region)" class="px-3 py-1 rounded-full text-sm font-semibold">
            {{ region }}
          </span>
          <span class="text-gray-400 text-sm">{{ placements.length }} items</span>
        </h3>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div
            v-for="placement in placements"
            :key="placement.locationId"
            :class="[
              'p-4 rounded-xl border transition-all hover:scale-[1.02]',
              placement.isProgression
                ? 'bg-orange-500/20 border-orange-500/50'
                : 'bg-slate-700/50 border-slate-600/50'
            ]"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1 min-w-0">
                <div class="flex items-center space-x-2 mb-1">
                  <span v-if="placement.isProgression" class="w-2 h-2 bg-orange-500 rounded-full flex-shrink-0"></span>
                  <h4 class="font-semibold text-white truncate">{{ placement.itemName }}</h4>
                </div>
                <p class="text-sm text-gray-400 truncate">{{ placement.locationName }}</p>
                <p class="text-xs text-gray-500 font-mono mt-1">{{ placement.locationId }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Map View -->
    <div v-else-if="spoilerData && activeTab === 'map'" class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20">
      <h3 class="text-xl font-bold text-white mb-4">Item Placement Map</h3>
      <p class="text-gray-400 mb-6">Visual map coming soon! For now, use the list view to see all item placements.</p>

      <!-- Simple region overview -->
      <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
        <div
          v-for="(placements, region) in spoilerData.placementsByRegion"
          :key="region"
          :class="[
            'p-6 rounded-xl border-2 cursor-pointer transition-all hover:scale-105',
            getRegionBorderClass(region)
          ]"
        >
          <h4 class="text-lg font-bold text-white mb-2">{{ region }}</h4>
          <p class="text-3xl font-bold mb-1">{{ placements.length }}</p>
          <p class="text-sm text-gray-400">items placed</p>

          <div class="mt-4 space-y-2">
            <div
              v-for="placement in placements.slice(0, 5)"
              :key="placement.locationId"
              class="text-sm truncate"
              :class="placement.isProgression ? 'text-orange-400' : 'text-gray-400'"
            >
              {{ placement.itemName }}
            </div>
            <div v-if="placements.length > 5" class="text-xs text-gray-500">
              +{{ placements.length - 5 }} more...
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Progression Guide -->
    <div v-else-if="spoilerData && activeTab === 'progression'" class="bg-white/10 backdrop-blur-lg rounded-2xl p-6 border border-white/20">
      <h3 class="text-xl font-bold text-white mb-4">Step-by-Step Progression Guide</h3>
      <p class="text-gray-400 mb-6">Play order guide coming soon! This will show the optimal path through the game.</p>

      <!-- Simple progression items list for now -->
      <div class="space-y-4">
        <h4 class="text-lg font-semibold text-white">Progression Items</h4>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div
            v-for="placement in getAllProgressionItems()"
            :key="placement.locationId"
            class="p-4 bg-orange-500/20 border border-orange-500/50 rounded-xl"
          >
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white font-bold text-sm">
                {{ placement.region.charAt(0) }}
              </div>
              <div>
                <h5 class="font-semibold text-white">{{ placement.itemName }}</h5>
                <p class="text-sm text-gray-400">{{ placement.locationName }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const props = defineProps({
  seedId: {
    type: String,
    required: true
  }
});

const activeTab = ref('list');
const loading = ref(true);
const error = ref(null);
const spoilerData = ref(null);

const regionColors = {
  'Brinstar': { bg: 'bg-green-500/20', border: 'border-green-500', text: 'text-green-400' },
  'Crateria': { bg: 'bg-blue-500/20', border: 'border-blue-500', text: 'text-blue-400' },
  'Norfair': { bg: 'bg-red-500/20', border: 'border-red-500', text: 'text-red-400' },
  'Maridia': { bg: 'bg-yellow-500/20', border: 'border-yellow-500', text: 'text-yellow-400' },
  'Wrecked': { bg: 'bg-purple-500/20', border: 'border-purple-500', text: 'text-purple-400' },
  'Tourian': { bg: 'bg-pink-500/20', border: 'border-pink-500', text: 'text-pink-400' }
};

const getRegionBadgeClass = (region) => {
  const colors = regionColors[region] || { bg: 'bg-gray-500/20', text: 'text-gray-400' };
  return `${colors.bg} ${colors.text} px-3 py-1 rounded-full text-sm font-semibold`;
};

const getRegionBorderClass = (region) => {
  const colors = regionColors[region] || { bg: 'bg-gray-500/10', border: 'border-gray-500' };
  return `${colors.bg} ${colors.border} border-2`;
};

const getAllProgressionItems = () => {
  if (!spoilerData.value) return [];

  const progressionItems = [];
  for (const region in spoilerData.value.placementsByRegion) {
    for (const placement of spoilerData.value.placementsByRegion[region]) {
      if (placement.isProgression) {
        progressionItems.push(placement);
      }
    }
  }
  return progressionItems;
};

const loadSpoilerData = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await fetch(`http://localhost:8080/seed/${props.seedId}/spoiler/data`);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    spoilerData.value = await response.json();
  } catch (err) {
    error.value = `Failed to load spoiler: ${err.message}`;
    console.error('Error loading spoiler:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadSpoilerData();
});
</script>
