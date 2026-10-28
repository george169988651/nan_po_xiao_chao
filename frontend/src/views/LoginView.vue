<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { Food } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref()
const loginForm = reactive({ username: '', password: '' })
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerFormRef = ref()
const registerForm = reactive({ username: '', nickname: '', password: '', confirm: '' })
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名 2-20 个字符', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码 6-20 位', trigger: 'blur' },
  ],
  confirm: [
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await auth.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/menu')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await auth.register({
      username: registerForm.username,
      nickname: registerForm.nickname,
      password: registerForm.password,
    })
    ElMessage.success('注册成功,请登录')
    loginForm.username = registerForm.username
    registerForm.username = ''
    registerForm.nickname = ''
    registerForm.password = ''
    registerForm.confirm = ''
    activeTab.value = 'login'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card" shadow="never">
      <div class="login-head">
        <el-icon :size="24" class="login-icon"><Food /></el-icon>
        <h2>南坡小炒</h2>
        <p>成都理工大学工程技术学院 · 学生餐厅点餐系统</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="用户名" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="用户名" clearable />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="昵称(下单署名)" clearable />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirm">
              <el-input
                v-model="registerForm.confirm"
                type="password"
                placeholder="确认密码"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: var(--npc-bg);
}

.login-card {
  width: 380px;
  border: 1px solid var(--npc-border);
}

.login-card :deep(.el-card__body) {
  padding: 24px 28px 28px;
}

.login-head {
  text-align: center;
  margin-bottom: 12px;
}

.login-icon {
  color: var(--npc-accent);
}

.login-head h2 {
  margin: 8px 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: var(--npc-text);
}

.login-head p {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--npc-text-3);
}

.submit-btn {
  width: 100%;
}
</style>
