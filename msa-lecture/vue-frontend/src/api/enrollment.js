import api from './index.js'

export const enrollmentApi = {
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
  },
  getCarrierRequests() {
    return api.get('/api/enrollments/carrier/my')
  },
  acceptRequest(enrollmentId) {
    return api.post(`/api/enrollments/${enrollmentId}/accept`)
  },
  rejectRequest(enrollmentId) {
    return api.post(`/api/enrollments/${enrollmentId}/reject`)
  },
  completeRequest(enrollmentId) {
    return api.post(`/api/enrollments/${enrollmentId}/complete`)
  },
  reserve(reservation) {
    return api.post('/api/enrollments', reservation)
  },
  cancel(enrollmentId) {
    return api.delete(`/api/enrollments/${enrollmentId}`)
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  }
}
