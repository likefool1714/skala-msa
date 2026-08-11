import { defineStore } from 'pinia'
import { ref } from 'vue'
import { courseApi } from '@/api/course.js'
import { authApi } from '@/api/auth.js'

export const useCourseStore = defineStore('course', () => {
  const courses = ref([])
  const selectedCourse = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const selectedCategory = ref('전체')

  const categories = ['전체', '일반의료폐기물', '손상성폐기물', '조직물류폐기물']

  // 백엔드 카테고리 → 프론트 표시용 카테고리
  const categoryLabelMap = {
    GENERAL_MEDICAL: '일반의료폐기물',
    SHARPS: '손상성폐기물',
    PATHOLOGICAL: '조직물류폐기물'
  }

  // 썸네일 이미지 매핑
  const thumbnailMap = {
    SPRING: new URL('../assets/images/courses/spring_boot.png', import.meta.url).href,
    VUE: new URL('../assets/images/courses/vue_js.png', import.meta.url).href,
    DOCKER: new URL('../assets/images/courses/docker.png', import.meta.url).href,
    KUBERNETES: new URL('../assets/images/courses/kubernetes.png', import.meta.url).href,
    PYTHON: new URL('../assets/images/courses/python.png', import.meta.url).href,
    AI: new URL('../assets/images/courses/generative_ai.png', import.meta.url).href,
  }

  const categoryThumbnailMap = {
    '백엔드': thumbnailMap.SPRING,
    '프론트엔드': thumbnailMap.VUE,
    'DevOps': thumbnailMap.KUBERNETES,
    '데이터': thumbnailMap.PYTHON,
    'AI': thumbnailMap.AI
  }

  function normalizeCategory(category) {
    if (!category) return ''
    return categoryLabelMap[category] || category
  }

  function normalizeCourse(course) {
    if (!course || typeof course !== 'object') return course

    return {
      ...course,
      wasteType: normalizeCategory(course.wasteType)
    }
  }

  async function attachCarrierNames(rawCourses) {
    const cache = new Map()
    return Promise.all(rawCourses.map(async (course) => {
      const carrierId = course?.carrierId
      if (!carrierId) return normalizeCourse(course)
      try {
        if (!cache.has(carrierId)) {
          cache.set(carrierId, authApi.getUser(carrierId).then(res => res.data?.data ?? res.data))
        }
        const carrier = await cache.get(carrierId)
        return normalizeCourse({ ...course, carrierName: carrier?.organizationName || carrier?.name })
      } catch (error) {
        console.warn('[CourseStore] failed to load carrier name:', carrierId, error)
        return normalizeCourse(course)
      }
    }))
  }

  function getThumbnail(course) {
    const thumbKey = course?.thumbnail?.toUpperCase?.() || ''
    if (thumbKey && thumbnailMap[thumbKey]) {
      return thumbnailMap[thumbKey]
    }

    return categoryThumbnailMap[course?.category] || null
  }

  async function fetchCourses() {
    loading.value = true
    error.value = null

    try {
      const res = await courseApi.getAll()
      console.log('[CourseStore] fetchCourses response =', res.data)

      const rawCourses = Array.isArray(res.data?.data)
        ? res.data.data
        : Array.isArray(res.data)
          ? res.data
          : []

      courses.value = await attachCarrierNames(rawCourses)

      console.log('[CourseStore] normalized courses =', courses.value)
    } catch (e) {
      console.error('[CourseStore] fetchCourses failed:', e)
      error.value = e.message || '수거 서비스 목록을 불러오지 못했습니다.'
      courses.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchCourse(id) {
    loading.value = true
    error.value = null

    try {
      const res = await courseApi.getById(id)
      console.log('[CourseStore] fetchCourse response =', res.data)

      const rawCourse =
        res.data?.data && typeof res.data.data === 'object'
          ? res.data.data
          : res.data

      selectedCourse.value = (await attachCarrierNames([rawCourse]))[0]

      console.log('[CourseStore] normalized selectedCourse =', selectedCourse.value)
    } catch (e) {
      console.error('[CourseStore] fetchCourse failed:', e)
      error.value = e.message || '수거 서비스 정보를 불러오지 못했습니다.'
      selectedCourse.value = null
    } finally {
      loading.value = false
    }
  }

  function setCategory(cat) {
    selectedCategory.value = cat
  }

  return {
    courses,
    selectedCourse,
    loading,
    error,
    categories,
    selectedCategory,
    thumbnailMap,
    categoryLabelMap,
    normalizeCategory,
    normalizeCourse,
    getThumbnail,
    fetchCourses,
    fetchCourse,
    setCategory
  }
})
