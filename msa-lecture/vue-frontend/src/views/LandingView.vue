<template>
  <div class="landing">
    <AppHeader />

    <main>
      <section class="hero">
        <div class="hero-inner">
          <div class="hero-copy fade-in-up">
            <span class="eyebrow"><i></i> MEDICAL WASTE WORKSPACE</span>
            <h1>의료폐기물 수거 업무를<br><em>하나의 흐름</em>으로 관리하세요.</h1>
            <p>
              배출 사업장과 수집·운반 업체를 연결하고, 서비스 선택부터
              수거 접수 확정까지 필요한 기록을 한곳에 모읍니다.
            </p>
            <div class="hero-actions">
              <router-link :to="primaryAction.to" class="btn hero-primary">
                {{ primaryAction.label }} <span aria-hidden="true">→</span>
              </router-link>
              <router-link :to="secondaryAction.to" class="btn hero-secondary">
                {{ secondaryAction.label }}
              </router-link>
            </div>
            <div class="trust-note">
              <span>✓</span>
              공식 신고 시스템을 대체하지 않는 수거 업무 지원 서비스입니다.
            </div>
          </div>

          <div class="workflow-panel fade-in" aria-label="수거 업무 진행 예시">
            <div class="panel-header">
              <div>
                <span class="panel-kicker">TODAY'S WORKFLOW</span>
                <strong>수거 접수 현황</strong>
              </div>
              <span class="live-badge"><i></i> 운영 중</span>
            </div>

            <div class="status-card status-main">
              <div class="status-icon">01</div>
              <div>
                <span>수거 서비스 선택</span>
                <strong>폐기물 유형과 조건 확인</strong>
              </div>
              <span class="status-check">✓</span>
            </div>
            <div class="status-card">
              <div class="status-icon">02</div>
              <div>
                <span>희망 일정 입력</span>
                <strong>수거 날짜와 요청사항 접수</strong>
              </div>
              <span class="status-state">진행</span>
            </div>
            <div class="status-card muted">
              <div class="status-icon">03</div>
              <div>
                <span>결제 및 접수 확정</span>
                <strong>처리 상태 자동 기록</strong>
              </div>
              <span class="status-state">대기</span>
            </div>

            <div class="panel-summary">
              <div><strong>통합</strong><span>수거 신청 관리</span></div>
              <div><strong>추적</strong><span>진행 상태 확인</span></div>
              <div><strong>연결</strong><span>사업장·운반 업체</span></div>
            </div>
          </div>
        </div>
      </section>

      <section class="process-section">
        <div class="section-inner">
          <div class="section-intro">
            <span class="section-kicker">COLLECTION PROCESS</span>
            <h2>신청부터 접수 확정까지</h2>
            <p>각 서비스가 분리되어 있어도 사용자는 하나의 연결된 업무 흐름으로 이용합니다.</p>
          </div>
          <div class="process-list">
            <article v-for="(step, index) in processSteps" :key="step.title" class="process-item">
              <div class="process-number">0{{ index + 1 }}</div>
              <div class="process-icon">{{ step.icon }}</div>
              <h3>{{ step.title }}</h3>
              <p>{{ step.desc }}</p>
              <span v-if="index < processSteps.length - 1" class="process-arrow" aria-hidden="true">→</span>
            </article>
          </div>
        </div>
      </section>

      <section class="services-section">
        <div class="section-inner">
          <div class="section-heading-row">
            <div>
              <span class="section-kicker">ACTIVE SERVICES</span>
              <h2>현재 이용 가능한 수거 서비스</h2>
              <p>수집·운반 업체가 실제로 등록한 서비스 중 접수가 많은 항목입니다.</p>
            </div>
            <router-link :to="servicesLink" class="text-link">전체 서비스 보기 <span>→</span></router-link>
          </div>

          <div v-if="courseStore.loading" class="service-list" aria-label="서비스를 불러오는 중">
            <div v-for="i in 3" :key="i" class="service-row skeleton-row">
              <span class="skeleton-block skeleton-icon"></span>
              <span class="skeleton-block skeleton-copy"></span>
              <span class="skeleton-block skeleton-price"></span>
            </div>
          </div>

          <div v-else-if="courseStore.error" class="service-empty">
            <span class="empty-icon">!</span>
            <div>
              <h3>서비스 목록을 불러오지 못했습니다.</h3>
              <p>잠시 후 다시 확인해 주세요. 다른 메뉴는 정상적으로 이용할 수 있습니다.</p>
            </div>
            <button class="btn retry-button" @click="courseStore.fetchCourses()">다시 불러오기</button>
          </div>

          <div v-else-if="featuredCourses.length" class="service-list">
            <router-link
              v-for="course in featuredCourses"
              :key="course.id"
              :to="courseLink(course.id)"
              class="service-row"
            >
              <div class="service-symbol" :class="wasteConfig(course.wasteType).className">
                {{ wasteConfig(course.wasteType).icon }}
              </div>
              <div class="service-copy">
                <span class="service-type">{{ wasteConfig(course.wasteType).label }}</span>
                <h3>{{ course.name }}</h3>
                <p>{{ course.carrierName || '수집·운반 업체' }}</p>
              </div>
              <div class="request-count">
                <span>누적 접수</span>
                <strong>{{ Number(course.requestCount || 0).toLocaleString() }}건</strong>
              </div>
              <div class="service-price">
                <span>서비스 비용</span>
                <strong>₩{{ Number(course.price || 0).toLocaleString() }}</strong>
              </div>
              <span class="row-arrow" aria-hidden="true">→</span>
            </router-link>
          </div>

          <div v-else class="service-empty">
            <span class="empty-icon">＋</span>
            <div>
              <h3>아직 등록된 수거 서비스가 없습니다.</h3>
              <p>수집·운반 업체 계정으로 첫 서비스를 등록해 보세요.</p>
            </div>
            <router-link :to="emptyAction.to" class="btn retry-button">{{ emptyAction.label }}</router-link>
          </div>
        </div>
      </section>

      <section class="role-section">
        <div class="section-inner role-grid">
          <article class="role-card generator-card">
            <span class="role-label">배출 사업장</span>
            <h2>조건에 맞는 서비스를 찾고<br>수거 일정을 신청하세요.</h2>
            <ul>
              <li>폐기물 유형별 서비스 비교</li>
              <li>희망 수거일과 요청사항 접수</li>
              <li>신청·결제 진행 상태 확인</li>
            </ul>
            <router-link :to="auth.isAuthenticated ? '/courses' : '/login'">수거 서비스 확인 →</router-link>
          </article>
          <article class="role-card carrier-card">
            <span class="role-label">수집·운반 업체</span>
            <h2>제공 가능한 서비스를 등록하고<br>접수 이력을 관리하세요.</h2>
            <ul>
              <li>폐기물 유형과 서비스 비용 등록</li>
              <li>업체 정보 기반 서비스 노출</li>
              <li>접수 건수와 처리 이력 확인</li>
            </ul>
            <router-link :to="carrierAction.to">{{ carrierAction.label }} →</router-link>
          </article>
        </div>
      </section>

      <section class="cta-section">
        <div class="cta-inner">
          <span>MEDIWASTE HUB</span>
          <h2>{{ ctaContent.title }}</h2>
          <p>{{ ctaContent.desc }}</p>
          <router-link :to="ctaContent.to" class="btn cta-button">{{ ctaContent.label }} →</router-link>
        </div>
      </section>
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-logo"><span>✚</span> MediWaste Hub</div>
        <p>수거 신청과 업무 기록을 연결하는 B2B workflow prototype</p>
        <small>© 2026 MediWaste Hub</small>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAuthStore } from '@/store/auth.js'
