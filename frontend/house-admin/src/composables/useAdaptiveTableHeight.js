import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

export function useAdaptiveTableHeight(options = {}) {
  const tableCardRef = ref(null)
  const tableHeight = ref(options.minHeight ?? 320)
  let frameId = 0

  function updateTableHeight() {
    if (frameId) {
      cancelAnimationFrame(frameId)
    }

    frameId = requestAnimationFrame(() => {
      if (!tableCardRef.value) return

      const rect = tableCardRef.value.getBoundingClientRect()
      const viewportBottomGap = options.viewportBottomGap ?? 28
      const cardBottomPadding = options.cardBottomPadding ?? 24
      const reservedHeight = options.reservedHeight ?? 0
      const available = window.innerHeight - rect.top - viewportBottomGap - cardBottomPadding - reservedHeight
      tableHeight.value = Math.max(options.minHeight ?? 260, Math.floor(available))
    })
  }

  async function scheduleTableHeightUpdate() {
    await nextTick()
    updateTableHeight()
  }

  onMounted(() => {
    updateTableHeight()
    window.addEventListener('resize', updateTableHeight)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', updateTableHeight)
    if (frameId) {
      cancelAnimationFrame(frameId)
    }
  })

  return {
    tableCardRef,
    tableHeight,
    updateTableHeight,
    scheduleTableHeightUpdate
  }
}
