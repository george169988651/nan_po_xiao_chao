import request from './request'

/**
 * 分页查询订单
 * 后端返回 PageResult:{code, message, data: Order[], total}
 * Order: {id, userId, userName, orderList:number[](菜单id), message, createTime, updateTime}
 * @param {number} currentPage 从 1 开始
 * @param {number} pageSize
 */
export const queryOrderPage = (currentPage, pageSize) =>
  request.post('/order/page', { currentPage, pageSize })

/**
 * 第12课翻账本:分页查订单,返回 OrderPageVO
 * @param {number} page 从 0 开始
 * @param {number} size
 * @returns {Promise<{data:{records:Array, totalElements:number, totalPages:number,
 *   pageNum:number, pageSize:number}}>}
 */
export const queryOrderList = (page, size) =>
  request.get('/order/list', { params: { page, size } })

/** 订单详情:GET /order/{id} */
export const getOrderDetail = (id) => request.get(`/order/${id}`)

/** 按菜名查"谁点过这道菜":GET /order/search?dish=回锅肉 */
export const searchOrders = (dish) =>
  request.get('/order/search', { params: { dish } })

/**
 * AI 自然语言下单:POST /order/ai,请求体 {"message":"来份不辣的下饭菜"}
 * 返回 Result<OrderVO>
 */
export const orderByAi = (message) => request.post('/order/ai', { message })

