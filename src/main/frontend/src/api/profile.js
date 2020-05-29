import client from './client'

async function get () {
  try {
    const response = await client.get('/api/bennu-core/profile')
    return response.data
  } catch (err) {
    if (!process.env.DEV) {
      if (err.response.status === 401) {
        window.location.href = `/login?callback=${window.location}`
      } else {
        throw err
      }
    }
  }
}

async function changeLocale (language) {
  const localeTag = language === 'pt' ? 'pt-PT' : 'en-GB'

  await client.post(`api/bennu-core/profile/locale/${localeTag}`, null, {
    headers: { 'X-Requested-With': 'applications-admissions-frontend' }
  })
}

export default {
  get,
  changeLocale
}
