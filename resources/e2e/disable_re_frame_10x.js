try {
  // Re-frame 10x panel can get in the way of e2e tests action, eg by being over a button
  console.log("Disabling re-frame-10x pane")
  window.localStorage.setItem("day8.re-frame-10x.show-panel", '"false"')
} catch (error) {
  console.error("Unable to save to localStorage")
}
