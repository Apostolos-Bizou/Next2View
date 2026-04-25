import api from './api'

export const reportService = {
  async getTemplates() {
    const res = await api.get('/reports/templates')
    return res.data
  },

  async getPreview(templateId) {
    const res = await api.get(`/reports/preview/${templateId}`)
    return res.data
  },

  async aiQuery(question) {
    const res = await api.post('/reports/ai-query', { question })
    return res.data.answer
  },

  async downloadReport(templateId) {
    const res = await api.get(`/reports/generate/${templateId}`, {
      responseType: 'blob'
    })
    // Create download link
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `${templateId}_${new Date().toISOString().slice(0,10)}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  }
}
