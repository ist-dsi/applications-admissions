import client from '@/api/client'

async function getPermissions (contest, candidate, hash) {
  const params = {}
  if (contest) {
    params.contest = contest
  }
  if (candidate) {
    params.candidate = candidate
  }
  if (hash) {
    params.hash = hash
  }

  const response = await client.get('/applicationsAdmissions/permissions', { params: params })
  return response.data
}

async function getRecaptchaConfig () {
  const response = await client.get('/applicationsAdmissions/recaptcha')
  return response.data
}

async function getContestList () {
  const response = await client.get('/applicationsAdmissions/contest')
  return response.data
}

async function getContest (id, hash) {
  const response = await client.get(`/applicationsAdmissions/contest/${id}`, {
    params: {
      hash: hash
    }
  })
  return response.data
}

async function createContest (name, beginDate, endDate) {
  const response = await client.post('/applicationsAdmissions/contest/', {
    name: name,
    beginDate: beginDate,
    endDate: endDate
  })
  return response.data
}

async function editContest (contest, name, beginDate, endDate) {
  const response = await client.patch(`/applicationsAdmissions/contest/${contest}`, {
    name: name,
    beginDate: beginDate,
    endDate: endDate
  })
  return response.data
}

async function deleteContest (contest) {
  const response = await client.delete(`/applicationsAdmissions/contest/${contest}`)
  return response.data
}

async function createContestHash (contest) {
  const response = await client.post(`/applicationsAdmissions/contest/${contest}/hash`)
  return response.data
}

async function deleteContestHash (contest) {
  const response = await client.delete(`/applicationsAdmissions/contest/${contest}/hash`)
  return response.data
}

async function exportContestCandidates (contest) {
  const response = await client.get(`/applicationsAdmissions/contest/${contest}/export`, {
    responseType: 'blob'
  })
  return response
}

async function getCandidate (candidate, hash) {
  const response = await client.get(`/applicationsAdmissions/candidate/${candidate}`, {
    params: {
      hash: hash
    }
  })
  return response.data
}

async function createCandidate (contest, name, email, recaptcha) {
  const response = await client.post(`/applicationsAdmissions/contest/${contest}/candidate`, {
    name: name,
    email: email,
    recaptcha: recaptcha
  })
  return response.data
}

async function deleteCandidate (candidate) {
  const response = await client.delete(`/applicationsAdmissions/candidate/${candidate}`)
  return response.data
}

async function createCandidateHash (candidate) {
  const response = await client.post(`/applicationsAdmissions/candidate/${candidate}/hash`)
  return response.data
}

async function deleteCandidateHash (candidate) {
  const response = await client.delete(`/applicationsAdmissions/candidate/${candidate}/hash`)
  return response.data
}

async function createCandidateSeal (candidate, hash) {
  const response = await client.post(`/applicationsAdmissions/candidate/${candidate}/seal`, {}, {
    params: {
      hash: hash
    }
  })
  return response.data
}

async function downloadAllCandidateFiles (candidate, hash) {
  const response = await client.get(`/applicationsAdmissions/candidate/${candidate}/file`, {
    params: {
      hash: hash
    },
    responseType: 'blob'
  })
  return response
}

async function downloadSingleCandidateFile (candidate, file, hash) {
  const response = await client.get(`/applicationsAdmissions/candidate/${candidate}/file/${file}`, {
    params: {
      hash: hash
    },
    responseType: 'blob'
  })
  return response
}

async function uploadCandidateFile (candidate, formData) {
  const response = await client.post(`/applicationsAdmissions/candidate/${candidate}/file`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

async function deleteCandidateFile (candidate, file, hash) {
  const response = await client.delete(`/applicationsAdmissions/candidate/${candidate}/file/${file}`, {
    params: {
      hash: hash
    }
  })
  return response.data
}

async function getCandidateLogs (candidate, hash) {
  const response = await client.get(`/applicationsAdmissions/candidate/${candidate}/logs`, {
    params: {
      hash: hash
    }
  })
  return response.data
}

export default {
  getPermissions,
  getRecaptchaConfig,

  getContestList,
  getContest,
  createContest,
  editContest,
  deleteContest,
  createContestHash,
  deleteContestHash,
  createCandidate,
  exportContestCandidates,

  getCandidate,
  deleteCandidate,
  createCandidateHash,
  deleteCandidateHash,
  createCandidateSeal,
  downloadAllCandidateFiles,
  downloadSingleCandidateFile,
  uploadCandidateFile,
  deleteCandidateFile,
  getCandidateLogs
}
