<template>
  <div
    class="ui item"
    :class="item.value.toLowerCase()"
    @click="menuClickHandler($event,item)">
    <div
      v-if="typeof item === 'string'"
      class="string">
      <span>{{ item }}</span>
    </div>
    <div
      v-else
      class="object"
      :class="item.class">
      <i
        v-if="!!item.icon"
        :class="item.icon"/>
      <span class="value">{{ item.value }}</span>
      <span
        class="shortcut"
        style="float:right;">{{ item.shortcut }}</span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'MenuItem',
  props: { item: { type: String, required: true } },
  methods: {
    menuClickHandler: function (e, t) {
      this.$store.dispatch(t.fn, {})
    }
  }
}
</script>
<style>
  .ui.item div {
    white-space: nowrap;
    overflow-x: clip;
  }

</style>
