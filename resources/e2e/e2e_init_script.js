const settings = `{:word-families.settings.db/groups [{:word-families.settings.db/name "Terre" :word-families.settings.db/members ["Enterrer" "Terrien"] :word-families.settings.db/traps ["Terminer"]} {:word-families.settings.db/name "Dent" :word-families.settings.db/members ["Dentiste" "Dentelle"] :word-families.settings.db/traps ["Accident"]}]}`;

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
