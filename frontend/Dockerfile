# ===== 阶段一:构建——容器内装依赖并打包,不依赖宿主机预先打好的 dist =====
FROM node:22-alpine AS builder

WORKDIR /app

# 先装依赖:package*.json 不变就命中缓存
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ===== 阶段二:运行——只放 nginx 和静态文件 =====
FROM nginx:1.28-alpine

# 配置文件
COPY nginx.conf /etc/nginx/nginx.conf

# 前端文件
COPY --from=builder /app/dist /etc/nginx/html/

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
