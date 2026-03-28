<template>
  <div id="userLoginPage">
    <h2 class="title">用户登录</h2>
    <div class="desc">不写一行代码，完整生成应用！</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>

      <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formState = reactive<FormState>({
  userAccount: '',
  userPassword: '',
})
/**
 * 提交表单
 * @param value
 */
const handleSubmit = async (value: any) => {
  const res = await userLogin(value)
  // 登录成功
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('登录失败', res.data.message)
  }
}
</script>
<style scoped>
#userLoginPage {
  max-width: 480px;
  margin: 50px auto;
}

.title {
  text-align: center;
  margin-bottom: 20px;
}

.desc {
  text-align: center;
  margin-bottom: 20px;
  color: #666;
  font-size: 14px;
}

.tips {
  margin-bottom: 20px;
  color: #999;
  font-size: 14px;
  text-align: right;
}
</style>
