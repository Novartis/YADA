export default function login (username, password) {
  return $.ajax({
    url: `${process.env.YADA_BASEURL}/yada.jsp?q=YADA+resource+access&pl=Authorizer,YADA`,
    type: 'POST',
    beforeSend: function (xhr) {
      xhr.setRequestHeader('Authorization', 'Basic ' + btoa(username + ':' + password))
      xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest')
    },
    xhrFields: { withCredentials: true }
  })
    .then(function (data, textStatus, jqXhr) {
      window.sessionStorage.setItem('YADA', JSON.stringify({sec: {...data, u: btoa(username)}}))
      window.location = `${process.env.YADA_ADMIN_BASEURL}/yada-admin/index.html`
    })
}

window.login = login
