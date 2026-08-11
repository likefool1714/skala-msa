<template>
  <div class="landing">
    <AppHeader />

    <!-- 히어로 섹션 -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-content fade-in-up">
          <span class="hero-badge">의료폐기물 수거 업무를 한 곳에서</span>
          <h1 class="hero-title">수거 신청부터 기록까지,<br><em>MediWaste Hub</em></h1>
          <p class="hero-desc">병·의원 등 배출 사업장과 수집·운반 업체를 연결하여 수거 서비스 신청, 결제와 접수 상태를 통합 관리합니다.</p>
          <div class="hero-actions">
            <router-link to="/courses" class="btn btn-primary btn-lg">수거 서비스 찾기</router-link>
            <router-link to="/login" class="btn btn-outline btn-lg">기업 계정 시작하기</router-link>
          </div>
          <div class="hero-stats">
            <div class="stat"><span class="stat-num">통합</span><span class="stat-label">수거 신청 관리</span></div>
            <div class="stat"><span class="stat-num">추적</span><span class="stat-label">접수 상태 확인</span></div>
            <div class="stat"><span class="stat-num">연결</span><span class="stat-label">배출처·수거업체</span></div>
          </div>
        </div>
        <div class="hero-visual fade-in" aria-label="의료폐기물 수거 관리 일러스트">
          <div class="pet-scene">
            <span class="scene-heart">✚</span>
            <span class="scene-paw paw-one">♻️</span>
            <span class="scene-paw paw-two">✓</span>
            <div class="pet-bubble dog">🏥</div>
            <div class="pet-bubble cat">🚚</div>
            <div class="sitter-card"><span>✓</span> 수거 접수 상태 확인</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 인기 강의 -->
    <section class="popular-section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">의료폐기물 수거 서비스</h2>
          <router-link to="/login" class="section-link">전체 보기 →</router-link>
        </div>
        <div class="course-grid">
          <div v-for="course in featuredCourses" :key="course.id" class="course-card-landing">
            <div class="card-thumb" :class="course.thumbBg">
              <span class="care-emoji">{{ course.emoji }}</span>
              <span class="available-label">수거 신청 가능</span>
            </div>
            <div class="card-body">
              <span class="badge" :class="course.badgeClass">{{ course.category }}</span>
              <h3 class="card-title">{{ course.title }}</h3>
              <div class="card-meta">
                <span class="instructor">{{ course.instructor }}</span>
                <span class="price">{{ course.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 특징 섹션 -->
    <section class="features-section">
      <div class="section-inner">
        <h2 class="section-title center">반복적인 수거 업무를 더 명확하게</h2>
        <div class="features-grid">
          <div v-for="f in features" :key="f.title" class="feature-card">
            <div class="feature-icon">{{ f.icon }}</div>
            <h3 class="feature-title">{{ f.title }}</h3>
            <p class="feature-desc">{{ f.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="cta-inner">
        <h2>지금 바로 시작하세요</h2>
        <p>공식 신고 시스템을 대체하지 않고, 수거 신청과 업무 기록 관리를 지원합니다.</p>
        <router-link to="/login" class="btn btn-primary btn-lg">무료로 시작하기</router-link>
      </div>
    </section>

    <!-- 푸터 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-logo">
          <span class="footer-symbol">✚</span>
          <span>MediWaste Hub</span>
        </div>
        <p class="footer-copy">© 2026 MediWaste Hub. B2B workflow prototype.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'

const featuredCourses = [
  { id:1, title:'서울권 일반의료폐기물 수거', category:'일반의료폐기물', instructor:'그린메디컬 운반', price:'₩120,000', emoji:'♻️', thumbBg:'thumb-teal', badgeClass:'badge-teal' },
  { id:2, title:'의원급 손상성폐기물 수거', category:'손상성폐기물', instructor:'세이프웨이스트', price:'₩150,000', emoji:'💉', thumbBg:'thumb-blue', badgeClass:'badge-blue' },
  { id:3, title:'조직물류폐기물 지정 수거', category:'조직물류폐기물', instructor:'메디클린 운송', price:'₩220,000', emoji:'🧪', thumbBg:'thumb-purple', badgeClass:'badge-purple' },
  { id:4, title:'경기 남부 정기 수거 서비스', category:'일반의료폐기물', instructor:'에코메디 로지스', price:'₩130,000', emoji:'🚚', thumbBg:'thumb-teal', badgeClass:'badge-teal' },
  { id:5, title:'치과·의원 전용 수거', category:'손상성폐기물', instructor:'클린패스', price:'₩145,000', emoji:'📦', thumbBg:'thumb-blue', badgeClass:'badge-blue' },
  { id:6, title:'냉장 운반 조건 수거', category:'조직물류폐기물', instructor:'바이오세이프', price:'₩250,000', emoji:'❄️', thumbBg:'thumb-purple', badgeClass:'badge-purple' },
]

const features = [
  { icon:'🏢', title:'기업 간 서비스 연결', desc:'의료폐기물 배출 사업장과 수집·운반 업체를 하나의 업무 흐름으로 연결합니다.' },
  { icon:'📅', title:'희망 수거일 신청', desc:'폐기물 정보와 희망 수거 시간을 입력해 온라인으로 수거를 신청합니다.' },
  { icon:'💳', title:'비용 결제와 접수 확정', desc:'결제 완료 이벤트가 처리되면 수거 접수 상태가 자동으로 확정됩니다.' },
  { icon:'📊', title:'이용 이력 기반 추천', desc:'확정된 수거 이력을 기준으로 관련 수거 서비스를 추천합니다.' },
]
</script>

<style scoped>
.landing { background: var(--color-bg-secondary); }

/* 히어로 */
.hero {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 84% 16%, rgba(255,255,255,.22) 0 7%, transparent 7.2%),
    radial-gradient(circle at 72% 78%, rgba(45,206,137,.20), transparent 18rem),
    linear-gradient(135deg, #172b4d 0%, #5e72e4 52%, #825ee4 100%);
  border-bottom: 1px solid var(--color-border);
  padding: 80px 0 64px;
}
.hero-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1.05fr .95fr;
  gap: 48px;
  align-items: center;
}
.hero-badge {
  display: inline-block;
  padding: 5px 14px;
  background: rgba(255,255,255,.14);
  color: rgba(255,255,255,.92);
  border: 1px solid rgba(255,255,255,.18);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 16px;
}
.hero-title {
  font-size: clamp(38px, 5vw, 58px);
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.5px;
  color: #fff;
  margin-bottom: 16px;
}
.hero-title em { color: #9cf0cd; font-style: normal; }
.hero-desc {
  font-size: 16px;
  color: rgba(255,255,255,.76);
  line-height: 1.7;
  max-width: 460px;
  margin-bottom: 28px;
}
.hero-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 40px;
}
.btn-lg { padding: 12px 28px; font-size: 15px; }
.hero-stats {
  display: flex;
  gap: 36px;
}
.stat { display: flex; flex-direction: column; gap: 2px; }
.stat-num { font-size: 22px; font-weight: 700; color: #fff; }
.stat-label { font-size: 12px; color: rgba(255,255,255,.68); }
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}
.pet-scene {
  position: relative;
  width: min(420px, 38vw);
  aspect-ratio: 1;
  border-radius: 34px;
  background: linear-gradient(145deg, rgba(255,255,255,.22), rgba(255,255,255,.09));
  border: 1px solid rgba(255,255,255,.26);
  backdrop-filter: blur(16px);
  box-shadow: inset 0 0 0 10px rgba(255,255,255,.06), var(--shadow-lg);
}
.pet-bubble {
  position: absolute;
  display: grid;
  place-items: center;
  background: white;
  border: 5px solid rgba(255,255,255,.75);
  box-shadow: var(--shadow-md);
}
.pet-bubble.dog { width: 170px; height: 170px; left: 44px; top: 84px; border-radius: 45% 55% 50% 50%; font-size: 92px; }
.pet-bubble.cat { width: 125px; height: 125px; right: 38px; bottom: 62px; border-radius: 50%; font-size: 68px; }
.scene-heart { position: absolute; right: 54px; top: 42px; color: #9cf0cd; font-size: 42px; transform: rotate(10deg); }
.scene-paw { position: absolute; opacity: .28; font-size: 30px; }
.paw-one { left: 34px; bottom: 44px; transform: rotate(-18deg); }
.paw-two { right: 84px; top: 128px; transform: rotate(22deg); }
.sitter-card {
  position: absolute;
  left: 24px;
  bottom: 25px;
  padding: 12px 18px;
  border-radius: 16px;
  background: white;
  box-shadow: var(--shadow-md);
  color: var(--color-text-primary);
  font-size: 13px;
  font-weight: 700;
}
.sitter-card span { color: white; background: var(--color-secondary); border-radius: 50%; padding: 2px 6px; margin-right: 6px; }

/* 강의 섹션 */
.popular-section { padding: 72px 0; background: #fff; }
.section-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.section-title { font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.section-title.center { text-align: center; margin-bottom: 40px; }
.section-link { font-size: 14px; color: var(--color-primary); font-weight: 500; }
.section-link:hover { text-decoration: underline; }

.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.course-card-landing {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: var(--transition);
  box-shadow: var(--shadow-sm);
}
.course-card-landing:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-md);
}
.card-thumb {
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.thumb-teal   { background: linear-gradient(135deg, #e5fbf2, #c6f3df); }
.thumb-blue   { background: linear-gradient(135deg, #e7f5ff, #cbdfff); }
.thumb-purple { background: linear-gradient(135deg, #f1ecff, #dacbff); }
.thumb-pink   { background: #FBEAF0; }
.care-emoji { font-size: 72px; filter: drop-shadow(0 10px 18px rgba(50,50,93,.16)); transition: transform .25s ease; }
.course-card-landing:hover .care-emoji { transform: scale(1.08) rotate(-3deg); }
.available-label { position: absolute; right: 12px; top: 12px; padding: 4px 9px; border-radius: 20px; background: rgba(255,255,255,.88); color: var(--color-secondary); font-size: 10px; font-weight: 800; }
.card-body { padding: 14px 16px; display: flex; flex-direction: column; gap: 6px; }
.card-title { font-size: 14px; font-weight: 600; color: var(--color-text-primary); line-height: 1.4; }
.card-meta { display: flex; justify-content: space-between; align-items: center; }
.instructor { font-size: 12px; color: var(--color-text-secondary); }
.price { font-size: 14px; font-weight: 600; color: var(--color-primary); }

/* 특징 */
.features-section { padding: 80px 0; background: var(--color-bg-secondary); }
.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.feature-card {
  padding: 28px 24px;
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  text-align: left;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}
.feature-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.feature-icon { width: 54px; height: 54px; display:grid; place-items:center; border-radius: 18px; background: var(--color-primary-light); font-size: 28px; margin-bottom: 18px; }
.feature-title { font-size: 15px; font-weight: 600; margin-bottom: 8px; }
.feature-desc { font-size: 13px; color: var(--color-text-secondary); line-height: 1.6; }

/* CTA */
.cta-section {
  padding: 80px 0;
  background:
    radial-gradient(circle at 15% 50%, rgba(45,206,137,.22), transparent 18rem),
    linear-gradient(135deg, #172b4d 0%, #5e72e4 55%, #825ee4 100%);
  text-align: center;
}
.cta-inner { max-width: 600px; margin: 0 auto; padding: 0 24px; }
.cta-inner h2 { font-size: 32px; font-weight: 700; color: #fff; margin-bottom: 12px; }
.cta-inner p { font-size: 16px; color: rgba(255,255,255,0.8); margin-bottom: 32px; }
.cta-inner .btn-primary {
  background: #fff;
  color: var(--color-primary);
  border-color: #fff;
  font-weight: 600;
}
.cta-inner .btn-primary:hover { background: #f0f7ff; }

/* 푸터 */
.footer {
  background: #172b4d;
  padding: 32px 0;
}
.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
}
.footer-symbol { width:28px; height:28px; display:grid; place-items:center; border-radius:8px; background:rgba(255,255,255,.12); }
.footer-copy { font-size: 13px; color: rgba(255,255,255,0.5); }

@media (max-width: 820px) {
  .hero-inner { grid-template-columns: 1fr; text-align: center; }
  .hero-desc { margin-left: auto; margin-right: auto; }
  .hero-actions, .hero-stats { justify-content: center; }
  .pet-scene { width: min(380px, 86vw); }
  .course-grid { grid-template-columns: repeat(2, 1fr); }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .hero { padding-top: 48px; }
  .hero-actions { flex-direction: column; }
  .hero-stats { gap: 18px; }
  .course-grid, .features-grid { grid-template-columns: 1fr; }
  .footer-inner { flex-direction: column; gap: 12px; }
}
</style>
