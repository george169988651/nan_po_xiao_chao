<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { fetchImageBlob } from '@/api/image'

const props = defineProps({
  dishId: { type: [Number, String], required: true },
  name: { type: String, default: '' },
  size: { type: Number, default: 64 },
})

const objectUrl = ref('')

function release() {
  if (objectUrl.value) {
    URL.revokeObjectURL(objectUrl.value)
    objectUrl.value = ''
  }
}

async function load(id) {
  release()
  if (id === null || id === undefined) return
  try {
    const blob = await fetchImageBlob(id)
    if (blob instanceof Blob && blob.size > 0) {
      objectUrl.value = URL.createObjectURL(blob)
    }
  } catch {
    // 没传过照片时后端返回 404,保持占位即可
  }
}

watch(() => props.dishId, load, { immediate: true })
onBeforeUnmount(release)
</script>

<template>
  <div class="photo" :style="{ width: `${size}px`, height: `${size}px` }">
    <img v-if="objectUrl" :src="objectUrl" :alt="name" />
    <span v-else class="placeholder">{{ (name || '菜').charAt(0) }}</span>
  </div>
</template>

<style scoped>
.photo {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
  background: var(--npc-bg);
}

.photo img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder {
  font-size: 18px;
  color: var(--npc-text-3);
  user-select: none;
}
</style>