import { useCourseStore } from '@/store/course.js'

const auth = useAuthStore()
const courseStore = useCourseStore()

const processSteps = [
  { icon: '⌕', title: '서비스 선택', desc: '폐기물 유형과 수거 조건을 비교합니다.' },
  { icon: '◷', title: '일정 신청', desc: '희망 수거일과 현장 요청사항을 입력합니다.' },
  { icon: '₩', title: '비용 결제', desc: '수거 서비스 비용을 결제하고 처리를 요청합니다.' },
  { icon: '✓', title: '접수 확정', desc: '이벤트가 처리되면 신청 상태가 자동 갱신됩니다.' },
]

const featuredCourses = computed(() => (
  [...(Array.isArray(courseStore.courses) ? courseStore.courses : [])]
    .sort((a, b) => Number(b.requestCount || 0) - Number(a.requestCount || 0))
    .slice(0, 3)
))

const primaryAction = computed(() => {
  if (!auth.isAuthenticated) return { to: '/login', label: '기업 계정으로 시작하기' }
  if (auth.user?.role === 'INSTRUCTOR') return { to: '/courses/new', label: '수거 서비스 등록하기' }
  return { to: '/courses', label: '수거 서비스 찾기' }
})

const secondaryAction = computed(() => (
  auth.isAuthenticated
    ? { to: '/mypage', label: '내 업무 현황 보기' }
    : { to: '/login', label: '로그인' }
))

