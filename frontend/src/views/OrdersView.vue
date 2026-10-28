<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { queryOrderList, getOrderDetail, searchOrders } from '@/api/order'
import { formatDateTime, formatMoney } from '@/utils/format'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const searchDish = ref('')
const searching = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

const page = reactive({
  currentPage: 1,
  pageSize: 10,
})

async function loadOrders() {
  loading.value = true
  try {
    // 后端 page 从 0 开始,el-pagination 从 1 开始
    const res = await queryOrderList(page.currentPage - 1, page.pageSize)
    records.value = res.data?.records || []
    total.value = Number(res.data?.totalElements || 0)
  } finally {
    loading.value = false
  }
}

async function doSearch() {
  const dish = searchDish.value.trim()
  if (!dish) {
    ElMessage.warning('先输入菜名,例如:回锅肉')
    return
  }
  loading.value = true
  searching.value = true
  try {
    const res = await searchOrders(dish)
    records.value = res.data || []
    total.value = records.value.length
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  searchDish.value = ''
  searching.value = false
  page.currentPage = 1
  loadOrders()
}

function onPageChange() {
  loadOrders()
}

async function openDetail(row) {
  const res = await getOrderDetail(row.orderId)
  detail.value = res.data
  detailVisible.value = true
}

onMounted(loadOrders)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="searchDish"
        class="search-input"
        placeholder="按菜名查:谁点过回锅肉"
        clearable
        @keyup.enter="doSearch"
      />
      <el-button type="primary" @click="doSearch">搜索</el-button>
      <el-button v-if="searching" @click="resetSearch">返回全部</el-button>
      <span v-if="searching" class="hint">搜索结果: {{ total }} 单</span>
    </div>

    <el-table v-loading="loading" :data="records" empty-text="还没有订单">
      <el-table-column prop="orderId" label="订单号" width="90" />
      <el-table-column prop="username" label="下单人" width="110" />
      <el-table-column label="菜品" min-width="220">
        <template #default="{ row }">
          <el-tag
            v-for="(item, idx) in row.itemList || []"
            :key="idx"
            type="info"
            size="small"
            class="tag"
          >
            {{ item.dishName }} ×{{ item.count }}
          </el-tag>
          <span v-if="!row.itemList || !row.itemList.length">-</span>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ formatMoney(row.totalMoney) }}</template>
      </el-table-column>
      <el-table-column label="备注" min-width="120">
        <template #default="{ row }">{{ row.msg || '-' }}</template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!searching" class="pager">
      <el-pagination
        v-model:current-page="page.currentPage"
        v-model:page-size="page.pageSize"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        @current-change="onPageChange"
        @size-change="onPageChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="560px">
      <template v-if="detail">
        <div class="detail-head">
          <span>订单号:{{ detail.orderId }}</span>
          <span>下单人:{{ detail.username }}</span>
          <span>{{ formatDateTime(detail.createTime) }}</span>
        </div>
        <el-table :data="detail.itemList || []" size="small">
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ formatMoney(row.unitPrice) }}</template>
          </el-table-column>
          <el-table-column prop="count" label="数量" width="70" />
          <el-table-column label="小计" width="110">
            <template #default="{ row }">¥{{ formatMoney(row.subTotal) }}</template>
          </el-table-column>
        </el-table>
        <div class="detail-total">
          合计:<strong>¥{{ formatMoney(detail.totalMoney) }}</strong>
        </div>
        <div v-if="detail.msg" class="detail-msg">备注:{{ detail.msg }}</div>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.search-input {
  width: 260px;
}

.hint {
  font-size: 13px;
  color: var(--npc-text-3);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.tag {
  margin: 2px 4px 2px 0;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--npc-text-3);
}

.detail-total {
  margin-top: 12px;
  text-align: right;
}

.detail-msg {
  margin-top: 8px;
  font-size: 13px;
  color: var(--npc-text-3);
}
</style>
