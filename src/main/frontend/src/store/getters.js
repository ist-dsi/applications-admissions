export const checkPermission = (state, getters) => (permissionKey) => {
  return state.permissions ? !!state.permissions[permissionKey] : false
}

export const isLoggedIn = (state, getters) => {
  return state.profile ? !!state.profile.username : false
}