const servicesLink = computed(() => auth.isAuthenticated ? '/courses' : '/login')
const emptyAction = computed(() => (
  auth.user?.role === 'INSTRUCTOR'
    ? { to: '/courses/new', label: '첫 서비스 등록' }
    : { to: '/login', label: '업체 계정으로 로그인' }
))
const carrierAction = computed(() => (
  auth.user?.role === 'INSTRUCTOR'
    ? { to: '/courses/new', label: '수거 서비스 등록' }
    : { to: '/login', label: '업체 계정 시작' }
))

const ctaContent = computed(() => {
  if (!auth.isAuthenticated) return {
    title: '반복되는 수거 업무, 이제 한곳에서 시작하세요.',
    desc: '배출 사업장과 수집·운반 업체 모두를 위한 업무 공간입니다.',
    to: '/login', label: '기업 계정으로 시작하기'
  }
  if (auth.user?.role === 'INSTRUCTOR') return {
    title: '새로운 수거 서비스를 등록해 보세요.',
    desc: '처리 가능한 폐기물 유형과 서비스 조건을 안내할 수 있습니다.',
    to: '/courses/new', label: '서비스 등록하기'
  }
  return {
    title: '현장 조건에 맞는 수거 서비스를 찾아보세요.',
    desc: '폐기물 유형별로 비교하고 희망 수거 일정을 신청할 수 있습니다.',
    to: '/courses', label: '수거 서비스 찾기'
  }
})

function courseLink(id) {
  return auth.isAuthenticated ? `/courses/${id}` : '/login'
}

function wasteConfig(wasteType) {
  const configs = {
    GENERAL_MEDICAL: { label: '일반의료폐기물', icon: '♻', className: 'teal' },
    '일반의료폐기물': { label: '일반의료폐기물', icon: '♻', className: 'teal' },
    SHARPS: { label: '손상성폐기물', icon: '↗', className: 'blue' },
    '손상성폐기물': { label: '손상성폐기물', icon: '↗', className: 'blue' },
    PATHOLOGICAL: { label: '조직물류폐기물', icon: '◇', className: 'purple' },
    '조직물류폐기물': { label: '조직물류폐기물', icon: '◇', className: 'purple' },
  }
  return configs[wasteType] || { label: wasteType || '수거 서비스', icon: '○', className: 'gray' }
}

onMounted(() => {
  courseStore.fetchCourses()
})
</script>

