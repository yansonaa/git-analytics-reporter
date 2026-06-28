import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export function fetchReport(projectId, start, end) {
  return api.get('/reports/team', {
    params: { projectId, start, end }
  })
}

export function exportReport(projectId, start, end) {
  return api.get('/reports/team/export', {
    params: { projectId, start, end },
    responseType: 'blob'
  })
}

export function collectLocal(repoPath, projectId, since) {
  return api.post('/collect/local', null, {
    params: { repoPath, projectId, since }
  })
}

export default api
