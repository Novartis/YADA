<template>
  <td>
    <span
      :id="'popup-info-'+qname"
      class="info"
      v-html="octinfo"/>
    <span
      :id="'popup-comments-'+qname"
      v-if="!!comments"
      v-html="octcomment"/>
    <span
      :id="'popup-settings-'+qname"
      v-if="settings > 0"
      v-html="octsettings"/>
    <span
      :id="'popup-security-'+qname"
      v-if="!!security && security !== 'f'"
      v-html="octsec"/>
    <span
      :id="'popup-security-'+qname"
      v-else
      v-html="octalert"/>
  </td>
</template>
<script>
const octicons = require('octicons')
export default {
  props: { qname: { type: String, required: true },
    info: { type: Array, required: true },
    comments: { type: String, required: true },
    settings: { type: String, required: true },
    security: { type: String, required: true } },
  data () {
    return {
      dims: { 'width': 20, 'height': 20 }
    }
  },
  computed: {
    octinfo () { return octicons.info.toSVG({ 'class': 'info', ...this.dims }) },
    octcomment () { return octicons.comment.toSVG({ ...this.dims }) },
    octsettings () { return octicons.settings.toSVG({ ...this.dims }) },
    octsec () { return octicons.lock.toSVG({ 'class': 'secure', ...this.dims }) },
    octalert () { return octicons.alert.toSVG({ 'class': 'alert', ...this.dims }) }
  },
  methods: {

  },
  mounted () {
    $(`#popup-info-${this.qname}`).popup({
      html: `<table class="info-table">
              <tr><th>Last Access</th><td>${this.info[0]}</td></tr>
              <tr><th>Access Count</th><td>${this.info[1]}</td></tr>
              <tr><th>Creator</th><td>${this.info[3]}</td></tr>
              <tr><th>Created At</th><td>${this.info[2]}</td></tr>
              <tr><th>Modifier</th><td>${this.info[5]}</td></tr>
              <tr><th>Modified At</th><td>${this.info[4]}</td></tr>
            </table>`
    })
    $(`#popup-comments-${this.qname}`).popup({
      html: `<div class="popup-comment">${this.comments}</div>`
    })
    $(`#popup-settings-${this.qname}`).popup({
      html: `<div>Param Count: ${this.settings}</div>`
    })
    $(`#popup-security-${this.qname}`).popup({
      html: `<div class="popup-comment">${!!this.security && this.security !== 'f' ? 'Secure' : 'NOT SECURE!'}</div>`
    })
  }
}
</script>
<style>
.octicon-alert {
  margin: 0;
}
.octicon {
  padding: 2px;
  margin: 1px;
  border: 1px solid #EEE;
}

.info {
  color: blue;
}

.alert {
  color: red;
}

.secure {
  color: darkgreen;
}

table.info-table th,
table.info-table td {
  white-space: nowrap;
  font-size: smaller;
  padding-left: 3px;
  padding-right: 3px;
}

table.info-table th {
  text-align: right;
  background-color: #DDD;
}
</style>
