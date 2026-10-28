<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { queryMenu, createOrder } from '@/api/waiter'
import { formatMoney } from '@/utils/format'
import DishPhoto from '@/component/DishPhoto.vue'
import ImageCard from '@/component/ImageCard.vue'

const dishList = ref([])
const loading = ref(false)

// 购物车:菜单id -> {dish, count}
const cart = reactive(new Map())

const cartItems = computed(() => [...cart.values()].filter((i) => i.count > 0))
const cartCount = computed(() => cartItems.value.reduce((s, i) => s + i.count, 0))
const cartTotal = computed(() =>
  cartItems.value.reduce((s, i) => s + i.count * Number(i.dish.price), 0)
)

const orderResultVisible = ref(false)
const orderResult = ref(null)
const submitting = ref(false)

async function loadMenu() {
  loading.value = true
  try {
    const res = await queryMenu()
    dishList.value = res.data || []
  } finally {
    loading.value = false
  }
}

function add(dish) {
  const item = cart.get(dish.id)
  if (item) item.count += 1
  else cart.set(dish.id, { dish, count: 1 })
}

function remove(dishId) {
  const item = cart.get(dishId)
  if (item && item.count > 0) item.count -= 1
}

function clearCart() {
  cart.clear()
}

async function submitOrder() {
  if (!cartItems.value.length) {
    ElMessage.warning('先挑几道菜吧')
    return
  }
  submitting.value = true
  try {
    const res = await createOrder(
      cartItems.value.map((i) => ({ dishName: i.dish.name, count: i.count }))
    )
    orderResult.value = res.data
    orderResultVisible.value = true
    clearCart()
  } finally {
    submitting.value = false
  }
}

const imageCardRef = ref(null)

function changeImage(dish) {
  imageCardRef.value.open(dish)
}

onMounted(loadMenu)
</script>

<template>
  <div v-loading="loading">
    <el-row :gutter="16">
      <!-- 菜品区 -->
      <el-col :span="16">
        <el-row v-if="dishList.length" :gutter="16">
          <el-col v-for="dish in dishList" :key="dish.id" :span="8" class="dish-col">
            <el-card shadow="never" class="dish-card">
              <DishPhoto
                :dish-id="dish.id"
                :name="dish.name"
                :size="72"
                title="点击更换菜品照片"
                class="dish-photo"
                @click="changeImage(dish)"
              />
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-price">¥{{ formatMoney(dish.price) }}</div>
              <div class="dish-actions">
                <el-button
                  size="small"
                  :disabled="!cart.get(dish.id) || cart.get(dish.id).count === 0"
                  @click="remove(dish.id)"
                >
                  <el-icon><Minus /></el-icon>
                </el-button>
                <span class="dish-count">{{ cart.get(dish.id)?.count || 0 }}</span>
                <el-button size="small" type="primary" @click="add(dish)">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <div v-else class="empty">菜单空空如也</div>
      </el-col>

      <!-- 购物车 -->
      <el-col :span="8">
        <el-card shadow="never" class="cart-card">
          <template #header>
            <div class="cart-head">
              <span>购物车({{ cartCount }})</span>
              <el-button link :disabled="!cartCount" @click="clearCart">清空</el-button>
            </div>
          </template>

          <template v-if="cartItems.length">
            <div v-for="item in cartItems" :key="item.dish.id" class="cart-item">
              <span class="cart-name">{{ item.dish.name }}</span>
              <span class="cart-mid">×{{ item.count }}</span>
              <span class="cart-sum">¥{{ formatMoney(item.count * item.dish.price) }}</span>
            </div>
            <el-divider />
            <div class="cart-total">
              合计 <b>¥{{ formatMoney(cartTotal) }}</b>
            </div>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="submitting"
              @click="submitOrder"
            >
              提交订单
            </el-button>
          </template>
          <div v-else class="empty">还没选菜</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下单结果 -->
    <el-dialog v-model="orderResultVisible" title="下单成功" width="420px">
      <template v-if="orderResult">
        <div class="order-no">订单号 #{{ orderResult.orderId }}</div>
        <div v-for="item in orderResult.itemList" :key="item.dishName" class="order-line">
          <span>{{ item.dishName }} ×{{ item.count }}</span>
          <span>¥{{ formatMoney(item.price) }}</span>
        </div>
        <el-divider />
        <div class="order-line total">
          <span>合计</span>
          <span>¥{{ formatMoney(orderResult.totalPrice) }}</span>
        </div>
      </template>
      <template #footer>
        <el-button type="primary" @click="orderResultVisible = false">好的</el-button>
      </template>
    </el-dialog>

    <ImageCard ref="imageCardRef" @uploaded="loadMenu" />
  </div>
</template>

<style scoped>
.dish-col {
  margin-bottom: 16px;
}

.dish-card {
  text-align: center;
}

.dish-card :deep(.el-card__body) {
  padding: 20px 16px;
}

.dish-photo {
  margin: 0 auto;
  cursor: pointer;
}

.dish-name {
  margin-top: 12px;
  font-weight: 600;
  color: var(--npc-text);
}

.dish-price {
  margin: 6px 0 14px;
  color: var(--npc-accent);
}

.dish-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.dish-count {
  min-width: 20px;
  font-weight: 600;
  color: var(--npc-text);
}

.cart-card {
  position: sticky;
  top: 20px;
}

.cart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.cart-mid {
  color: var(--npc-text-3);
}

.cart-sum {
  color: var(--npc-accent);
}

.cart-total {
  margin-bottom: 14px;
  text-align: right;
  color: var(--npc-text-2);
}

.cart-total b {
  margin-left: 6px;
  font-size: 18px;
  color: var(--npc-accent);
}

.submit-btn {
  width: 100%;
}

.empty {
  padding: 32px 0;
  text-align: center;
  font-size: 13px;
  color: var(--npc-text-3);
}

.order-no {
  margin-bottom: 12px;
  font-weight: 600;
}

.order-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  color: var(--npc-text-2);
}

.order-line.total {
  font-size: 16px;
  font-weight: 600;
  color: var(--npc-text);
}

.order-line.total span:last-child {
  color: var(--npc-accent);
}
</style>
