<template>
  <div class="page-wrapper">
    <AppHeader />
    <div class="page-layout">
      <DashboardSidebar />

      <main class="main-content">
        <!-- 프로필 카드 -->
        <div class="profile-card fade-in-up">
          <div class="profile-info">
            <span class="workspace-eyebrow">OPERATIONS DASHBOARD</span>
            <p class="welcome-copy">안녕하세요,</p>
            <h2 class="profile-name">{{ auth.user?.organizationName || auth.user?.name || '사용자' }}</h2>
            <span class="badge" :class="isInstructor ? 'badge-amber' : 'badge-blue'">
              {{ isInstructor ? '의료폐기물 수집·운반 업체' : '병원·의원' }}
            </span>
          </div>
        </div>

        <!-- 의료폐기물 배출 사업장 화면 -->
        <section v-if="!isInstructor" class="recommend-section">
          <h3 class="section-title">추천 수거 서비스</h3>

          <p v-if="recommendMessage" class="recommend-message">
            {{ recommendMessage }}
          </p>

          <div v-if="recommendLoading" class="loading-row">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-thumb"></div>
              <div class="skeleton-body">
                <div class="skeleton-line short"></div>
                <div class="skeleton-line"></div>
              </div>
            </div>
          </div>

          <div v-else-if="recommendations.length" class="recommend-grid fade-in">
            <CourseCard v-for="c in recommendations" :key="c.id" :course="c" />
          </div>

          <p v-else-if="recommendError" class="empty-text">
            {{ recommendError }}
          </p>

          <p v-else class="empty-text">
            아직 추천할 수거 서비스가 없습니다.
          </p>
        </section>

        <!-- 수집·운반 업체 화면 -->
        <section v-else class="instructor-section">
          <div class="section-head">
            <h3 class="section-title">내가 등록한 수거 서비스</h3>
            <span class="section-subtitle">등록한 수거 서비스와 접수 건수를 확인할 수 있습니다.</span>
          </div>

          <div class="summary-cards">
            <div class="summary-card">
              <div class="summary-label">등록 서비스 수</div>
              <div class="summary-value">{{ myCourses.length }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">총 접수 건수</div>
              <div class="summary-value">{{ totalEnrollmentCount }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">업체 확인 대기</div>
              <div class="summary-value">{{ waitingRequestCount }}</div>
            </div>
          </div>

          <div v-if="instructorLoading" class="loading-row instructor-loading">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-thumb"></div>
              <div class="skeleton-body">
                <div class="skeleton-line short"></div>
                <div class="skeleton-line"></div>
              </div>
            </div>
          </div>

          <div v-else-if="myCourses.length" class="instructor-course-list fade-in">
            <div
              v-for="course in myCourses"
              :key="course.id"
              class="instructor-course-card"
            >
              <div class="course-card-top">
                <div>
                  <h4 class="course-title">{{ course.name }}</h4>
                  <p class="course-desc">{{ course.description || '설명이 없습니다.' }}</p>
                </div>
                <span
                  class="status-badge"
                  :class="course.status === 'ACTIVE' ? 'status-active' : 'status-inactive'"
                >
                  {{ course.status || 'UNKNOWN' }}
                </span>
              </div>

              <div class="course-meta-grid">
                <div class="meta-box">
                  <div class="meta-label">카테고리</div>
                  <div class="meta-value">{{ course.wasteType || '-' }}</div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">가격</div>
                  <div class="meta-value">{{ formatPrice(course.price) }}</div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">접수 건수</div>
                  <div class="meta-value">
                    {{ course.requestCount ?? 0 }}건
                  </div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">서비스 ID</div>
                  <div class="meta-value">#{{ course.id }}</div>
                </div>
              </div>

              <div class="course-card-actions">
                <router-link :to="`/courses/${course.id}`" class="action-btn action-primary">
                  서비스 보기
                </router-link>
              </div>
            </div>
          </div>

          <p v-else-if="instructorError" class="empty-text">
            {{ instructorError }}
          </p>

          <p v-else class="empty-text">
            아직 등록한 수거 서비스가 없습니다.
          </p>

          <div id="carrier-requests" class="request-section-head">
            <div>
              <h3 class="section-title">{{ waitingOnly ? '업체 확인 대기' : '수거 신청 처리' }}</h3>
              <p class="section-subtitle">{{ waitingOnly ? '새로 들어온 신청만 모아보고 수락·거절하세요.' : '결제가 완료된 접수를 수락하고, 수거 후 완료 처리하세요.' }}</p>
            </div>
            <router-link v-if="waitingOnly" to="/mypage#carrier-requests" class="action-btn">전체 신청 보기</router-link>
          </div>

          <div v-if="carrierRequestsLoading" class="request-loading">수거 신청을 불러오는 중입니다.</div>
          <p v-else-if="carrierRequestsError" class="request-error">{{ carrierRequestsError }}</p>
          <div v-else-if="visibleCarrierRequests.length" class="carrier-request-list">
            <article v-for="request in visibleCarrierRequests" :key="request.id" class="carrier-request-card">
              <div class="request-card-head">
                <div>
                  <span class="request-id">신청 #{{ request.id }}</span>
                  <h4>{{ request.collectionService?.name || `수거 서비스 #${request.collectionServiceId}` }}</h4>
                </div>
                <span class="request-status" :class="`request-status-${request.status?.toLowerCase()}`">
                  {{ statusLabel(request.status) }}
                </span>
              </div>

              <div class="request-detail-grid">
                <div><span>배출 사업장</span><strong>#{{ request.generatorId }}</strong></div>
                <div><span>희망 수거일</span><strong>{{ request.preferredCollectionDate || '-' }}</strong></div>
                <div><span>희망 시간</span><strong>{{ formatRequestTime(request) }}</strong></div>
                <div><span>폐기물 정보</span><strong>{{ request.wasteInformation || '-' }}</strong></div>
              </div>

              <p v-if="request.collectionRequirements" class="request-requirements">
                <span>요청사항</span>{{ request.collectionRequirements }}
              </p>

              <div class="request-actions">
                <template v-if="request.status === 'CONFIRMED'">
                  <button
                    class="request-action-btn reject-btn"
                    :disabled="processingRequestId === request.id"
                    @click="processRequest(request, 'reject')"
                  >
                    거절
                  </button>
                  <button
                    class="request-action-btn accept-btn"
                    :disabled="processingRequestId === request.id"
                    @click="processRequest(request, 'accept')"
                  >
                    {{ processingRequestId === request.id ? '처리 중...' : '수거 신청 수락' }}
                  </button>
                </template>
                <button
                  v-else-if="request.status === 'ACCEPTED'"
                  class="request-action-btn complete-btn"
                  :disabled="processingRequestId === request.id"
                  @click="processRequest(request, 'complete')"
                >
                  {{ processingRequestId === request.id ? '처리 중...' : '수거 완료' }}
                </button>
                <span v-else-if="request.status === 'COMPLETED'" class="completed-note">✓ 수거 완료된 신청입니다.</span>
                <span v-else class="waiting-note">{{ statusGuide(request.status) }}</span>
              </div>
            </article>
          </div>
          <p v-else class="empty-text">{{ waitingOnly ? '새로 확인할 신청이 없습니다.' : '아직 들어온 수거 신청이 없습니다.' }}</p>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import DashboardSidebar from '@/components/DashboardSidebar.vue'
import CourseCard from '@/components/CourseCard.vue'
import { useAuthStore } from '@/store/auth.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { courseApi } from '@/api/course.js'

const auth = useAuthStore()
const route = useRoute()

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

/* 학생용 */
const recommendations = ref([])
const recommendLoading = ref(true)
const recommendError = ref('')
const recommendMessage = ref('')

/* 강사용 */
const myCourses = ref([])
const instructorLoading = ref(true)
const instructorError = ref('')
const carrierRequests = ref([])
const carrierRequestsLoading = ref(true)
const carrierRequestsError = ref('')
const processingRequestId = ref(null)

const totalEnrollmentCount = computed(() =>
  myCourses.value.reduce((sum, course) => {
    const count = Number(course.requestCount ?? 0)
    return sum + (Number.isNaN(count) ? 0 : count)
  }, 0)
)

const waitingRequestCount = computed(() =>
  carrierRequests.value.filter(request => request.status === 'CONFIRMED').length
)
const waitingOnly = computed(() => route.query.requestStatus === 'CONFIRMED')
const visibleCarrierRequests = computed(() => waitingOnly.value
  ? carrierRequests.value.filter(request => request.status === 'CONFIRMED')
  : carrierRequests.value
)

async function focusRequestList() {
  if (!waitingOnly.value) return
  await nextTick()
  document.getElementById('carrier-requests')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function formatPrice(price) {
  const value = Number(price ?? 0)
  if (Number.isNaN(value)) return '-'
  return `${value.toLocaleString()}원`
}

function formatRequestTime(request) {
  const nextDay = request.preferredStartTime && request.preferredEndTime && request.preferredEndTime < request.preferredStartTime
  return `${request.preferredStartTime || '-'} → ${nextDay ? '다음 날 ' : ''}${request.preferredEndTime || '-'}`
}

function statusLabel(status) {
  return ({
    PENDING: '결제 대기',
    CONFIRMED: '업체 확인 대기',
    ACCEPTED: '수거 예정',
    COMPLETED: '수거 완료',
    REJECTED: '업체 거절',
    CANCELLED: '취소'
  })[status] || status || '상태 미확인'
}

function statusGuide(status) {
  if (status === 'PENDING') return '결제 완료를 기다리고 있습니다.'
  if (status === 'REJECTED') return '수거업체가 거절한 신청입니다.'
  if (status === 'CANCELLED') return '취소된 신청입니다.'
  return '현재 상태를 확인해 주세요.'
}

/**
 * course 객체에서 강사 식별자 추출
 */
function getCourseInstructorId(course) {
  return (
    course.carrierId ??
    course.instructor ??
    course.teacherId ??
    course.teacher_id ??
    null
  )
}

async function loadStudentRecommendations() {
  try {
    if (!auth.user) {
      console.warn('[MyPage] auth.user is missing')
      recommendError.value = '추천 수거 서비스를 준비 중입니다.'
      return
    }

    if (!auth.user.id) {
      console.warn('[MyPage] auth.user.id is missing:', auth.user)
      recommendError.value = '추천 수거 서비스를 준비 중입니다.'
      return
    }

    const res = await enrollmentApi.getRecommendations(auth.user.id)
    console.log('[MyPage] recommendation response:', res.data)

    const payload = res.data

    if (Array.isArray(payload?.recommendedCollectionServices)) {
      recommendations.value = payload.recommendedCollectionServices
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload?.data)) {
      recommendations.value = payload.data
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload)) {
      recommendations.value = payload
      recommendMessage.value = ''
    } else {
      console.warn('[MyPage] unexpected recommendation response shape:', payload)
      recommendations.value = []
      recommendMessage.value = ''
    }
  } catch (error) {
    console.error('[MyPage] failed to load recommendations:', error)
    recommendError.value = '현재 추천 수거 서비스를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    recommendLoading.value = false
  }
}

async function loadInstructorCourses() {
  try {
    if (!auth.user) {
      console.warn('[MyPage] instructor auth.user is missing')
      instructorError.value = '수거 서비스 정보를 불러오지 못했습니다.'
      return
    }

    if (!auth.user.id) {
      console.warn('[MyPage] instructor auth.user.id is missing:', auth.user)
      instructorError.value = '수거 서비스 정보를 불러오지 못했습니다.'
      return
    }

    const res = await courseApi.getCourses()
    console.log('[MyPage] course list response:', res.data)

    let courses = []

    if (Array.isArray(res.data?.data)) {
      courses = res.data.data
    } else if (Array.isArray(res.data)) {
      courses = res.data
    } else {
      console.warn('[MyPage] unexpected course response shape:', res.data)
    }

    console.log('[MyPage] auth.user =', auth.user)
    console.log('[MyPage] courses =', courses)
    console.log('[MyPage] first course =', courses[0])

    courses.forEach(course => {
      console.log('[MyPage] instructor fields check:', {
        courseId: course.id,
        instructorId: course.instructorId,
        instructor_id: course.instructor_id,
        instructor: course.instructor,
        teacherId: course.teacherId,
        teacher_id: course.teacher_id,
        rawCourse: course
      })
    })

    const instructorId = Number(auth.user.id)

    myCourses.value = courses.filter(course => {
      const courseInstructorId = Number(getCourseInstructorId(course))
      return !Number.isNaN(courseInstructorId) && courseInstructorId === instructorId
    })

    console.log('[MyPage] filtered myCourses =', myCourses.value)
  } catch (error) {
    console.error('[MyPage] failed to load instructor courses:', error)
    instructorError.value = '현재 수거 서비스 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    instructorLoading.value = false
  }
}

async function loadCarrierRequests() {
  carrierRequestsLoading.value = true
  carrierRequestsError.value = ''
  try {
    const res = await enrollmentApi.getCarrierRequests()
    carrierRequests.value = Array.isArray(res.data?.data) ? res.data.data : []
  } catch (error) {
    console.error('[MyPage] failed to load carrier requests:', error)
    carrierRequestsError.value = error.response?.status === 401
      ? '로그인이 만료되었습니다. 로그아웃 후 다시 로그인해 주세요.'
      : '수거 신청 목록을 불러오지 못했습니다.'
  } finally {
    carrierRequestsLoading.value = false
  }
}

async function processRequest(request, action) {
  processingRequestId.value = request.id
  carrierRequestsError.value = ''
  try {
    let res
    if (action === 'accept') res = await enrollmentApi.acceptRequest(request.id)
    else if (action === 'reject') res = await enrollmentApi.rejectRequest(request.id)
    else res = await enrollmentApi.completeRequest(request.id)
    const updated = res.data?.data
    if (updated) {
      const index = carrierRequests.value.findIndex(item => item.id === request.id)
      if (index !== -1) carrierRequests.value[index] = updated
    } else {
      await loadCarrierRequests()
    }
  } catch (error) {
    console.error('[MyPage] failed to process carrier request:', error)
    carrierRequestsError.value = error.response?.status === 401
      ? '로그인이 만료되었습니다. 로그아웃 후 다시 로그인해 주세요.'
      : error.response?.data?.message || '수거 신청 상태를 변경하지 못했습니다.'
  } finally {
    processingRequestId.value = null
  }
}

onMounted(async () => {
  if (isInstructor.value) {
    recommendLoading.value = false
    await Promise.all([loadInstructorCourses(), loadCarrierRequests()])
    await focusRequestList()
  } else {
    instructorLoading.value = false
    carrierRequestsLoading.value = false
    await loadStudentRecommendations()
  }
})

watch(() => route.query.requestStatus, focusRequestList)
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
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background:
    radial-gradient(circle at 88% 25%, rgba(45,206,137,.22), transparent 15rem),
    linear-gradient(135deg, #172b4d 0%, #5e72e4 58%, #825ee4 100%);
  border: 1px solid rgba(255,255,255,.14);
  border-radius: 24px;
  padding: 32px;
  box-shadow: var(--shadow-lg);
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-card .workspace-eyebrow { color: rgba(255,255,255,.72); }
.profile-card .workspace-eyebrow::before { background: #9cf0cd; }
.welcome-copy { color: rgba(255,255,255,.68); font-size: 13px; }

.profile-name {
  color: #fff;
  font-size: 28px;
  font-weight: 800;
}

.profile-email {
  font-size: 14px;
  color: rgba(255,255,255,.66);
}

.badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-blue {
  background: #e8f1ff;
  color: #2563eb;
}

.badge-amber {
  background: #f7edd8;
  color: #9a6700;
}

.section-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
}

.section-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
}

.recommend-message {
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.loading-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.instructor-loading {
  margin-bottom: 20px;
}

.skeleton-card {
  background: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.skeleton-thumb {
  height: 110px;
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

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(160px, 220px));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
}

.summary-card::after { content: ''; position: absolute; inset: 0 0 auto; height: 3px; background: linear-gradient(90deg, var(--color-primary), var(--color-accent)); }
.summary-card:nth-child(2)::after { background: linear-gradient(90deg, #11cdef, #5e72e4); }
.summary-card:nth-child(3)::after { background: linear-gradient(90deg, #f5b942, #fb6340); }

.summary-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.instructor-course-list {
  display: grid;
  gap: 18px;
}

.instructor-course-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 22px;
  box-shadow: var(--shadow-sm);
}

.course-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.course-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  white-space: pre-line;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #eaf8ef;
  color: #0f8a3b;
}

.status-inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.course-meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}

.meta-box {
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: 14px;
}

.meta-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.meta-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.course-card-actions {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  border-radius: var(--radius-md);
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
}

.action-primary {
  background: var(--color-primary);
  color: white;
}

.action-primary:hover {
  opacity: 0.92;
}

.empty-text {
  color: var(--color-text-muted);
  font-size: 14px;
}

.request-section-head {
  margin-top: 36px;
  margin-bottom: 14px;
}

.request-loading,
.request-error {
  padding: 18px;
  border-radius: var(--radius-md);
  font-size: 14px;
}

.request-loading {
  background: var(--color-bg-primary);
  color: var(--color-text-muted);
}

.request-error {
  background: #fff1f2;
  color: #be123c;
}

.carrier-request-list {
  display: grid;
  gap: 14px;
}

.carrier-request-card {
  padding: 20px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.request-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.request-card-head h4 {
  margin-top: 4px;
  font-size: 17px;
}

.request-id {
  color: var(--color-text-muted);
  font-size: 12px;
}

.request-status {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  white-space: nowrap;
  font-size: 12px;
  font-weight: 700;
  background: #f3f4f6;
  color: #6b7280;
}

.request-status-confirmed { background: #fff7d6; color: #8a6100; }
.request-status-accepted { background: #e8f1ff; color: #2563eb; }
.request-status-completed { background: #eaf8ef; color: #0f7a36; }
.request-status-rejected { background: #fff1f2; color: #be123c; }
.request-status-cancelled { background: #fff1f2; color: #be123c; }

.request-detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.request-detail-grid > div {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
}

.request-detail-grid span,
.request-requirements span {
  color: var(--color-text-muted);
  font-size: 11px;
}

.request-detail-grid strong {
  font-size: 13px;
  line-height: 1.4;
}

.request-requirements {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: 10px;
  padding: 12px;
  border-left: 3px solid var(--color-primary);
  background: var(--color-primary-light);
  font-size: 13px;
}

.request-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16px;
  gap: 8px;
}

.request-action-btn {
  border: none;
  border-radius: var(--radius-md);
  padding: 10px 16px;
  color: white;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.request-action-btn:disabled { opacity: 0.6; cursor: wait; }
.accept-btn { background: var(--color-primary); }
.reject-btn { background: #be123c; }
.complete-btn { background: #2563eb; }
.completed-note { color: #0f7a36; font-size: 13px; font-weight: 600; }
.waiting-note { color: var(--color-text-muted); font-size: 13px; }

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}

@media (max-width: 992px) {
  .page-layout {
    grid-template-columns: 1fr;
  }

  .recommend-grid,
  .loading-row,
  .course-meta-grid,
  .request-detail-grid {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .profile-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .course-card-top {
    flex-direction: column;
  }

  .summary-cards {
    grid-template-columns: 1fr;
  }

  .request-card-head {
    flex-direction: column;
  }
}
</style>
