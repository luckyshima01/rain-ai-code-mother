<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<template>
  <a-layout class="basic-layout">
    <a-layout-header class="layout-header">
      <GlobalHeader />
    </a-layout-header>

    <a-layout-content class="layout-content">
      <RouterView />
    </a-layout-content>

    <a-layout-footer class="layout-footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  padding: 0;
  height: auto;
  line-height: normal;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.layout-content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
}

.layout-footer {
  padding: 0;
  background: #f5f5f5;
  position: sticky;
  bottom: 0;
  z-index: 100;
}

@media (max-width: 768px) {
  .layout-content {
    padding: 16px 12px;
  }
}
</style>
