import request from './request'

/**
 * 上传菜品照片
 * 后端契约:POST /image/{id}/upload,multipart 字段名固定为 file
 * 不要手动设置 Content-Type,交给浏览器补上 multipart 边界
 * @param {number|string} id 菜品 id
 * @param {File} file
 */
export function uploadImage(id, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/image/${id}/upload`, form)
}

/**
 * 读取菜品照片(后端返回图片字节)。
 *
 * 图片接口同样需要令牌,用 <img src> 直接引用不会带上 Authorization,
 * 所以这里用 axios 取 blob(会自动附加请求头)。
 * silent:照片没上传过时后端返回 404,属于正常情况,不要弹全局错误提示。
 * @param {number|string} id 菜品 id
 * @returns {Promise<Blob>}
 */
export function fetchImageBlob(id) {
  return request.get(`/image/${id}`, { responseType: 'blob', _silent: true })
}