<style scoped>
.landing { min-height: 100vh; background: #f6f8f8; color: #182c35; }
.section-inner { width: min(1180px, calc(100% - 48px)); margin: 0 auto; }
.section-kicker, .panel-kicker { display: block; color: #5e72e4; font-size: 11px; font-weight: 800; letter-spacing: .16em; margin-bottom: 10px; }

.hero { position: relative; overflow: hidden; padding: 88px 0 78px; background: linear-gradient(135deg, #f4f6ff 0%, #eef0ff 58%, #f5f0ff 100%); border-bottom: 1px solid #e1e5f2; }
.hero::before { content: ''; position: absolute; width: 540px; height: 540px; right: -140px; top: -250px; border: 90px solid rgba(94,114,228,.07); border-radius: 50%; }
.hero::after { content: ''; position: absolute; width: 280px; height: 280px; left: -170px; bottom: -180px; border: 55px solid rgba(16,48,58,.05); border-radius: 50%; }
.hero-inner { position: relative; z-index: 1; width: min(1180px, calc(100% - 48px)); margin: 0 auto; display: grid; grid-template-columns: 1.02fr .98fr; gap: 88px; align-items: center; }
.eyebrow { display: inline-flex; align-items: center; gap: 9px; margin-bottom: 24px; color: #5e72e4; font-size: 11px; font-weight: 800; letter-spacing: .16em; }
.eyebrow i { width: 22px; height: 2px; background: #825ee4; }
.hero h1 { max-width: 650px; margin-bottom: 22px; color: #102f39; font-size: clamp(40px, 4.8vw, 62px); line-height: 1.17; letter-spacing: -.045em; font-weight: 800; }
.hero h1 em { color: #5e72e4; font-style: normal; }
.hero-copy > p { max-width: 570px; margin-bottom: 30px; color: #60727a; font-size: 17px; line-height: 1.8; word-break: keep-all; }
.hero-actions { display: flex; gap: 10px; margin-bottom: 23px; }
.hero-primary { padding: 13px 22px; background: linear-gradient(135deg, #5e72e4, #825ee4); border: 1px solid #5e72e4; color: #fff; border-radius: 8px; }
.hero-primary:hover { background: linear-gradient(135deg, #4a5bd4, #7048d6); transform: translateY(-1px); }
.hero-secondary { padding: 13px 22px; background: rgba(255,255,255,.6); border: 1px solid #c9d8d5; color: #29434b; border-radius: 8px; }
.hero-secondary:hover { background: #fff; border-color: #aebfbb; }
.trust-note { display: flex; align-items: center; gap: 8px; color: #74868c; font-size: 12px; }
.trust-note span { display: grid; width: 20px; height: 20px; place-items: center; border-radius: 50%; background: #e5e8ff; color: #5e72e4; font-weight: 800; }

.workflow-panel { padding: 28px; border: 1px solid rgba(255,255,255,.75); border-radius: 22px; background: rgba(255,255,255,.78); box-shadow: 0 28px 70px rgba(33,72,75,.13); backdrop-filter: blur(14px); }
.panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 22px; }
.panel-header strong { display: block; color: #14333c; font-size: 20px; }
.live-badge { display: inline-flex; align-items: center; gap: 7px; padding: 6px 10px; border: 1px solid #d7dcfa; border-radius: 99px; background: #f0f2ff; color: #4a5bd4; font-size: 11px; font-weight: 700; }
.live-badge i { width: 7px; height: 7px; border-radius: 50%; background: #16a086; box-shadow: 0 0 0 4px rgba(22,160,134,.12); }
.status-card { display: grid; grid-template-columns: 42px 1fr auto; gap: 13px; align-items: center; margin-top: 10px; padding: 15px; border: 1px solid #e2eae8; border-radius: 12px; background: #fff; }
.status-card.status-main { border-color: #cdd4f6; box-shadow: 0 8px 24px rgba(94,114,228,.10); }
.status-card.muted { opacity: .66; }
.status-icon { display: grid; width: 42px; height: 42px; place-items: center; border-radius: 10px; background: #eef0ff; color: #5e72e4; font-size: 12px; font-weight: 800; }
.status-card span { display: block; color: #819096; font-size: 10px; }
.status-card strong { color: #29434b; font-size: 13px; }
.status-check { display: grid !important; width: 24px; height: 24px; place-items: center; border-radius: 50%; background: #5e72e4; color: #fff !important; font-weight: 800; }
.status-state { padding: 5px 8px; border-radius: 5px; background: #edf3f2; color: #5e7479 !important; font-weight: 700; }
.panel-summary { display: grid; grid-template-columns: repeat(3, 1fr); margin-top: 22px; padding-top: 20px; border-top: 1px solid #e1e9e7; }
.panel-summary div { padding-left: 16px; border-left: 1px solid #e1e9e7; }
.panel-summary div:first-child { padding-left: 0; border-left: 0; }
.panel-summary strong, .panel-summary span { display: block; }
.panel-summary strong { color: #173640; font-size: 15px; }
.panel-summary span { color: #87959a; font-size: 10px; }

.process-section { padding: 82px 0; background: #fff; }
.section-intro { max-width: 600px; margin-bottom: 42px; }
.section-intro h2, .section-heading-row h2 { color: #173640; font-size: 30px; line-height: 1.3; letter-spacing: -.025em; }
.section-intro p, .section-heading-row p { margin-top: 9px; color: #77888e; font-size: 14px; }
.process-list { display: grid; grid-template-columns: repeat(4, 1fr); border-top: 1px solid #dfe8e6; border-bottom: 1px solid #dfe8e6; }
.process-item { position: relative; min-height: 224px; padding: 28px 28px 28px 0; }
.process-item + .process-item { padding-left: 28px; border-left: 1px solid #dfe8e6; }
.process-number { margin-bottom: 30px; color: #aab7b9; font-size: 11px; font-weight: 800; letter-spacing: .14em; }
.process-icon { display: grid; width: 42px; height: 42px; place-items: center; margin-bottom: 17px; border-radius: 10px; background: #eef0ff; color: #5e72e4; font-size: 20px; font-weight: 800; }
.process-item h3 { margin-bottom: 7px; color: #213d45; font-size: 16px; }
.process-item p { color: #7b8b90; font-size: 12px; line-height: 1.65; word-break: keep-all; }
.process-arrow { position: absolute; z-index: 1; top: 105px; right: -13px; display: grid; width: 26px; height: 26px; place-items: center; border: 1px solid #d7e3e0; border-radius: 50%; background: #fff; color: #6b827f; font-size: 12px; }

.services-section { padding: 86px 0 92px; background: #f7f9fc; }
.section-heading-row { display: flex; align-items: flex-end; justify-content: space-between; gap: 28px; margin-bottom: 32px; }
.text-link { flex: none; color: #5e72e4; font-size: 13px; font-weight: 800; }
.text-link span { margin-left: 7px; transition: margin .2s ease; }
.text-link:hover span { margin-left: 12px; }
.service-list { overflow: hidden; border: 1px solid #dce5e3; border-radius: 16px; background: #fff; box-shadow: 0 16px 45px rgba(40,67,70,.06); }
.service-row { display: grid; grid-template-columns: 62px minmax(0, 1fr) 110px 150px 28px; gap: 20px; align-items: center; min-height: 112px; padding: 20px 24px; transition: background .2s ease; }
.service-row + .service-row { border-top: 1px solid #e4ebe9; }
.service-row:hover { background: #f7fbfa; }
.service-symbol { display: grid; width: 58px; height: 58px; place-items: center; border-radius: 12px; font-size: 24px; font-weight: 700; }
.service-symbol.teal { background: #e2f4ef; color: #087667; }
.service-symbol.blue { background: #e5eff4; color: #366d86; }
.service-symbol.purple { background: #eee9f4; color: #6c5580; }
.service-symbol.gray { background: #edf0f0; color: #647477; }
.service-type { color: #5e72e4; font-size: 10px; font-weight: 800; }
.service-copy h3 { overflow: hidden; margin: 2px 0; color: #203c44; font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }
.service-copy p { color: #849297; font-size: 12px; }
.request-count span, .service-price span { display: block; margin-bottom: 3px; color: #8a979b; font-size: 10px; }
.request-count strong { color: #4d6268; font-size: 13px; }
.service-price { text-align: right; }
.service-price strong { color: #153942; font-size: 15px; }
.row-arrow { color: #78908c; font-size: 17px; transition: transform .2s ease; }
.service-row:hover .row-arrow { transform: translateX(4px); }
.service-empty { display: flex; align-items: center; gap: 18px; min-height: 130px; padding: 28px; border: 1px solid #dce5e3; border-radius: 16px; background: #fff; }
.empty-icon { display: grid; flex: none; width: 48px; height: 48px; place-items: center; border-radius: 12px; background: #eef0ff; color: #5e72e4; font-size: 20px; font-weight: 800; }
.service-empty div { flex: 1; }
.service-empty h3 { color: #29434b; font-size: 15px; }
.service-empty p { margin-top: 3px; color: #849297; font-size: 12px; }
.retry-button { padding: 9px 14px; border: 1px solid #c8d8d4; border-radius: 7px; background: #fff; color: #315058; font-size: 12px; }
.skeleton-row { pointer-events: none; }
.skeleton-block { display: block; border-radius: 8px; background: linear-gradient(90deg, #edf1f0 25%, #f7f9f8 50%, #edf1f0 75%); background-size: 200% 100%; animation: skeleton 1.4s infinite; }
.skeleton-icon { width: 58px; height: 58px; }
.skeleton-copy { width: min(340px, 90%); height: 34px; }
.skeleton-price { width: 110px; height: 24px; grid-column: 4; }
@keyframes skeleton { to { background-position: -200% 0; } }

.role-section { padding: 86px 0; background: #fff; }
.role-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.role-card { position: relative; overflow: hidden; min-height: 350px; padding: 46px; border-radius: 18px; }
.role-card::after { content: ''; position: absolute; width: 240px; height: 240px; right: -90px; bottom: -120px; border: 48px solid currentColor; border-radius: 50%; opacity: .07; }
.generator-card { background: #eef0ff; color: #5e72e4; }
.carrier-card { background: linear-gradient(135deg, #27364b, #4a5bd4); color: #cdd4ff; }
.role-label { display: inline-block; margin-bottom: 28px; padding: 6px 10px; border: 1px solid currentColor; border-radius: 5px; font-size: 10px; font-weight: 800; }
.role-card h2 { margin-bottom: 24px; color: #173640; font-size: 25px; line-height: 1.45; letter-spacing: -.02em; }
.carrier-card h2 { color: #fff; }
.role-card ul { margin: 0 0 30px; padding: 0; list-style: none; }
.role-card li { margin-top: 9px; color: #60777d; font-size: 13px; }
.carrier-card li { color: #b8c9cd; }
.role-card li::before { content: '✓'; margin-right: 9px; color: #5e72e4; font-weight: 800; }
.carrier-card li::before { color: #cdd4ff; }
.role-card a { position: relative; z-index: 1; color: #5e72e4; font-size: 13px; font-weight: 800; }
.carrier-card a { color: #fff; }

.cta-section { padding: 92px 24px; background: linear-gradient(135deg, #172b4d 0%, #5e72e4 60%, #825ee4 100%); text-align: center; }
.cta-inner { max-width: 700px; margin: 0 auto; }
.cta-inner > span { display: block; margin-bottom: 15px; color: #d9ddff; font-size: 10px; font-weight: 800; letter-spacing: .18em; }
.cta-inner h2 { color: #fff; font-size: 34px; line-height: 1.4; letter-spacing: -.03em; }
.cta-inner p { margin: 12px 0 30px; color: #a9bdc1; font-size: 14px; }
.cta-button { padding: 12px 20px; border-radius: 7px; background: #fff; color: #5e72e4; }
.cta-button:hover { background: #eef0ff; transform: translateY(-1px); }

.footer { padding: 28px 0; background: #172b4d; }
.footer-inner { width: min(1180px, calc(100% - 48px)); margin: 0 auto; display: flex; align-items: center; gap: 30px; color: #748d92; }
.footer-logo { display: flex; align-items: center; gap: 8px; margin-right: auto; color: #d7e4e5; font-size: 13px; font-weight: 800; }
.footer-logo span { display: grid; width: 27px; height: 27px; place-items: center; border-radius: 7px; background: rgba(255,255,255,.12); color: #d9ddff; }
.footer p, .footer small { font-size: 10px; }

@media (max-width: 900px) {
  .hero-inner { grid-template-columns: 1fr; gap: 50px; }
  .hero-copy { text-align: center; }
  .hero-copy > p { margin-right: auto; margin-left: auto; }
  .hero-actions, .trust-note { justify-content: center; }
  .workflow-panel { max-width: 620px; margin: 0 auto; }
  .process-list { grid-template-columns: repeat(2, 1fr); }
  .process-item:nth-child(3) { border-top: 1px solid #dfe8e6; border-left: 0; padding-left: 0; }
  .process-item:nth-child(4) { border-top: 1px solid #dfe8e6; }
  .process-arrow { display: none; }
  .service-row { grid-template-columns: 58px minmax(0, 1fr) 130px 24px; }
  .request-count { display: none; }
  .service-price { grid-column: 3; }
  .role-card { padding: 36px; }
}

@media (max-width: 640px) {
  .section-inner, .hero-inner, .footer-inner { width: min(100% - 32px, 1180px); }
  .hero { padding: 58px 0; }
  .hero h1 { font-size: 38px; }
  .hero-copy > p { font-size: 15px; }
  .hero-actions { flex-direction: column; }
  .hero-actions .btn { justify-content: center; }
  .trust-note { align-items: flex-start; text-align: left; }
  .workflow-panel { padding: 20px; }
  .status-card { grid-template-columns: 38px 1fr; }
  .status-card > :last-child { display: none !important; }
  .panel-summary { gap: 8px; }
  .panel-summary div { padding-left: 8px; }
  .process-section, .services-section, .role-section { padding: 64px 0; }
  .process-list { grid-template-columns: 1fr; }
  .process-item, .process-item + .process-item, .process-item:nth-child(3) { min-height: 0; padding: 24px 0; border-top: 1px solid #dfe8e6; border-left: 0; }
  .process-item:first-child { border-top: 0; }
  .process-number { margin-bottom: 16px; }
  .section-heading-row { align-items: flex-start; flex-direction: column; }
  .section-intro h2, .section-heading-row h2 { font-size: 26px; }
  .service-row { grid-template-columns: 48px minmax(0, 1fr) 18px; gap: 12px; padding: 17px 15px; }
  .service-symbol { width: 46px; height: 46px; font-size: 19px; }
  .service-price, .request-count { display: none; }
  .service-copy h3 { font-size: 14px; }
  .service-empty { align-items: flex-start; flex-wrap: wrap; }
  .service-empty .retry-button { margin-left: 66px; }
  .role-grid { grid-template-columns: 1fr; }
  .role-card { min-height: 320px; padding: 32px 26px; }
  .cta-inner h2 { font-size: 28px; }
  .footer-inner { align-items: flex-start; flex-direction: column; gap: 10px; }
}
</style>
