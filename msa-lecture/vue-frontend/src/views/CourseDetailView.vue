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
              <span>🏢 {{ course.carrierName || '수집·운반 업체' }}</span>
              <span>♻️ 접수 {{ displayEnrollmentCount }}건</span>
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
                <p class="time-guide">24시간 기준 · {{ timeRangeGuide }}</p>
                <label class="field-label">폐기물 정보</label>
                <input v-model.trim="reservation.wasteInformation" class="form-input" placeholder="예: 일반의료폐기물 20kg · 전용용기 4개" />
                <label class="field-label">수거 조건·요청사항 <span>(선택)</span></label>
                <textarea v-model.trim="reservation.collectionRequirements" class="form-input" rows="3" placeholder="사업장 위치, 출입 절차 등 필요한 내용을 입력하세요"></textarea>
                <div class="form-checklist" aria-live="polite">
                  <p :class="{ complete: fieldChecks.date }"><span>{{ fieldChecks.date ? '✓' : '·' }}</span> 희망 수거일</p>
                  <p :class="{ complete: fieldChecks.time }"><span>{{ fieldChecks.time ? '✓' : '·' }}</span> 시작·종료 시간</p>
                  <p :class="{ complete: fieldChecks.waste }"><span>{{ fieldChecks.waste ? '✓' : '·' }}</span> 폐기물 정보</p>
                </div>
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
const enrollmentStatus = ref('NONE') // NONE | PENDING | CONFIRMED | ACCEPTED | COMPLETED | REJECTED | CANCELLED
function localDateString(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const minDate = localDateString()
const reservation = reactive({ preferredCollectionDate: '', preferredStartTime: '', preferredEndTime: '', wasteInformation: '', collectionRequirements: '' })
const fieldChecks = computed(() => ({
  date: Boolean(reservation.preferredCollectionDate),
  time: Boolean(reservation.preferredStartTime && reservation.preferredEndTime && reservation.preferredStartTime !== reservation.preferredEndTime),
  waste: Boolean(reservation.wasteInformation.trim())
}))
const reservationComplete = computed(() => Object.values(fieldChecks.value).every(Boolean))
const crossesMidnight = computed(() => fieldChecks.value.time && reservation.preferredEndTime < reservation.preferredStartTime)
const timeRangeGuide = computed(() => {
  if (!reservation.preferredStartTime || !reservation.preferredEndTime) return '시작과 종료 시간을 선택하세요.'
  if (reservation.preferredStartTime === reservation.preferredEndTime) return '시작과 종료 시간은 다르게 선택해야 합니다.'
  return `${reservation.preferredStartTime} → ${crossesMidnight.value ? '다음 날 ' : ''}${reservation.preferredEndTime}`
})

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
  if (enrollmentStatus.value === 'PENDING') return '수거 신청 · 결제 처리 중'
  if (enrollmentStatus.value === 'CONFIRMED') return '업체 확인 대기 · 신청 관리'
  if (enrollmentStatus.value === 'ACCEPTED') return '수거 예정 · 신청 관리'
  if (enrollmentStatus.value === 'COMPLETED') return '수거 완료 · 신청 내역 보기'
  if (enrollmentStatus.value === 'REJECTED') return '업체 거절 · 신청 내역 보기'
  if (enrollmentStatus.value === 'CANCELLED') return '취소된 신청 내역 보기'
  return '비용 결제하고 수거 신청'
})

const buttonDisabled = computed(() => {
  if (enrolling.value) return true
  if (isInstructor.value) return true
  if (enrollmentStatus.value === 'PENDING') return true
  if (enrollmentStatus.value === 'NONE' && !reservationComplete.value) return true
  return false
})

const helperText = computed(() => {
  if (isInstructor.value) {
    return '수집·운반 업체 계정은 수거 서비스를 신청할 수 없습니다.'
  }

  if (enrollmentStatus.value === 'CONFIRMED') {
    return '결제가 완료되어 수거업체의 확인을 기다리고 있습니다.'
  }

  if (enrollmentStatus.value === 'ACCEPTED') {
    return '수거업체가 신청을 수락했습니다. 희망 수거일에 수거가 예정되어 있습니다.'
  }

  if (enrollmentStatus.value === 'COMPLETED') {
    return '해당 신청의 의료폐기물 수거가 완료되었습니다.'
  }

  if (enrollmentStatus.value === 'REJECTED') {
    return '수거업체가 해당 신청을 거절했습니다. 신청 관리에서 상태를 확인해 주세요.'
  }

  if (enrollmentStatus.value === 'CANCELLED') {
    return '취소된 수거 신청입니다.'
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

    const serviceEnrollments = enrollments
      .filter(item => Number(item.collectionServiceId) === Number(course.value.id))
      .sort((a, b) => {
        const createdAtOrder = new Date(b.createdAt || 0) - new Date(a.createdAt || 0)
        return createdAtOrder || Number(b.id || 0) - Number(a.id || 0)
      })
    const matched = serviceEnrollments.find(item => ['PENDING', 'CONFIRMED', 'ACCEPTED'].includes(item.status))

    if (!matched) {
      enrollmentStatus.value = 'NONE'
      return
    }

    const supportedStatuses = ['PENDING', 'CONFIRMED', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'CANCELLED']
    enrollmentStatus.value = supportedStatuses.includes(matched.status) ? matched.status : 'NONE'
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

  if (['CONFIRMED', 'ACCEPTED', 'COMPLETED', 'REJECTED', 'CANCELLED'].includes(enrollmentStatus.value)) {
    router.push('/enrollments')
    return
  }

  if (enrollmentStatus.value === 'PENDING') {
    return
  }

  if (!reservationComplete.value) {
		enrollError.value = '필수 항목을 모두 입력하고, 시작과 종료 시간을 다르게 지정해 주세요.'
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
  background:
    radial-gradient(circle at 86% 12%, rgba(130,94,228,.10), transparent 18rem),
    linear-gradient(135deg, #fbfcff 0%, #f1f4ff 55%, #eefaf6 100%);
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
.form-checklist { display: grid; gap: 6px; margin: 10px 0 4px; padding: 12px; border-radius: 12px; background: var(--color-bg-secondary); }
.form-checklist p { margin: 0; font-size: 12px; color: var(--color-text-muted); }
.form-checklist p.complete { color: #14865a; font-weight: 700; }
.form-checklist span { display: inline-grid; place-items: center; width: 16px; }
.time-guide { margin: 6px 0 2px; font-size: 11px; color: var(--color-text-muted); }

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

.thumb-teal { background: linear-gradient(135deg, #e5fbf2, #c6f3df); }
.thumb-blue { background: linear-gradient(135deg, #e7f5ff, #cbdfff); }
.thumb-purple { background: linear-gradient(135deg, #f1ecff, #dacbff); }
.thumb-pink { background: #FBEAF0; }
.thumb-gray { background: #F1EFE8; }

.enroll-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.care-hero-icon { font-size: 78px; filter: drop-shadow(0 12px 18px rgba(50,50,93,.18)); }
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
