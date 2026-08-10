<template>
  <div class="page-wrapper">
    <AppHeader />

    <div class="detail-layout" v-if="course">
      <div class="detail-hero">
        <div class="detail-hero-inner">
          <!-- 좌측 상세 정보 -->
          <div class="detail-info fade-in-up">
            <span class="badge" :class="badgeClass">{{ displayCategory }}</span>
            <h1 class="detail-title">{{ course.name }}</h1>
            <p class="detail-desc">
              {{ course.description || '허가 정보를 등록한 수집·운반 업체의 의료폐기물 수거 서비스입니다.' }}
            </p>

            <div class="detail-meta">
              <span>🏢 수집·운반 업체 #{{ course.carrierId }}</span>
              <span>✓ 업체 정보 등록</span>
              <span>♻️ 접수 {{ displayEnrollmentCount }}건</span>
            </div>
            <div class="trust-panel">
              <div><strong>업체 정보</strong><span>수집·운반 업체 정보 확인</span></div>
              <div><strong>비용 결제</strong><span>결제 후 수거 접수 확정</span></div>
              <div><strong>업무 기록</strong><span>수거 조건과 폐기물 정보 관리</span></div>
            </div>
          </div>

          <!-- 우측 결제/수강 카드 -->
          <div class="enroll-card fade-in">
            <div class="enroll-thumb" :class="thumbBg">
              <div class="care-hero-icon">{{ careIcon }}</div>
            </div>

            <div class="enroll-body">
              <div class="enroll-price">₩{{ displayPrice }}</div>

              <template v-if="!isInstructor && enrollmentStatus === 'NONE'">
                <div class="reservation-heading"><strong>수거 신청 정보</strong><span>필수 항목을 입력해 주세요</span></div>
                <label class="field-label">희망 수거일</label>
                <input v-model="reservation.preferredCollectionDate" type="date" class="form-input" :min="minDate" aria-label="희망 수거일" />
                <label class="field-label">희망 수거 시간</label>
                <div class="time-row">
                  <input v-model="reservation.preferredStartTime" type="time" class="form-input" aria-label="시작 시간" />
                  <span>–</span>
                  <input v-model="reservation.preferredEndTime" type="time" class="form-input" aria-label="종료 시간" />
                </div>
                <label class="field-label">폐기물 정보</label>
                <input v-model.trim="reservation.wasteInformation" class="form-input" placeholder="예: 일반의료폐기물 20kg · 전용용기 4개" />
                <label class="field-label">수거 조건·요청사항 <span>(선택)</span></label>
                <textarea v-model.trim="reservation.collectionRequirements" class="form-input" rows="3" placeholder="사업장 위치, 출입 절차 등 필요한 내용을 입력하세요"></textarea>
              </template>

              <button
                class="btn btn-primary btn-full"
                @click="handlePrimaryAction"
                :disabled="buttonDisabled"
                :class="{ 'btn-disabled': buttonDisabled }"
              >
                <span v-if="enrolling">처리 중...</span>
                <span v-else>{{ buttonLabel }}</span>
              </button>

              <div v-if="enrollError" class="error-msg">{{ enrollError }}</div>

              <p class="helper-text" v-if="helperText">
                {{ helperText }}
              </p>

              <ul class="enroll-info-list">
                <li>✅ 결제 완료 후 수거 접수 자동 확정</li>
                <li>✅ 희망 수거일·시간 지정</li>
                <li>✅ 신청 관리에서 접수 상태 확인</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="loading-center">
      <div class="spinner"></div>
    </div>

    <div v-else class="loading-center">
      <p class="empty-text">수거 서비스 정보를 불러오지 못했습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { useCourseStore } from '@/store/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'

const route = useRoute()
const router = useRouter()
const courseStore = useCourseStore()
const auth = useAuthStore()

const enrolling = ref(false)
const enrollError = ref('')
const enrollmentStatus = ref('NONE') // NONE | PENDING | CONFIRMED
const minDate = new Date().toISOString().slice(0, 10)
const reservation = reactive({ preferredCollectionDate: '', preferredStartTime: '', preferredEndTime: '', wasteInformation: '', collectionRequirements: '' })

