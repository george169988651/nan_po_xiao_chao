/**
 * 时间展示:后端 LocalDateTime 序列化成 "2026-09-14T17:30:00"
 */
export function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

/**
 * 金额展示:后端 BigDecimal 序列化成数字
 */
export function formatMoney(value) {
  const n = Number(value)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}
