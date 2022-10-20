const settings = `$PLACEHOLDER`;

try {
  // Tests for localStorage persistence rely on page reloading, upon which
  // this script will be run again, so we only write to localStorage if it's
  // empty, to avoid cancelling any test produced modification
  if (!window.localStorage.getItem("settings")) {
    window.localStorage.setItem("settings", settings)
  }
} catch (error) {
  console.error("Unable to save to localStorage")
}
