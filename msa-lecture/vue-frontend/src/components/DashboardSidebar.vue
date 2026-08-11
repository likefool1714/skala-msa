<template>
  <aside class="dashboard-sidebar">
    <div>
      <p class="sidebar-kicker">WORKSPACE</p>
      <nav class="dashboard-nav">
        <router-link to="/mypage" class="dashboard-link" :class="{ active: route.path === '/mypage' }">
          <span class="link-icon">◈</span>
          <span class="link-copy"><strong>대시보드</strong><small>업무 현황 요약</small></span>
        </router-link>
        <router-link to="/courses" class="dashboard-link" :class="{ active: route.path === '/courses' }">
          <span class="link-icon">▦</span>
          <span class="link-copy"><strong>수거 서비스</strong><small>서비스 조회·관리</small></span>
        </router-link>
        <router-link
          v-if="isInstructor"
          to="/mypage?requestStatus=CONFIRMED"
          class="dashboard-link"
          :class="{ active: route.path === '/mypage' && route.query.requestStatus === 'CONFIRMED' }"
		  @click="markWaitingAsRead"
        >
          <span class="link-icon">◷</span>
          <span class="link-copy"><strong>업체 확인 대기</strong><small>신규 신청 {{ waitingCount }}건</small></span>
          <em v-if="waitingCount" class="waiting-badge">{{ waitingCount }}</em>
        </router-link>
        <router-link
          v-if="isInstructor"
          to="/courses/new"
          class="dashboard-link"
          :class="{ active: route.path === '/courses/new' }"
        >
          <span class="link-icon">＋</span>
          <span class="link-copy"><strong>서비스 등록</strong><small>새 수거 상품 등록</small></span>
        </router-link>
        <router-link
          v-else
          to="/enrollments?status=result"
          class="dashboard-link"
          :class="{ active: route.path === '/enrollments' && route.query.status === 'result' }"
		  @click="markResultsAsRead"
        >
          <span class="link-icon">◉</span>
          <span class="link-copy"><strong>신청 상태 알림</strong><small>처리 결과 {{ resultCount }}건</small></span>
          <em v-if="resultCount" class="waiting-badge">{{ resultCount }}</em>
        </router-link>
        <router-link
          v-if="!isInstructor"
          to="/enrollments"
          class="dashboard-link"
          :class="{ active: route.path === '/enrollments' && !route.query.status }"
        >
          <span class="link-icon">✓</span>
          <span class="link-copy"><strong>신청 관리</strong><small>전체 수거 진행 상태</small></span>
        </router-link>
      </nav>
    </div>

    <div class="sidebar-account">
      <div class="account-avatar">{{ (auth.user?.organizationName || auth.user?.name)?.charAt(0) || '?' }}</div>
      <div class="account-copy">
        <strong>{{ auth.user?.organizationName || auth.user?.name || '사용자' }}</strong>
        <span>{{ isInstructor ? '수집·운반 업체' : '병원·의원' }}</span>
      </div>
      <button type="button" class="logout-icon" title="로그아웃" @click="logout">→</button>
    </div>
  </aside>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { enrollmentApi } from '@/api/enrollment.js'

const route = useRoute()
const auth = useAuthStore()
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')
const waitingCount = ref(0)
const resultCount = ref(0)
const waitingSignatures = ref([])
const resultSignatures = ref([])
const notificationKey = (type) => `${type}:${auth.user?.id ?? 'anonymous'}`

function readSignatures(key) {
  try {
    const value = JSON.parse(sessionStorage.getItem(key) || '[]')
    return Array.isArray(value) ? value : []
  } catch {
    return []
  }
}

function markWaitingAsRead() {
  sessionStorage.setItem(notificationKey('seen_carrier_waiting_requests'), JSON.stringify(waitingSignatures.value))
  waitingCount.value = 0
}

function markResultsAsRead() {
  sessionStorage.setItem(notificationKey('seen_generator_request_results'), JSON.stringify(resultSignatures.value))
  resultCount.value = 0
}