const course = computed(() => courseStore.selectedCourse)
const loading = computed(() => courseStore.loading)
const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')

const categoryConfig = {
  '일반의료폐기물': { badge: 'badge-teal', bg: 'thumb-teal', icon: '♻️' },
  '손상성폐기물': { badge: 'badge-blue', bg: 'thumb-blue', icon: '💉' },
  '조직물류폐기물': { badge: 'badge-purple', bg: 'thumb-purple', icon: '🧪' },
}

const config = computed(() => categoryConfig[course.value?.wasteType] || {})
const badgeClass = computed(() => config.value.badge || 'badge-gray')
const thumbBg = computed(() => config.value.bg || 'thumb-gray')
const careIcon = computed(() => config.value.icon || '♻️')

const displayCategory = computed(() => course.value?.wasteType || '-')

const displayInstructorName = computed(() => {
  return (
    course.value?.instructorName ||
    course.value?.teacherName ||
    course.value?.instructor?.name ||
    course.value?.instructor_name ||
    course.value?.ownerName ||
    '수집·운반 업체 정보 없음'
  )
})

const displayEnrollmentCount = computed(() => {
  const value = Number(
    course.value?.requestCount ??
    0
  )
  return Number.isNaN(value) ? 0 : value.toLocaleString()
})

const displayPrice = computed(() => {
  const value = Number(course.value?.price ?? 0)
  return Number.isNaN(value) ? '0' : value.toLocaleString()
})

