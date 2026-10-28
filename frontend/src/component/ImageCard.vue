<script setup>
import { onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/image'
import DishPhoto from '@/component/DishPhoto.vue'

// 与后端 spring.servlet.multipart.max-file-size 保持一致
const MAX_SIZE = 10 * 1024 * 1024

const visible = ref(false)
const dish = ref(null)
const file = ref(null)
const localPreview = ref('')
const uploading = ref(false)

const emit = defineEmits(['uploaded'])

function releasePreview() {
  if (localPreview.value) {
    URL.revokeObjectURL(localPreview.value)
    localPreview.value = ''
  }
}

/** 由父组件调用:传入选中的菜品 */
function open(target) {
  dish.value = target
  file.value = null
  releasePreview()
  visible.value = true
}

defineExpose({ open })

// el-upload 的 on-change:这里是 auto-upload=false,只负责挑文件
function onFileChange(uploadFile) {
  const raw = uploadFile?.raw
  if (!raw) return
  if (!raw.type.startsWith('image/')) {
    ElMessage.warning('只能上传图片文件')
    return
  }
  if (raw.size > MAX_SIZE) {
    ElMessage.warning('图片不能超过 10MB')
    return
  }
  releasePreview()
  file.value = raw
  localPreview.value = URL.createObjectURL(raw)
}

async function submit() {
  if (!file.value) {
    ElMessage.warning('请先选择一张图片')
    return
  }
  uploading.value = true
  try {
    await uploadImage(dish.value.id, file.value)
    ElMessage.success('照片已更新')
    visible.value = false
    emit('uploaded')
  } finally {
    uploading.value = false
  }
}

watch(visible, (v) => {
  if (!v) {
    file.value = null
    releasePreview()
  }
})
onBeforeUnmount(releasePreview)
</script>

<template>
  <el-dialog v-model="visible" title="菜品照片" width="420px" append-to-body>
    <div v-if="dish" class="body">
      <div class="current">
        <span class="label">当前</span>
        <DishPhoto :dish-id="dish.id" :name="dish.name" :size="72" />
      </div>
      <div v-if="localPreview" class="current">
        <span class="label">待上传</span>
        <img class="preview" :src="localPreview" alt="预览" />
      </div>

      <el-upload
        class="picker"
        accept="image/*"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="onFileChange"
      >
        <el-button>选择图片</el-button>
        <span class="hint">支持 jpg / png,不超过 10MB</span>
      </el-upload>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" :disabled="!file" @click="submit">
        上传
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.current {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  width: 48px;
  font-size: 13px;
  color: var(--npc-text-3);
}

.preview {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border: 1px solid var(--npc-border);
  border-radius: var(--npc-radius);
}

.picker :deep(.el-upload) {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hint {
  font-size: 12px;
  color: var(--npc-text-3);
}
</style>
