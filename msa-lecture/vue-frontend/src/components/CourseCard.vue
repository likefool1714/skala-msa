<template>
  <router-link :to="`/courses/${course.id}`" class="course-card">
    <!-- 썸네일 -->
    <div class="card-thumb" :class="thumbBg">
      <div class="thumb-placeholder">{{ serviceIcon }}</div>
    </div>

    <!-- 내용 -->
    <div class="card-body">
      <span class="badge" :class="badgeClass">{{ displayWasteType }}</span>
      <h3 class="card-title">{{ course.name }}</h3>
      <div class="card-meta">
        <span class="instructor">수집·운반 업체 #{{ course.carrierId }}</span>
        <span class="price">₩{{ Number(course.price).toLocaleString() }}</span>
      </div>
      <div class="card-footer">
        <span class="enrolled">접수 {{ course.requestCount?.toLocaleString() || 0 }}건</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: { type: Object, required: true }
})

const categoryConfig = {
  '일반의료폐기물': { bg: 'thumb-teal', badge: 'badge-teal', icon: '♻️' },
  '손상성폐기물': { bg: 'thumb-blue', badge: 'badge-blue', icon: '💉' },
  '조직물류폐기물': { bg: 'thumb-purple', badge: 'badge-purple', icon: '🧪' },
}

const wasteTypeLabels = { GENERAL_MEDICAL: '일반의료폐기물', SHARPS: '손상성폐기물', PATHOLOGICAL: '조직물류폐기물' }
const displayWasteType = computed(() => wasteTypeLabels[props.course.wasteType] || props.course.wasteType)
const config = computed(() => categoryConfig[displayWasteType.value] || { bg: 'thumb-gray', badge: 'badge-gray', icon: '♻️' })
const thumbBg = computed(() => config.value.bg)
const badgeClass = computed(() => config.value.badge)
const serviceIcon = computed(() => config.value.icon)

</script>

<style scoped>
.course-card {
  display: flex;
  flex-direction: column;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: var(--transition);
  cursor: pointer;
  box-shadow: var(--shadow-sm);
}
.course-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-hover);
}
.card-thumb {
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb-teal   { background: linear-gradient(135deg, #e5fbf2, #c6f3df); }
.thumb-blue   { background: linear-gradient(135deg, #e7f5ff, #cbdfff); }
.thumb-amber  { background: #FAEEDA; }
.thumb-purple { background: linear-gradient(135deg, #f1ecff, #dacbff); }
.thumb-pink   { background: #FBEAF0; }
.thumb-gray   { background: #F1EFE8; }
.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 16px;
}
.thumb-placeholder {
  font-size: 68px;
  font-weight: 700;
  color: var(--color-text-muted);
  filter: drop-shadow(0 10px 18px rgba(50,50,93,.16));
  transition: transform .25s ease;
}
.course-card:hover .thumb-placeholder { transform: scale(1.08) rotate(-3deg); }
.card-body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.4;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.instructor {
  font-size: 12px;
  color: var(--color-text-secondary);
}
.price {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-primary);
}
.card-footer {
  margin-top: 2px;
}
.enrolled {
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>
