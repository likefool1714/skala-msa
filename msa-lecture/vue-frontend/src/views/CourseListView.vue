<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-layout">
      <DashboardSidebar />

      <!-- 메인 -->
      <main class="main-content">
        <div class="content-header">
          <div>
            <span class="workspace-eyebrow">SERVICE CATALOG</span>
            <h1 class="page-title">의료폐기물 수거 서비스</h1>
            <p class="page-subtitle">{{ isInstructor ? '제공 중인 수거 서비스를 관리하고 새 상품을 등록하세요.' : '폐기물 유형과 조건에 맞는 수거 서비스를 찾아보세요.' }}</p>
          </div>

          <router-link
            v-if="isInstructor"
            to="/courses/new"
            class="btn btn-primary create-course-btn"
          >
            수거 서비스 등록
          </router-link>
        </div>

        <!-- 필터 -->
        <div class="filter-bar">
          <button
            v-for="cat in categories"
            :key="cat"
            :class="['filter-chip', { active: selectedCategory === cat }]"
            @click="selectCategory(cat)"
          >
            {{ cat }}
          </button>
        </div>

        <!-- 로딩 -->
        <div v-if="loading" class="loading-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="skeleton-thumb"></div>
            <div class="skeleton-body">
              <div class="skeleton-line short"></div>
              <div class="skeleton-line"></div>
              <div class="skeleton-line medium"></div>
            </div>
          </div>
        </div>

        <!-- 강의 그리드 -->
        <div v-else-if="filteredCourses.length" class="course-grid fade-in">
          <CourseCard
            v-for="course in filteredCourses"
            :key="course.id"
            :course="course"
          />
        </div>

        <!-- 빈 상태 -->
        <div v-else class="empty-state">
          <p>해당 폐기물 유형의 수거 서비스가 없습니다.</p>

          <router-link
            v-if="isInstructor"
            to="/courses/new"
            class="btn btn-primary empty-action-btn"
          >
            첫 수거 서비스 등록하기
          </router-link>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import DashboardSidebar from '@/components/DashboardSidebar.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useCourseStore } from '@/store/course.js'
import { useAuthStore } from '@/store/auth.js'

const courseStore = useCourseStore()
const auth = useAuthStore()

const { categories, loading } = courseStore

const selectedCategory = computed(() => courseStore.selectedCategory)
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const filteredCourses = computed(() => {
  if (!Array.isArray(courseStore.courses)) return []
  if (selectedCategory.value === '전체') return courseStore.courses
  return courseStore.courses.filter(c => c.wasteType === selectedCategory.value)
})

function selectCategory(cat) {
  courseStore.setCategory(cat)
}


onMounted(() => {
  courseStore.fetchCourses()
})
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.page-layout {
  max-width: 1320px;
  margin: 0 auto;
  padding: 32px 24px;
  display: grid;
  grid-template-columns: 248px 1fr;
  gap: 32px;
}

/* 사이드바 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: 8px;
}

.sidebar-label {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-muted);
  padding: 8px 12px 4px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--color-text-secondary);
  transition: var(--transition);
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-family: var(--font-sans);
  text-decoration: none;
}

.sidebar-item:hover {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

.sidebar-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 500;
}

.si-icon {
  font-size: 15px;
}

.sidebar-btn {
  color: var(--color-text-secondary);
}

/* 메인 */
.main-content {
  min-width: 0;
}

.content-header {
  margin-bottom: 22px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text-primary);
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.create-course-btn {
  white-space: nowrap;
  text-decoration: none;
}

/* 필터 */
.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 26px;
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: rgba(255,255,255,.78);
  box-shadow: var(--shadow-sm);
}

.filter-chip {
  padding: 7px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  border: 1.5px solid var(--color-border);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  transition: var(--transition);
  cursor: pointer;
}

.filter-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.filter-chip.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  border-color: var(--color-primary);
}

/* 강의 그리드 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 스켈레톤 */
.loading-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.skeleton-card {
  background: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.skeleton-thumb {
  height: 120px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-line.short {
  width: 40%;
}

.skeleton-line.medium {
  width: 70%;
}

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-muted);
  font-size: 15px;
}

.empty-action-btn {
  display: inline-flex;
  margin-top: 16px;
  text-decoration: none;
}

@media (max-width: 992px) {
  .page-layout {
    grid-template-columns: 1fr;
  }

  .course-grid,
  .loading-grid {
    grid-template-columns: 1fr;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
