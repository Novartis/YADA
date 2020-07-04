<template>
  <div>
    <div v-if="!!idle">
      <div class="ui basic modal">
        <div class="ui icon header">
          <i class="logout icon"/>
          Automatic Logout in {{ countdown }} second<span v-if="countdown!==1">s</span>
        </div>
        <div class="content">
          <p>{{ message }}</p>
        </div>
        <div class="actions">
          <div
            v-if="!!auth"
            class="ui red basic cancel inverted button"
            @click="resetTimer">
            <i class="remove icon"/>
            Stay logged in
          </div>
          <div
            class="ui green ok inverted button"
            @click="logout">
            <i class="checkmark icon"/>
            Logout
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'IdleAlert',
  data () {
    return {
      idle: false,
      auth: true,
      events: ['load', 'mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'],
      timeout: 1000 * 60 * 30,   // 30 minutes
      authTimeout: 1000 * 31, // 31 seconds
      authInterval: null,
      inactivityTimer: null,
      countdown: 10,    // count
      counter: null,    // interval for modal
      modalTimer: null, // timeout for modal
      onesec: 1000,
      message: 'To protect your data, the system will log you out automatically after 30 minutes of inactivity. What would you like to do?'
    }
  },
  methods: {

    // a second authentication-based timeout
    authenticationTime () {
      /* eslint-disable-next-line camelcase */
      let rx_cookie = /yadachkr/
      // let rx_domain = /local/
      // let login     = '/yada-admin/login.html'
      // set timeout
      // let date = new Date().getTime() / 1000
      this.authInterval = window.setInterval(() => {
        // console.log(document.cookie)
        // console.log(rx_cookie.test(document.cookie))
        // console.log(new Date().getTime()/1000 - date)
        if (!rx_cookie.test(document.cookie))// && !rx_domain.test(document.domain))
        {
          this.idle = true
          this.auth = false
          this.countdown = 10
          this.message = 'To protect your data, the system logs you out automatically and requires you to re-authenticate after 4 hours.'
          this.prompt()
          // TODO set the path cookie on logout so login takes user right back there
          // right now it doesn't work bc it creates a loop at least in local dev it does
          // document.cookie = "qdsspath=;domain=qdss.io;path=/;"
        }
      }, this.authTimeout)
    },

    // (re-)add event listeners to enable (re-)setting of of timeout
    inactivityTime () {
      // console.log('Adding listeners')
      window.addEventListener('load', this.resetTimer, true)
      this.events.forEach((evt) => {
        document.addEventListener(evt, this.resetTimer, true)
      })
    },

    removeListeners () {
      window.removeEventListener('load', this.resetTimer, true)
      this.events.forEach((evt) => {
        document.removeEventListener(evt, this.resetTimer, true)
      })
    },

    countdowner () {
      // countdown timer
      let secs = this.countdown
      // test countdown status
      if (secs > 0)
      {
        // decrement
        secs--
        // update display (reactively)
        this.countdown = secs
      }
      else
      {
        // cleanup and logout automatically
        this.clearTimers()
        this.logout()
      }
    },

    countdownerInterval () {
      // create the modal – has to be inside the timeout function to work
      $('.ui.basic.modal').modal({closable: false}).modal('show')
      // create the countdown timer
      this.counter = setInterval(this.countdowner, this.onesec)
    },

    // display modal dialog
    prompt () {
      this.removeListeners()
      // enable display of modal
      this.idle = true
      // timeout here seems to be necessary to affect DOM or rendering
      this.modalTimer = setTimeout(this.countdownerInterval, this.onesec)
    },

    // nav to login page
    logout () {
      document.location = '/login.html'
    },

    // cleanup
    clearTimers () {
      // console.log('clearTimeout')
      clearInterval(this.counter) // interval which changes countdown in modal
      clearTimeout(this.modalTimer) // timeout which renders modal
      clearTimeout(this.inactivityTimer) // inactivity monitor
    },

    // (re-)set the timeout
    resetTimer () {
      // console.log('resetTimer');
      this.idle = false
      // cleanup
      this.clearTimers()
      // (re-)set inactivityTimer
      this.inactivityTimer = setTimeout(this.prompt, this.timeout)
      // 1000 milliseconds = 1 second
    }

  },
  created () {
    window.onload = () => {
      // console.log('window.onload');
      this.inactivityTime()
    }
    document.onload = () => {
      // console.log('document.onload');
      this.inactivityTime()
    }
    document.onmousedown = () => {
      // console.log('mousedown');
      this.inactivityTime()
    }
    document.onkeypress = () => {
      // console.log('keypress');
      this.inactivityTime()
    }
    document.ontouchstart = () => {
      this.inactivityTime()
    }
    this.authenticationTime()
  }

}
</script>
<style>

</style>
