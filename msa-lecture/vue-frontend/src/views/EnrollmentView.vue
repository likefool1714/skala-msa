<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-layout">
      <DashboardSidebar />

      <main class="main-content">
        <div class="content-header">
          <div>
            <span class="workspace-eyebrow">COLLECTION TRACKING</span>
            <h1 class="page-title">수거 신청 관리</h1>
            <p class="page-subtitle">결제부터 업체 확인, 수거 완료까지 진행 상태를 확인하세요.</p>
          </div>
          <div class="status-legend">
            <span><i class="dot waiting"></i>업체 확인</span>
            <span><i class="dot accepted"></i>수거 예정</span>
            <span><i class="dot completed"></i>수거 완료</span>
          </div>
        </div>

        <div v-if="loading" class="loading-center">
          <div class="spinner"></div>
        </div>

        <div v-else-if="enrollments.length" class="enrollment-list fade-in">
          <div v-for="item in enrollments" :key="item.id" class="enrollment-card">
            <div class="enroll-thumb" :class="getThumbBg(item.collectionService?.wasteType)">
              <div style="font-size:42px">♻️</div>
            </div>

            <div class="enroll-info">
              <span class="badge" :class="getBadge(item.collectionService?.wasteType)">
                {{ item.collectionService?.wasteType }}
              </span>
              <h3 class="enroll-title">{{ item.collectionService?.name }}</h3>
              <p class="enroll-instructor">{{ item.preferredCollectionDate }} · {{ item.preferredStartTime }}~{{ item.preferredEndTime }}</p>
              <p class="enroll-instructor">폐기물 정보: {{ item.wasteInformation }}</p>
            </div>

            <div class="enroll-status">
              <span
                :class="[
                  'status-badge',
                  getStatusClass(item.status)
                ]"
              >
                {{ getStatusLabel(item.status) }}
              </span>
              <router-link :to="`/courses/${item.collectionServiceId}`" class="btn btn-ghost btn-sm">
                서비스 보기
              </router-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <p class="empty-icon">📭</p>
          <p>등록된 수거 신청이 없습니다.</p>
          <router-link to="/courses" class="btn btn-primary" style="margin-top:16px;">
            수거 서비스 둘러보기
          </router-link>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import DashboardSidebar from '@/components/DashboardSidebar.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'

const router = useRouter()
const auth = useAuthStore()

const enrollments = ref([])
const loading = ref(true)

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const categoryConfig = {
  '일반의료폐기물': { bg: 'thumb-teal', badge: 'badge-teal' },
  '손상성폐기물': { bg: 'thumb-blue', badge: 'badge-blue' },
  '조직물류폐기물': { bg: 'thumb-purple', badge: 'badge-purple' },
}

function getThumbBg(cat) {
  return categoryConfig[cat]?.bg || 'thumb-gray'
}

function getBadge(cat) {
  return categoryConfig[cat]?.badge || 'badge-gray'
}

function getStatusLabel(status) {
  return ({
    PENDING: '결제 처리 중',
    CONFIRMED: '업체 확인 대기',
    ACCEPTED: '수거 예정',
    COMPLETED: '수거 완료',
    REJECTED: '업체 거절',
    CANCELLED: '신청 취소'
  })[status] || status || '상태 미확인'
}

function getStatusClass(status) {
  return ({
    CONFIRMED: 'status-waiting',
    ACCEPTED: 'status-accepted',
    COMPLETED: 'status-completed',
    REJECTED: 'status-rejected',
    CANCELLED: 'status-rejected'
  })[status] || 'status-pending'
}

function getThumbSrc(course) {
  const key = course?.thumbnail || categoryConfig[course?.category]?.thumb
  if (!key) return ''
  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return ''
  }
}

onMounted(async () => {
  // 강사는 이 페이지 접근 불가 → 마이페이지로 이동
  if (isInstructor.value) {
    console.warn('[EnrollmentView] instructor tried to access /enrollments, redirect to /mypage')
    router.replace('/mypage')
    return
  }

  try {
    const res = await enrollmentApi.getMyEnrollments()
    console.log('[EnrollmentView] my enrollments response:', res.data)

    if (Array.isArray(res.data?.data)) {
      enrollments.value = res.data.data
    } else if (Array.isArray(res.data)) {
      enrollments.value = res.data
    } else {
      enrollments.value = []
    }
  } catch (error) {
    console.error('[EnrollmentView] failed to load enrollments:', error)
    enrollments.value = []
  } finally {
    loading.value = false
  }
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

.main-content {
  min-width: 0;
}

.content-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
}

.page-subtitle { margin-top: 6px; color: var(--color-text-muted); font-size: 13px; }
.status-legend { display: flex; gap: 14px; padding: 10px 14px; border: 1px solid var(--color-border); border-radius: 14px; background: #fff; box-shadow: var(--shadow-sm); }
.status-legend span { display: flex; align-items: center; gap: 6px; color: var(--color-text-secondary); font-size: 10px; font-weight: 700; }
.status-legend .dot { width: 7px; height: 7px; border-radius: 50%; }
.dot.waiting { background: #f5b942; }
.dot.accepted { background: #5e72e4; }
.dot.completed { background: #2dce89; }

.enrollment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.enrollment-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 20px;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}

.enrollment-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.enroll-thumb {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.enroll-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 8px;
}

.thumb-teal {
  background: #E1F5EE;
}

.thumb-blue {
  background: #E6F1FB;
}

.thumb-purple {
  background: #EEEDFE;
}

.thumb-pink {
  background: #FBEAF0;
}

.thumb-gray {
  background: #F1EFE8;
}

.enroll-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.enroll-title {
  font-size: 15px;
  font-weight: 600;
}

.enroll-instructor {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.enroll-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #E1F5EE;
  color: #0F6E56;
}

.status-pending {
  background: #FAEEDA;
  color: #854F0B;
}

.status-waiting { background: #FAEEDA; color: #854F0B; }
.status-accepted { background: #E8F1FF; color: #2563EB; }
.status-completed { background: #E1F5EE; color: #0F6E56; }
.status-rejected { background: #FFF1F2; color: #BE123C; }

.btn-sm {
  padding: 7px 14px;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