onMounted(async () => {
  try {
    const res = isInstructor.value
      ? await enrollmentApi.getCarrierRequests()
      : await enrollmentApi.getMyEnrollments()
    const requests = Array.isArray(res.data?.data) ? res.data.data : []
    if (isInstructor.value) {
      waitingSignatures.value = requests
        .filter(item => item.status === 'CONFIRMED')
        .map(item => `${item.id}:${item.status}`)
      const seen = new Set(readSignatures(notificationKey('seen_carrier_waiting_requests')))
      waitingCount.value = waitingSignatures.value.filter(signature => !seen.has(signature)).length
    } else {
      resultSignatures.value = requests
        .filter(item => ['ACCEPTED', 'COMPLETED', 'REJECTED'].includes(item.status))
        .map(item => `${item.id}:${item.status}`)
      const seen = new Set(readSignatures(notificationKey('seen_generator_request_results')))
      resultCount.value = resultSignatures.value.filter(signature => !seen.has(signature)).length
    }
  } catch (error) {
    console.warn('[DashboardSidebar] failed to load waiting requests:', error)
  }
})

function logout() {
  auth.logout()
}
</script>

<style scoped>
.dashboard-sidebar {
  position: sticky;
  top: 96px;
  height: calc(100vh - 120px);
  min-height: 560px;
  padding: 24px 16px 16px;
  border-radius: 24px;
  background:
    radial-gradient(circle at 12% 88%, rgba(45,206,137,.22), transparent 12rem),
    linear-gradient(165deg, #172b4d 0%, #263f73 55%, #4b3f92 100%);
  color: #fff;
  box-shadow: 0 24px 48px rgba(23,43,77,.18);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.sidebar-kicker { padding: 0 12px 14px; color: rgba(255,255,255,.46); font-size: 10px; font-weight: 800; letter-spacing: .16em; }
.dashboard-nav { display: grid; gap: 7px; }
.waiting-badge { margin-left: auto; min-width: 22px; height: 22px; padding: 0 6px; display: grid; place-items: center; border-radius: 999px; background: #ff6b6b; color: #fff; font-size: 11px; font-style: normal; font-weight: 800; }
.dashboard-link { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: 14px; color: rgba(255,255,255,.68); transition: var(--transition); }
.dashboard-link:hover { color: #fff; background: rgba(255,255,255,.08); transform: translateX(2px); }
.dashboard-link.active { color: #fff; background: rgba(255,255,255,.15); box-shadow: inset 0 0 0 1px rgba(255,255,255,.10); }
.link-icon { width: 34px; height: 34px; display: grid; place-items: center; border-radius: 11px; background: rgba(255,255,255,.10); font-size: 17px; flex-shrink: 0; }
.dashboard-link.active .link-icon { background: linear-gradient(135deg, #5e72e4, #825ee4); box-shadow: 0 8px 18px rgba(0,0,0,.18); }
.link-copy { display: flex; flex: 1; flex-direction: column; min-width: 0; }
.dashboard-link strong { font-size: 13px; line-height: 1.4; }
.dashboard-link small { font-size: 10px; color: rgba(255,255,255,.45); }
.sidebar-account { display: flex; align-items: center; gap: 10px; padding: 12px; border-radius: 16px; background: rgba(255,255,255,.10); border: 1px solid rgba(255,255,255,.10); }
.account-avatar { width: 34px; height: 34px; border-radius: 11px; display: grid; place-items: center; background: linear-gradient(135deg, #2dce89, #11cdef); font-weight: 800; }
.account-copy { min-width: 0; flex: 1; display: flex; flex-direction: column; }
.account-copy strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 12px; }
.account-copy span { font-size: 9px; color: rgba(255,255,255,.56); }
.logout-icon { width: 28px; height: 28px; border-radius: 9px; background: rgba(255,255,255,.10); color: #fff; }
.logout-icon:hover { background: rgba(255,255,255,.20); }
@media (max-width: 992px) {
  .dashboard-sidebar { position: static; height: auto; min-height: 0; }
  .dashboard-nav { grid-template-columns: repeat(3, 1fr); }
  .dashboard-link { justify-content: center; }
  .dashboard-link small { display: none; }
  .sidebar-account, .sidebar-kicker { display: none; }
}
@media (max-width: 640px) {
  .dashboard-nav { grid-template-columns: 1fr; }
  .dashboard-link { justify-content: flex-start; }
}
</style>
