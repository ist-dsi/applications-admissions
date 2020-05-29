import axios from 'axios'

const client = axios.create({
  baseURL: `/${process.env.CTX !== undefined ? process.env.CTX : ''}`
})

export default client
