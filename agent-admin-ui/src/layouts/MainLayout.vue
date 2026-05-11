<template>
  <n-layout has-sider position="absolute">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :collapsed="collapsed"
      show-trigger @collapse="collapsed = true" @expand="collapsed = false">
      <n-menu :collapsed-width="64" :collapsed="collapsed" :options="menuOptions"
        :value="currentPath" @update:value="handleMenuClick" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered style="height:56px;padding:0 16px;display:flex;align-items:center;justify-content:space-between">
        <div style="font-size:18px;font-weight:600">智能中心管理后台</div>
        <n-dropdown :options="dropdownOptions" @select="handleDropdown">
          <div style="cursor:pointer;display:flex;align-items:center;gap:8px">
            <n-avatar size="small">{{ userInfo?.nickName?.charAt(0) || 'A' }}</n-avatar>
            <span>{{ userInfo?.nickName || userInfo?.userName || '管理员' }}</span>
          </div>
        </n-dropdown>
      </n-layout-header>
      <n-layout-content content-style="padding:16px">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { NIcon } from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  HomeOutline, BuildOutline, SearchOutline, DocumentTextOutline,
  PersonOutline, PeopleOutline, LogOutOutline
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const collapsed = ref(false)

const currentPath = computed(() => route.path)

const iconMap: Record<string, any> = {
  HomeOutline, BuildOutline, SearchOutline, DocumentTextOutline,
  PersonOutline, PeopleOutline
}

function renderIcon(icon: string) {
  const comp = iconMap[icon]
  return comp ? () => h(NIcon, null, { default: () => h(comp) }) : undefined
}

const menuOptions: MenuOption[] = [
  { label: '仪表盘', key: '/dashboard', icon: renderIcon('HomeOutline') },
  {
    label: '智能中心', key: '/agent', icon: renderIcon('BuildOutline'),
    children: [
      { label: '在线医院', key: '/agent/hospital', icon: renderIcon('BuildOutline') },
      { label: '日志查询', key: '/agent/log', icon: renderIcon('SearchOutline') }
    ]
  },
  {
    label: '系统管理', key: '/system', icon: renderIcon('PeopleOutline'),
    children: [
      { label: '用户管理', key: '/system/user', icon: renderIcon('PersonOutline') },
      { label: '角色管理', key: '/system/role', icon: renderIcon('PeopleOutline') }
    ]
  }
]

function handleMenuClick(key: string) {
  router.push(key)
}

const dropdownOptions = [
  { label: '退出登录', key: 'logout', icon: () => h(NIcon, null, { default: () => h(LogOutOutline) }) }
]

async function handleDropdown(key: string) {
  if (key === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>
