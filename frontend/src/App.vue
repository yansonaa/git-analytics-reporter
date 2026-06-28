<template>
  <div id="app">
    <el-container>
      <el-header style="background:#409EFF;color:#fff;display:flex;align-items:center;font-size:18px;">
        <span><Baseball /> Git Analytics Dashboard</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
body { margin: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; }
</style>
