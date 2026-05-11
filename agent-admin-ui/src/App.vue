<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { zhCN, dateZhCN } from 'naive-ui'

const themeOverrides = {
  common: {
    borderRadius: '8px'
  },
  Input: {
    borderRadius: '8px'
  },
  Select: {
    borderRadius: '8px'
  },
  DatePicker: {
    borderRadius: '8px'
  },
  InputNumber: {
    borderRadius: '8px'
  }
}
</script>

<style>
.n-input {
  --n-border-radius: 8px;
}
.n-base-selection {
  --n-border-radius: 8px;
}
.n-date-picker .n-base-selection {
  --n-border-radius: 8px;
}
.n-input-number .n-input {
  --n-border-radius: 8px;
}
</style>
