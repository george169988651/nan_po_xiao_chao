import request from './request'

/**
 * 查询菜单
 * 后端返回 Menu 实体列表:[{id, name, price, createTime, updateTime}]
 */
export const queryMenu = () => request.get('/waiter/menu')

/**
 * 下单
 * @param {Array<{dishName:string, count:number}>} dishList
 * @returns {Promise<{data:{orderId:number, userId:number, username:string,
 *   itemList:Array<{dishName:string, count:number, price:number}>,
 *   totalMoney:number, totalPrice:number, msg:string}}>}
 */
export const createOrder = (dishList) => request.post('/waiter/order', { dishList })
