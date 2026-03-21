<template>
  <div class="map-display">
    <!-- SVG Map -->
    <svg viewBox="0 0 1200 800" class="w-full h-auto bg-slate-900 rounded-xl" @click="clearTooltip">
      <!-- Zone Connections -->
      <line x1="200" y1="300" x2="400" y2="200" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />
      <line x1="200" y1="350" x2="400" y2="400" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />
      <line x1="400" y1="250" x2="700" y2="300" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />
      <line x1="400" y1="450" x2="700" y2="400" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />
      <line x1="700" y1="350" x2="950" y2="250" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />
      <line x1="950" y1="350" x2="1050" y2="400" stroke="#4B5563" stroke-width="3" stroke-dasharray="8,4" />

      <!-- Crateria -->
      <rect x="50" y="250" width="300" height="200" rx="12" :class="getZoneClass('Crateria')" />
      <text x="200" y="320" text-anchor="middle" class="fill-white text-2xl font-bold">Crateria</text>
      <text x="200" y="345" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Crateria') }} items</text>

      <!-- Brinstar -->
      <rect x="400" y="100" width="300" height="200" rx="12" :class="getZoneClass('Brinstar')" />
      <text x="550" y="170" text-anchor="middle" class="fill-white text-2xl font-bold">Brinstar</text>
      <text x="550" y="195" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Brinstar') }} items</text>

      <!-- Norfair -->
      <rect x="400" y="400" width="300" height="200" rx="12" :class="getZoneClass('Norfair')" />
      <text x="550" y="470" text-anchor="middle" class="fill-white text-2xl font-bold">Norfair</text>
      <text x="550" y="495" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Norfair') }} items</text>

      <!-- Maridia -->
      <rect x="700" y="250" width="250" height="200" rx="12" :class="getZoneClass('Maridia')" />
      <text x="825" y="320" text-anchor="middle" class="fill-white text-2xl font-bold">Maridia</text>
      <text x="825" y="345" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Maridia') }} items</text>

      <!-- Wrecked Ship -->
      <rect x="950" y="200" width="200" height="150" rx="12" :class="getZoneClass('Wrecked')" />
      <text x="1050" y="265" text-anchor="middle" class="fill-white text-xl font-bold">Wrecked</text>
      <text x="1050" y="290" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Wrecked') }} items</text>

      <!-- Tourian -->
      <rect x="1000" y="400" width="150" height="150" rx="12" :class="getZoneClass('Tourian')" />
      <text x="1075" y="465" text-anchor="middle" class="fill-white text-xl font-bold">Tourian</text>
      <text x="1075" y="490" text-anchor="middle" class="fill-gray-300 text-sm">{{ getZoneItemCount('Tourian') }} items</text>

      <!-- Item Markers -->
      <g v-for="placement in allPlacements" :key="placement.locationId">
        <circle
          :cx="getItemX(placement.locationId, placement.region)"
          :cy="getItemY(placement.locationId, placement.region)"
          r="14"
          :class="getItemMarkerClass(placement)"
          @click.stop="showItemDetails(placement)"
          @mouseenter="showTooltip($event, placement)"
          @mouseleave="hideTooltip"
          class="cursor-pointer transition-all hover:scale-125"
          style="transform-box: fill-box; transform-origin: center;"
        />
      </g>
    </svg>

    <!-- Tooltip -->
    <div
      v-if="tooltip.visible"
      :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
      class="fixed bg-slate-800 text-white px-4 py-2 rounded-lg shadow-xl border border-slate-600 z-50 pointer-events-none"
    >
      <div class="font-semibold">{{ tooltip.placement?.itemName }}</div>
      <div class="text-sm text-gray-300">{{ tooltip.placement?.locationName }}</div>
      <div class="text-xs text-gray-400 mt-1">{{ tooltip.placement?.region }}</div>
    </div>

    <!-- Item Details Modal -->
    <div
      v-if="selectedItem"
      class="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50"
      @click="clearSelectedItem"
    >
      <div class="bg-slate-800 rounded-2xl p-8 max-w-md w-full mx-4 border border-slate-600 shadow-2xl" @click.stop>
        <div class="flex items-start justify-between mb-6">
          <div>
            <h3 class="text-2xl font-bold text-white mb-2">{{ selectedItem.itemName }}</h3>
            <p class="text-gray-400">{{ selectedItem.locationName }}</p>
          </div>
          <button
            @click="clearSelectedItem"
            class="text-gray-400 hover:text-white transition-colors"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>

        <div class="space-y-4">
          <div class="flex items-center space-x-3">
            <span :class="[
              'w-3 h-3 rounded-full',
              selectedItem.isProgression ? 'bg-orange-500' : 'bg-gray-500'
            ]"></span>
            <span class="text-gray-300">
              {{ selectedItem.isProgression ? 'Progression Item' : 'Resource Tank' }}
            </span>
          </div>

          <div class="bg-slate-700/50 rounded-lg p-4">
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="text-gray-400">Region:</span>
                <span class="text-white ml-2">{{ selectedItem.region }}</span>
              </div>
              <div>
                <span class="text-gray-400">Location ID:</span>
                <span class="text-white ml-2 font-mono">{{ selectedItem.locationId }}</span>
              </div>
            </div>
          </div>

          <div v-if="selectedItem.isProgression" class="bg-orange-500/10 border border-orange-500/30 rounded-lg p-4">
            <p class="text-orange-400 text-sm">
              <strong>Progression Item:</strong> Required to advance through the game
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Zone Legend -->
    <div class="mt-6 bg-white/10 backdrop-blur-lg rounded-xl p-6 border border-white/20">
      <h3 class="text-lg font-bold text-white mb-4">Zone Overview</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <div
          v-for="zone in zones"
          :key="zone.name"
          :class="[
            'p-4 rounded-xl border-2 cursor-pointer transition-all hover:scale-105',
            getZoneBorderClass(zone.name)
          ]"
          @click="scrollToZone(zone.name)"
        >
          <h4 class="text-lg font-bold text-white mb-1">{{ zone.name }}</h4>
          <p class="text-3xl font-bold mb-1">{{ getZoneItemCount(zone.name) }}</p>
          <p class="text-sm text-gray-400">items</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  spoilerData: {
    type: Object,
    required: true
  }
});

const tooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  placement: null
});

const selectedItem = ref(null);

const zones = [
  { name: 'Crateria', color: 'bg-blue-500/20', border: 'border-blue-500' },
  { name: 'Brinstar', color: 'bg-green-500/20', border: 'border-green-500' },
  { name: 'Norfair', color: 'bg-red-500/20', border: 'border-red-500' },
  { name: 'Maridia', color: 'bg-yellow-500/20', border: 'border-yellow-500' },
  { name: 'Wrecked', color: 'bg-purple-500/20', border: 'border-purple-500' },
  { name: 'Tourian', color: 'bg-pink-500/20', border: 'border-pink-500' }
];

const allPlacements = computed(() => {
  const placements = [];
  for (const region in props.spoilerData.placementsByRegion) {
    for (const placement of props.spoilerData.placementsByRegion[region]) {
      placements.push(placement);
    }
  }
  return placements;
});

const getZoneClass = (zoneName) => {
  const zone = zones.find(z => z.name === zoneName);
  return zone ? `${zone.color} ${zone.border} border-2` : 'bg-gray-500/20 border-gray-500 border-2';
};

const getZoneBorderClass = (zoneName) => {
  const zone = zones.find(z => z.name === zoneName);
  return zone ? `${zone.color} ${zone.border} border-2` : 'bg-gray-500/10 border-gray-500 border-2';
};

const getZoneItemCount = (zoneName) => {
  const placements = props.spoilerData.placementsByRegion[zoneName];
  return placements ? placements.length : 0;
};

const getItemMarkerClass = (placement) => {
  return placement.isProgression
    ? 'fill-orange-500 stroke-orange-300 stroke-2'
    : 'fill-gray-500 stroke-gray-300 stroke-2';
};

const getZoneBounds = (zoneName) => {
  const bounds = {
    'Crateria': { x: 50, y: 250, w: 300, h: 200 },
    'Brinstar': { x: 400, y: 100, w: 300, h: 200 },
    'Norfair': { x: 400, y: 400, w: 300, h: 200 },
    'Maridia': { x: 700, y: 250, w: 250, h: 200 },
    'Wrecked': { x: 950, y: 200, w: 200, h: 150 },
    'Tourian': { x: 1000, y: 400, w: 150, h: 150 }
  };
  return bounds[zoneName] || { x: 100, y: 100, w: 200, h: 200 };
};

const hashString = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return Math.abs(hash);
};

const getItemX = (locationId, region) => {
  const bounds = getZoneBounds(region);
  const padding = 30;
  const availableWidth = bounds.w - (padding * 2);
  const hash = hashString(locationId);
  const position = (hash % 100) / 100;
  return bounds.x + padding + (position * availableWidth);
};

const getItemY = (locationId, region) => {
  const bounds = getZoneBounds(region);
  const padding = 50;
  const availableHeight = bounds.h - (padding * 2);
  const hash = hashString(locationId + 'y');
  const position = (hash % 100) / 100;
  return bounds.y + padding + (position * availableHeight);
};

const showTooltip = (event, placement) => {
  tooltip.value = {
    visible: true,
    x: event.clientX + 15,
    y: event.clientY - 10,
    placement
  };
};

const hideTooltip = () => {
  tooltip.value.visible = false;
};

const clearTooltip = () => {
  tooltip.value.visible = false;
};

const showItemDetails = (placement) => {
  selectedItem.value = placement;
};

const clearSelectedItem = () => {
  selectedItem.value = null;
};

const scrollToZone = (zoneName) => {
  console.log('Scroll to zone:', zoneName);
};
</script>