const thumbSrc = computed(() => {
  const key = course.value?.thumbnail || config.value.thumb
  if (!key) return null

  try {
    return new URL(`../assets/images/courses/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})

const buttonLabel = computed(() => {
  if (isInstructor.value) return '수집·운반 업체는 신청 불가'
  if (enrollmentStatus.value === 'CONFIRMED') return '수거 신청 관리로 이동'
  if (enrollmentStatus.value === 'PENDING') return '수거 신청 · 결제 처리 중'
  return '비용 결제하고 수거 신청'
})

const buttonDisabled = computed(() => {
  if (enrolling.value) return true
  if (isInstructor.value) return true
  if (enrollmentStatus.value === 'PENDING') return true
  return false
})

const helperText = computed(() => {
  if (isInstructor.value) {
    return '수집·운반 업체 계정은 수거 서비스를 신청할 수 없습니다.'
  }

  if (enrollmentStatus.value === 'CONFIRMED') {
    return '결제가 완료되어 수거 접수가 확정되었습니다.'
  }

  if (enrollmentStatus.value === 'PENDING') {
    return '수거 신청이 생성되었습니다. 결제 완료 이벤트 처리 후 접수가 확정됩니다.'
  }

  return '희망 수거일과 폐기물 정보를 입력한 뒤 결제를 진행해 주세요.'
})

async function loadEnrollmentStatus() {
  if (!auth.user?.id || !course.value?.id || isInstructor.value) {
    enrollmentStatus.value = 'NONE'
    return
  }

  try {
    const res = await enrollmentApi.getMyEnrollments()
    console.log('[CourseDetail] my enrollments response =', res.data)

    const enrollments = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
        ? res.data
        : []

    const matched = enrollments.find(item => Number(item.collectionServiceId) === Number(course.value.id))

    if (!matched) {
      enrollmentStatus.value = 'NONE'
      return
    }

    enrollmentStatus.value = matched.status === 'CONFIRMED' ? 'CONFIRMED' : 'PENDING'
  } catch (e) {
    console.error('[CourseDetail] failed to load enrollment status:', e)
    enrollmentStatus.value = 'NONE'
  }
}

async function handlePrimaryAction() {
  enrollError.value = ''

  if (!course.value?.id) {
    enrollError.value = '수거 서비스 정보가 올바르지 않습니다.'
    return
  }

  if (isInstructor.value) {
    enrollError.value = '수집·운반 업체 계정은 수거 서비스를 신청할 수 없습니다.'
    return
  }

  if (enrollmentStatus.value === 'CONFIRMED') {
    router.push('/enrollments')
    return
  }

  if (enrollmentStatus.value === 'PENDING') {
    return
  }

  if (!reservation.preferredCollectionDate || !reservation.preferredStartTime || !reservation.preferredEndTime || !reservation.wasteInformation) {
    enrollError.value = '희망 수거일, 시작·종료 시간, 폐기물 정보를 입력해 주세요.'
    return
  }

  enrolling.value = true

  try {
    await enrollmentApi.reserve({ collectionServiceId: course.value.id, ...reservation })
    enrollmentStatus.value = 'PENDING'
  } catch (e) {
    console.error('[CourseDetail] enroll failed:', e)
    enrollError.value = e.response?.data?.message || '결제/수거 신청에 실패했습니다.'
  } finally {
    enrolling.value = false
  }
}

onMounted(async () => {
  await courseStore.fetchCourse(route.params.id)
  console.log('[CourseDetail] selectedCourse =', courseStore.selectedCourse)
  await loadEnrollmentStatus()
})

watch(
  () => courseStore.selectedCourse,
  async (value) => {
    console.log('[CourseDetail] selectedCourse changed =', value)
    if (value?.id) {
      await loadEnrollmentStatus()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: var(--color-bg-secondary);
}

.detail-hero {
  background: linear-gradient(135deg, #f7fbfa 0%, #e8f4f1 55%, #eaf3f8 100%);
  border-bottom: 1px solid var(--color-border);
  padding: 48px 0;
}

.detail-hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 48px;
  align-items: start;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-title {
  font-size: 38px;
  font-weight: 800;
  line-height: 1.3;
}

.detail-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
  flex-wrap: wrap;
}
.trust-panel {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 18px;
}
.trust-panel div { padding: 15px; border: 1px solid var(--color-border); border-radius: 15px; background: rgba(255,255,255,.7); }
.trust-panel strong, .trust-panel span { display: block; }
.trust-panel strong { font-size: 13px; margin-bottom: 3px; }
.trust-panel span { font-size: 11px; color: var(--color-text-secondary); }

.enroll-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.enroll-thumb {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.enroll-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  padding: 20px;
}

.thumb-teal { background: linear-gradient(135deg, #e6f4f1, #cfe9e4); }
.thumb-blue { background: linear-gradient(135deg, #e7f4ee, #cee8dc); }
.thumb-purple { background: linear-gradient(135deg, #f2ecfa, #e3d7f2); }
.thumb-pink { background: #FBEAF0; }
.thumb-gray { background: #F1EFE8; }

.enroll-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.care-hero-icon { font-size: 78px; filter: drop-shadow(0 10px 12px rgba(84,54,34,.14)); }
.reservation-heading { display:flex; align-items:flex-end; justify-content:space-between; border-top:1px solid var(--color-border); padding-top:16px; }
.reservation-heading strong { font-size:15px; }
.reservation-heading span { font-size:10px; color:var(--color-text-muted); }
.field-label { margin-bottom:-9px; font-size:11px; font-weight:700; color:var(--color-text-secondary); }
.field-label span { font-weight:400; color:var(--color-text-muted); }
.time-row { display:flex; gap:7px; align-items:center; }
.time-row span { color:var(--color-text-muted); }
.form-input { width:100%; min-width:0; border:1px solid var(--color-border); border-radius:11px; padding:11px 12px; background:#fff; color:var(--color-text-primary); font:inherit; font-size:13px; outline:none; transition:var(--transition); }
.form-input:focus { border-color:var(--color-primary); box-shadow:0 0 0 3px var(--color-primary-light); }
textarea.form-input { resize:vertical; }

.enroll-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}

.btn-full {
  width: 100%;
  padding: 13px;
  font-size: 15px;
  justify-content: center;
}

.btn-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.enroll-info-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enroll-info-list li {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.error-msg {
  font-size: 13px;
  color: #dc2626;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: var(--radius-sm);
}

.helper-text {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-muted);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 100px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.badge-gray {
  background: #f3f4f6;
  color: #6b7280;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .detail-hero-inner {
    grid-template-columns: 1fr;
  }
}
</style>
