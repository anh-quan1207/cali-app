---
name: Cali
description: Đếm calo Android một tay, Material 3 seed chàm, số liệu chính xác kiểu MacroFactor.
colors:
  primary: "#4355B9"
  on-primary: "#FFFFFF"
  primary-container: "#DEE0FF"
  on-primary-container: "#00105C"
  secondary-container: "#E0E1F9"
  on-secondary-container: "#181A2C"
  surface: "#FBF8FF"
  surface-low: "#F5F2FA"
  surface-cont: "#EFEDF4"
  surface-highest: "#E4E1E9"
  on-surface: "#1B1B21"
  on-surface-var: "#46464F"
  outline: "#767680"
  outline-var: "#C6C5D0"
  error: "#BA1A1A"
  on-error: "#FFFFFF"
  error-container: "#FFDAD6"
  on-error-container: "#410002"
  warn: "#8A5100"
  warn-container: "#FFDCBE"
  on-warn-container: "#2C1600"
  inverse-surface: "#303036"
  inverse-on-surface: "#F2EFF7"
  inverse-primary: "#BAC3FF"
  scrim: "rgba(0,0,0,0.32)"
  protein: "#006780"
  carb: "#4C6700"
  fat: "#8E4585"
  meal-sang: "#4355B9"
  meal-trua: "#5868C9"
  meal-toi: "#1E2A78"
  meal-phu: "#6C79D6"
  primary-dark: "#BAC3FF"
  on-primary-dark: "#08218A"
  primary-container-dark: "#293CA0"
  on-primary-container-dark: "#DEE0FF"
  secondary-container-dark: "#43465A"
  on-secondary-container-dark: "#E0E1F9"
  surface-dark: "#121318"
  surface-low-dark: "#1B1B21"
  surface-cont-dark: "#1F1F25"
  surface-highest-dark: "#35343A"
  on-surface-dark: "#E4E1E9"
  on-surface-var-dark: "#C6C5D0"
  outline-dark: "#90909A"
  outline-var-dark: "#46464F"
  error-dark: "#FFB4AB"
  on-error-dark: "#690005"
  error-container-dark: "#93000A"
  on-error-container-dark: "#FFDAD6"
  warn-dark: "#FFB86E"
  warn-container-dark: "#693C00"
  on-warn-container-dark: "#FFDCBE"
  inverse-surface-dark: "#E4E1E9"
  inverse-on-surface-dark: "#303036"
  inverse-primary-dark: "#4355B9"
  scrim-dark: "rgba(0,0,0,0.5)"
  protein-dark: "#5FD4F6"
  carb-dark: "#B1D46A"
  fat-dark: "#FFAEE9"
  meal-sang-dark: "#BAC3FF"
  meal-trua-dark: "#8E9BF5"
  meal-toi-dark: "#E4E6FF"
  meal-phu-dark: "#6B78C9"
typography:
  display-large:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "57px"
    fontWeight: 650
    lineHeight: 1.05
    letterSpacing: "-0.03em"
    fontFeature: "\"tnum\" 1"
    fontVariation: "\"opsz\" 72, \"wdth\" 92"
  display-medium:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "45px"
    fontWeight: 600
    lineHeight: 1.15
    letterSpacing: "-0.01em"
    fontFeature: "\"tnum\" 1"
    fontVariation: "\"opsz\" 48"
  display-small:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "36px"
    fontWeight: 650
    lineHeight: 1.1
    letterSpacing: "-0.02em"
    fontFeature: "\"tnum\" 1"
    fontVariation: "\"opsz\" 48, \"wdth\" 92"
  headline-medium:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "28px"
    fontWeight: 600
    lineHeight: 1.25
    letterSpacing: "-0.01em"
    fontFeature: "\"tnum\" 1"
    fontVariation: "\"opsz\" 32"
  title-large:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "22px"
    fontWeight: 600
    lineHeight: 1.27
    fontFeature: "\"tnum\" 1"
    fontVariation: "\"opsz\" 22"
  title-medium:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "16px"
    fontWeight: 600
    lineHeight: 1.5
    fontFeature: "\"tnum\" 1"
  body-large:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.5
    fontFeature: "\"tnum\" 1"
  body-medium:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "14px"
    fontWeight: 500
    lineHeight: 1.43
    fontFeature: "\"tnum\" 1"
  body-small:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.33
    fontFeature: "\"tnum\" 1"
  label-large:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "14px"
    fontWeight: 600
    lineHeight: 1.43
    fontFeature: "\"tnum\" 1"
  label-medium:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "12px"
    fontWeight: 500
    lineHeight: 1.33
    fontFeature: "\"tnum\" 1"
  label-small:
    fontFamily: "Roboto Flex, Roboto, sans-serif"
    fontSize: "11px"
    fontWeight: 500
    lineHeight: 1.45
    fontFeature: "\"tnum\" 1"
rounded:
  small: "8px"
  medium: "12px"
  large: "16px"
  extra-large: "28px"
  full: "9999px"
  bubble: "20px"
  bubble-tail: "6px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  gutter: "20px"
  xl: "24px"
components:
  button-filled:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 16px"
    height: "48px"
  button-tonal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 14px"
    height: "48px"
  button-outlined-icon:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    rounded: "{rounded.full}"
    size: "48px"
  icon-button:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-var}"
    rounded: "{rounded.full}"
    size: "48px"
  fab-extended:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.large}"
    padding: "0 20px 0 16px"
    height: "56px"
  chip-state-warn:
    backgroundColor: "{colors.warn-container}"
    textColor: "{colors.on-warn-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "4px 12px"
    height: "32px"
  chip-state-error:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "4px 12px"
    height: "32px"
  meal-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "4px 0"
  meal-badge:
    backgroundColor: "{colors.meal-sang}"
    textColor: "{colors.surface}"
    rounded: "{rounded.small}"
    size: "24px"
  tile:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "14px 14px 12px"
  tile-button:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.medium}"
    height: "48px"
  progress-track:
    backgroundColor: "{colors.surface-highest}"
    rounded: "{rounded.full}"
    height: "6px"
  nav-bar:
    backgroundColor: "{colors.surface-cont}"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-medium}"
    height: "80px"
  nav-indicator-active:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.full}"
    width: "64px"
    height: "32px"
  button-outlined:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  bottom-sheet:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.extra-large}"
  sheet-footer:
    backgroundColor: "{colors.surface-cont}"
    padding: "12px 16px 28px"
  button-save:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    padding: "0 20px 0 18px"
    height: "56px"
    width: "100%"
  chip-time-meal:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "0 6px 0 8px"
    height: "32px"
  meal-tone-swatch:
    backgroundColor: "{colors.meal-trua}"
    rounded: "2px"
    size: "10px"
  confirm-item-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "12px 4px 2px 16px"
  portion-segment:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-large}"
    size: "48px"
  portion-segment-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.large}"
    width: "40px"
    height: "32px"
  flag-check:
    backgroundColor: "{colors.warn-container}"
    textColor: "{colors.on-warn-container}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.small}"
    padding: "0 8px"
    height: "24px"
  clarify-card:
    backgroundColor: "{colors.warn-container}"
    textColor: "{colors.on-warn-container}"
    rounded: "{rounded.large}"
    padding: "14px 8px 12px 16px"
  day-lane-track:
    backgroundColor: "{colors.surface-cont}"
    height: "10px"
  day-lane-overflow:
    backgroundColor: "{colors.error}"
    rounded: "3px"
    height: "6px"
  swipe-delete:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.label-large}"
    width: "112px"
  snackbar:
    backgroundColor: "{colors.inverse-surface}"
    textColor: "{colors.inverse-on-surface}"
    typography: "{typography.body-medium}"
    rounded: "4px"
    padding: "0 4px 0 16px"
    height: "48px"
  snackbar-action:
    textColor: "{colors.inverse-primary}"
    typography: "{typography.label-large}"
    height: "48px"
  picker-sheet:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.extra-large}"
    padding: "0 20px 28px"
  time-input-field:
    backgroundColor: "{colors.surface-highest}"
    textColor: "{colors.on-surface}"
    typography: "{typography.display-medium}"
    rounded: "{rounded.small}"
    width: "96px"
    height: "72px"
  time-input-field-active:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
  meal-segmented:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  meal-segmented-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
  choice-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.large}"
    padding: "16px"
  choice-card-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
  choice-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.large}"
    padding: "10px 14px"
    height: "64px"
  choice-row-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
  choice-icon-tile:
    backgroundColor: "{colors.surface-highest}"
    textColor: "{colors.on-surface-var}"
    rounded: "{rounded.medium}"
    size: "40px"
  choice-icon-tile-selected:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
  button-step:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.full}"
    height: "56px"
    width: "100%"
  button-step-disabled:
    backgroundColor: "color-mix(in srgb, #1B1B21 12%, transparent)"
    textColor: "color-mix(in srgb, #1B1B21 50%, transparent)"
  text-field-outlined:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-large}"
    rounded: "4px"
    padding: "0 16px"
    height: "56px"
  warn-box:
    backgroundColor: "{colors.warn-container}"
    textColor: "{colors.on-warn-container}"
    typography: "{typography.body-small}"
    rounded: "{rounded.large}"
    padding: "12px 14px"
  bmi-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "14px 16px"
  bmi-scale-track:
    backgroundColor: "{colors.surface-highest}"
    height: "8px"
  bmi-scale-current:
    backgroundColor: "{colors.primary}"
    height: "8px"
  energy-mix-bar:
    backgroundColor: "{colors.protein}"
    height: "10px"
  stat-grid:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "12px 4px"
  setup-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "16px"
  chat-bubble-me:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.bubble}"
    padding: "10px 14px"
  chat-bubble-ai:
    backgroundColor: "{colors.surface-cont}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.bubble}"
    padding: "10px 14px"
  chat-bubble-error:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.bubble}"
    padding: "12px 14px"
  chat-bubble-typing:
    backgroundColor: "{colors.surface-cont}"
    rounded: "{rounded.bubble}"
    padding: "14px 16px"
  chat-food-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.bubble}"
    padding: "14px 14px 6px"
    width: "304px"
  chat-food-chip:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.small}"
    padding: "0 10px"
    height: "28px"
  chat-error-retry:
    backgroundColor: "transparent"
    textColor: "{colors.on-error-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  chat-suggestion-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "0 14px"
    height: "48px"
  chat-field:
    backgroundColor: "{colors.surface-cont}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.full}"
    padding: "0 16px"
    height: "48px"
  chat-send:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    size: "48px"
  chat-send-disabled:
    backgroundColor: "color-mix(in srgb, #1B1B21 12%, transparent)"
    textColor: "color-mix(in srgb, #1B1B21 50%, transparent)"
  attach-option:
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "0 16px"
    height: "56px"
  score-card:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    typography: "{typography.display-large}"
    rounded: "{rounded.large}"
    padding: "14px 16px 16px"
  score-lane-track:
    backgroundColor: "color-mix(in srgb, #4355B9 16%, #DEE0FF)"
    height: "10px"
  score-lane-earned:
    backgroundColor: "{colors.primary}"
    height: "10px"
  week-switcher:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    padding: "0 4px 8px"
  chip-provisional:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.small}"
    padding: "0 8px"
    height: "24px"
  report-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "14px 16px 16px"
  day-column-bar:
    backgroundColor: "{colors.primary}"
    width: "20px"
  day-column-under:
    backgroundColor: "color-mix(in srgb, #4355B9 70%, #F5F2FA)"
    width: "20px"
  day-column-overflow:
    backgroundColor: "{colors.error}"
    width: "20px"
  goal-band:
    backgroundColor: "color-mix(in srgb, #1B1B21 6%, transparent)"
  food-share-bar:
    backgroundColor: "{colors.primary}"
    height: "4px"
  ai-step-badge:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    rounded: "{rounded.full}"
    size: "24px"
  ai-stale-strip:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.body-small}"
    rounded: "{rounded.medium}"
    padding: "6px 6px 6px 12px"
  ai-error-box:
    backgroundColor: "{colors.error-container}"
    textColor: "{colors.on-error-container}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.large}"
    padding: "12px 14px"
  history-week-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
  history-week-header:
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-medium}"
    padding: "0 4px 0 14px"
    height: "48px"
  history-day-cell:
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    width: "48px"
    height: "72px"
  history-date-today:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    rounded: "{rounded.full}"
    size: "28px"
  history-mini-bar:
    backgroundColor: "{colors.primary}"
    width: "14px"
  history-mini-bar-under:
    backgroundColor: "color-mix(in srgb, #4355B9 70%, #F5F2FA)"
    width: "14px"
  history-mini-bar-overflow:
    backgroundColor: "{colors.error}"
    width: "14px"
  history-unlogged-stub:
    backgroundColor: "transparent"
    width: "14px"
    height: "10px"
  history-link-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    padding: "10px 14px"
    height: "64px"
  history-link-icon:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    rounded: "{rounded.medium}"
    size: "40px"
  history-note:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-small}"
    rounded: "{rounded.large}"
    padding: "12px 14px"
  weight-hero-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.display-large}"
    rounded: "{rounded.large}"
    padding: "14px 16px 16px"
  goal-lane-track:
    backgroundColor: "{colors.surface-highest}"
    height: "10px"
  goal-lane-fill:
    backgroundColor: "{colors.primary}"
    height: "10px"
  range-segmented:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    height: "48px"
  range-segmented-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
  weight-week-header:
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-medium}"
    padding: "12px 0 2px"
  weight-record-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-large}"
    padding: "4px 16px"
    height: "48px"
  date-chip:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "0 4px 0 8px"
    height: "32px"
  input-fix-button:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 18px 0 14px"
    height: "48px"
  recalc-table:
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    padding: "6px 0"
  day-class-chip:
    backgroundColor: "{colors.surface-cont}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "4px 12px"
    height: "32px"
  day-class-chip-dark:
    backgroundColor: "{colors.surface-highest-dark}"
    textColor: "{colors.on-surface-dark}"
  day-class-swatch:
    backgroundColor: "{colors.primary}"
    size: "10px"
  underlog-box:
    backgroundColor: "{colors.warn-container}"
    textColor: "{colors.on-warn-container}"
    typography: "{typography.body-small}"
    rounded: "{rounded.large}"
    padding: "12px 8px 8px 14px"
  underlog-quick-add:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 16px"
    height: "48px"
  week-card-stale-row:
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-small}"
    padding: "4px 4px 4px 0"
    height: "48px"
  camera-viewfinder:
    backgroundColor: "#000000"
    textColor: "{colors.on-surface-dark}"
  camera-veil-button:
    backgroundColor: "color-mix(in srgb, #000 62%, transparent)"
    textColor: "{colors.on-surface-dark}"
    rounded: "{rounded.full}"
    size: "48px"
  camera-flash-pill:
    backgroundColor: "color-mix(in srgb, #000 62%, transparent)"
    textColor: "{colors.on-surface-dark}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 16px 0 12px"
    height: "48px"
  camera-flash-pill-on:
    backgroundColor: "{colors.secondary-container-dark}"
    textColor: "{colors.on-secondary-container-dark}"
  camera-hint-pill:
    backgroundColor: "color-mix(in srgb, #000 62%, transparent)"
    textColor: "{colors.on-surface-dark}"
    typography: "{typography.label-medium}"
    rounded: "{rounded.full}"
    padding: "6px 14px"
    height: "32px"
  camera-low-light-bar:
    backgroundColor: "color-mix(in srgb, #000 62%, transparent)"
    textColor: "{colors.on-surface-dark}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.full}"
    padding: "4px 4px 4px 16px"
    height: "56px"
  camera-panel:
    backgroundColor: "#000000"
    textColor: "{colors.on-surface-dark}"
    rounded: "{rounded.extra-large}"
    padding: "16px 16px 0"
  camera-shutter:
    backgroundColor: "{colors.on-surface-dark}"
    rounded: "{rounded.full}"
    size: "72px"
  camera-side-tile:
    backgroundColor: "{colors.surface-highest-dark}"
    textColor: "{colors.on-surface-dark}"
    typography: "{typography.label-small}"
    rounded: "14px"
    size: "48px"
  ai-error-box-dark:
    backgroundColor: "{colors.error-container-dark}"
    textColor: "{colors.on-error-container-dark}"
  search-field:
    backgroundColor: "{colors.surface-highest}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    rounded: "{rounded.full}"
    padding: "0 4px 0 16px"
    height: "56px"
  food-list:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
  food-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    padding: "6px 4px 6px 16px"
    height: "64px"
  food-row-saved:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    padding: "8px 4px 8px 16px"
    height: "72px"
  favourite-star:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-var}"
    rounded: "{rounded.full}"
    size: "48px"
  favourite-star-on:
    textColor: "{colors.primary}"
  field-expander:
    backgroundColor: "transparent"
    textColor: "{colors.primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 12px 0 8px"
    height: "48px"
  no-match-card:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
    padding: "20px 16px 16px"
  info-box:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-small}"
    rounded: "{rounded.large}"
    padding: "12px 14px"
  day-lane-removed:
    backgroundColor: "transparent"
    height: "10px"
  water-lane:
    backgroundColor: "{colors.surface-highest}"
    height: "10px"
  water-lane-over:
    backgroundColor: "color-mix(in srgb, #4355B9 18%, transparent)"
    rounded: "3px"
    height: "6px"
  water-goal-button:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.medium}"
    padding: "0 4px 2px"
    height: "48px"
  water-reminder-row:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-medium}"
    rounded: "{rounded.medium}"
    padding: "0 8px"
    height: "48px"
  water-drink-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface-var}"
    typography: "{typography.body-large}"
    padding: "4px 16px"
    height: "48px"
  water-quick-add-dock:
    backgroundColor: "{colors.surface-cont}"
    padding: "12px 16px 32px"
  settings-nav-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    padding: "10px 14px"
    height: "64px"
  settings-nav-icon:
    backgroundColor: "{colors.primary-container}"
    textColor: "{colors.on-primary-container}"
    rounded: "{rounded.medium}"
    size: "40px"
  settings-nav-icon-neutral:
    backgroundColor: "{colors.surface-highest}"
    textColor: "{colors.on-surface-var}"
    rounded: "{rounded.medium}"
    size: "40px"
  settings-group:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.large}"
  settings-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    padding: "8px 12px 8px 16px"
    height: "56px"
  settings-row-value:
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
  settings-row-unit:
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-medium}"
  settings-row-switch:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-large}"
    padding: "8px 16px 8px 16px"
    height: "64px"
  switch-on:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    rounded: "{rounded.full}"
    width: "52px"
    height: "32px"
  switch-off:
    backgroundColor: "{colors.surface-highest}"
    textColor: "{colors.outline}"
    rounded: "{rounded.full}"
    width: "52px"
    height: "32px"
  time-chip:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "0 10px"
    height: "32px"
  speed-row:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    typography: "{typography.title-medium}"
    rounded: "{rounded.large}"
    padding: "10px 14px"
    height: "60px"
  speed-row-selected:
    backgroundColor: "{colors.secondary-container}"
    textColor: "{colors.on-secondary-container}"
  meal-lane-24:
    backgroundColor: "{colors.meal-phu}"
    height: "10px"
  meal-lane-now:
    backgroundColor: "{colors.on-surface}"
    width: "2px"
    height: "18px"
  confirm-dialog:
    backgroundColor: "{colors.surface-low}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.extra-large}"
    padding: "24px 24px 16px"
    width: "312px"
  button-danger:
    backgroundColor: "{colors.error}"
    textColor: "{colors.on-error}"
    typography: "{typography.label-large}"
    rounded: "{rounded.full}"
    padding: "0 20px"
    height: "48px"
  stats-month-bar:
    backgroundColor: "{colors.primary}"
    width: "6px"
  stats-month-bar-under:
    backgroundColor: "color-mix(in srgb, #4355B9 70%, #F5F2FA)"
    width: "6px"
  stats-month-bar-overflow:
    backgroundColor: "{colors.error}"
    width: "6px"
  stats-month-unlogged-stub:
    backgroundColor: "transparent"
    width: "8px"
    height: "8px"
  stats-period-average-line:
    backgroundColor: "{colors.primary}"
    height: "1px"
  stats-scrub-selection:
    backgroundColor: "color-mix(in srgb, #1B1B21 8%, transparent)"
    rounded: "4px"
  stats-scrub-tag:
    backgroundColor: "{colors.on-surface}"
    textColor: "{colors.surface}"
    typography: "{typography.label-small}"
    rounded: "4px"
    padding: "0 6px"
    height: "18px"
  stats-day-readout:
    backgroundColor: "{colors.surface-cont}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-large}"
    rounded: "{rounded.medium}"
    padding: "4px"
    height: "56px"
  stats-week-row:
    backgroundColor: "transparent"
    textColor: "{colors.on-surface}"
    typography: "{typography.body-medium}"
    padding: "6px 12px 6px 16px"
    height: "56px"
  stats-compare-delta:
    textColor: "{colors.on-surface-var}"
    typography: "{typography.label-medium}"
    width: "74px"
---

# Design System: Cali

## Overview

**Creative North Star: "Đồng hồ đo của Pixel"**

Cali là Material 3 chuẩn của thể loại đếm calo, làm ở độ tinh xảo của app Pixel. Bảng màu Material You tonal sinh từ seed chàm, chiều sâu bằng các tầng surface container chứ không bằng bóng đổ, chữ Roboto Flex với số tabular ở mọi nơi, biểu tượng Material Symbols Rounded. Cá tính không đến từ trang trí mà từ độ chính xác của số liệu: mỗi con số canh cột, mỗi đơn vị đặt nhỏ hơn và nhạt hơn con số nó đi kèm, mỗi trạng thái vượt mục tiêu được nói rõ bằng chữ, biểu tượng và màu cùng lúc.

Mật độ vừa phải, thiên về đọc nhanh: một màn hình đọc được trong một cái liếc, số kcal còn lại luôn nổi nhất. Thao tác chính nằm trong vùng ngón cái, mọi vùng chạm từ 48dp. Giao diện sáng và tối là hai bản đầy đủ của cùng một scheme, không phải bản đảo màu. Ngoại lệ duy nhất là Camera (S11): màn đó luôn dùng scheme tối, bất kể giao diện hệ thống (The Forced-Dark Camera Rule).

Người dùng đã loại ba hướng: xanh lá Material tươi mát, ấm áp kem/serif, và số liệu nền tối kiểu mono. Màu thương hiệu là chàm; xanh lá chỉ xuất hiện như một chuỗi dữ liệu (tinh bột), không bao giờ như màu chủ đạo.

**Key Characteristics:**
- Material You tonal scheme, seed chàm #4355B9, sáng/tối đầy đủ.
- Tonal surface thay bóng đổ; chỉ thứ nổi trên nội dung (FAB, snackbar) có bóng.
- Một font Roboto Flex cho mọi chữ, số tabular toàn cục.
- Ba màu dữ liệu cố định cho đạm / tinh bột / béo, tách khỏi chàm, hổ phách và đỏ.
- Ba trạng thái mục tiêu: trong mục tiêu / vượt nhẹ (≤10%, hổ phách) / vượt nhiều (error), luôn kèm chữ và biểu tượng.
- Vòng calo chia đoạn theo bữa, theo thứ tự giờ ăn, là chữ ký thị giác.
- Hổ phách còn có nghĩa thứ hai, "cần kiểm tra" (số liệu AI cần xem lại), và nghĩa thứ ba, "đã điều chỉnh" (mục tiêu bị kẹp về mức an toàn, BMI mục tiêu thấp); đỏ chỉ cho vượt >10%, macro vượt, lỗi nhập của ô nhập, lỗi gửi tin nhắn và nút xoá cuối cùng "Xoá vĩnh viễn" (S45).
- Một ngữ pháp chọn duy nhất: thẻ / hàng viền 1dp chuyển sang secondary-container với viền 2dp primary, không dấu tick, không tự sang bước.
- Thanh làn "Hôm nay sau khi lưu" là vòng calo duỗi thẳng: cùng tông bữa, cùng ngữ pháp đoạn.
- Chat AI là một luồng tin nhắn: kết quả AI là bong bóng, thẻ món thu gọn mang theo làn "còn lại"; sổ chỉ đổi khi lưu (The Ledger Rule).
- Tổng kết tuần (S35) là bảy ngày của vòng calo xếp cạnh nhau: làn điểm, cột ngày và thanh macro cùng ngữ pháp đoạn (đầu vuông, khe 3dp), phần vượt tách khỏi phần trong mục tiêu ngay tại vạch (The Goal-Line Split Rule).
- Lịch sử (S30) là các hàng S35 thu nhỏ: mỗi tuần một thẻ có điểm, bảy ô ngày là bảy cột calo mini trên một vạch mục tiêu chung của cả hàng, cùng ngữ pháp Đạt / Thiếu / Vượt tách tại vạch.
- Cân nặng (S31) đọc theo tuần, cùng đơn vị với S35: số lớn là cân trung bình tuần, đường đậm nối trung bình các tuần, từng lần cân chỉ là ô nhỏ nhạt; thay đổi cân không bao giờ mang màu đánh giá (The No-Judgement Scale Rule).
- Chi tiết ngày (S33) là S10 dùng lại cho một ngày đã qua: cùng vòng, thẻ bữa và macro, nhưng tâm vòng nói kết quả của ngày (Δ có dấu và loại ngày của 08 §2) thay cho số còn lại.
- Nước (S15) là thanh làn của S12 duỗi theo mục tiêu nước: mỗi lần uống là một đoạn primary đầu vuông, khe 3dp, toàn chiều dài làn là mục tiêu; số lớn là tổng ngày, từng lần uống đọc nhỏ và nhạt như từng lần cân của S31. Nước không có trạng thái vượt: phần trên mục tiêu tách sang làn riêng 6dp nhưng vẫn primary, chữ trung tính (The Goal-Line Split Rule).
- Camera (S11) là màn duy nhất bị ép tối: viewfinder và panel dưới là #000 (gốc của `ColorScheme.scrim`), mọi nút và chữ đè lên ảnh trực tiếp nằm trên lớp che #000 62%; chip giờ ăn · bữa của S12 đứng ngay trên nút chụp để thấy giờ sẽ ghi trước khi bấm. Một lần chụp là một ảnh.
- Món đã lưu đọc giống nhau ở S12, S13, S14 và S34: một hàng món là dòng thẻ xác nhận thu gọn (tên, khẩu phần, chấm macro 6dp, kcal canh phải). Thêm nhanh (S13) và Sửa món (S14) là S12 thu gọn hoặc mở lại, nói tác động lên ngày bằng cùng làn "sau khi lưu" (The One Food Row Rule).
- Cài đặt (S40–S45) là bảng thông số của chính đồng hồ đo: mỗi hàng nói giá trị đang dùng (nhãn ← số tabular canh phải), mọi thay đổi chạm tới mục tiêu calo đi qua sheet Đang dùng / Mới của S31 và áp dụng từ hôm nay, khung giờ bốn bữa là một làn 24 giờ cùng ngữ pháp đoạn của vòng calo, và hộp thoại xác nhận duy nhất của app là Xoá toàn bộ dữ liệu (The One Dialog Rule).
- Thống kê (S32) là các ngày của vòng calo xếp cạnh nhau, đọc bằng trung bình của một kỳ Tuần hoặc Tháng: số lớn duy nhất là kcal TB mỗi ngày, cột ngày, thanh macro và làn nước dùng lại ngữ pháp của S35 / S15, cộng một vạch TB của kỳ liền 1dp primary cạnh vạch mục tiêu nét đứt. Không điểm, không xếp loại, không AI: những thứ đó ở S35, sau một hàng 64dp. Mọi so sánh với kỳ trước là mũi tên trung tính, và kỳ đang diễn ra chỉ so với cùng số ngày của kỳ trước (The Same-Days Rule).

## Colors

Một hệ tonal chàm điềm tĩnh, nơi màu chỉ mang nghĩa: chàm là thương hiệu và bữa ăn, ba màu dữ liệu cho macro, hổ phách cho vượt nhẹ, cho "cần kiểm tra" và cho "đã điều chỉnh", đỏ cho vượt nhiều và lỗi nhập.

Khi chuyển sang Compose: các token có tên M3 đi thẳng vào `lightColorScheme` / `darkColorScheme` (biến thể `-dark` trong frontmatter là giá trị dark). Ánh xạ tên: `surface-low` → `surfaceContainerLow`, `surface-cont` → `surfaceContainer`, `surface-highest` → `surfaceContainerHighest`, `on-surface-var` → `onSurfaceVariant`, `outline-var` → `outlineVariant`, `inverse-surface` → `inverseSurface`, `inverse-on-surface` → `inverseOnSurface`, `inverse-primary` → `inversePrimary`. `scrim` là `ColorScheme.scrim` (đen) cộng độ mờ của thành phần: 32% ở sáng, 50% ở tối. `warn*`, `protein/carb/fat` và `meal-*` không có vai trò trong `ColorScheme`: đưa vào một lớp màu mở rộng (`CaliColors` qua `CompositionLocal`) có cả bản sáng và tối. Camera (S11) bọc trong `darkColorScheme` (và bản tối của `CaliColors`) kể cả khi máy ở giao diện sáng; #000 của viewfinder, panel dưới và lớp che 62% là `ColorScheme.scrim` ở độ mờ của thành phần, không phải token mới.

### Primary
- **Chàm Seed** (#4355B9 / tối #BAC3FF): nút filled "Chụp món", biểu tượng nút thêm món trong thẻ bữa, biểu tượng nguồn món (AI / đã lưu / nhập tay), vòng focus, thanh tiến trình nước. Trong onboarding: nút bước 56dp, phần đã đi của thanh tiến độ bước, viền 2dp và ô biểu tượng của thẻ chọn đang chọn, đoạn hiện tại trên thang BMI, viền 2dp của ô nhập đang focus. Cũng là trạng thái "trong mục tiêu": khi chưa vượt, vòng calo chỉ có các tông chàm. Trên S35: phần điểm đã đạt của làn điểm, cột ngày "Đạt" và phần trong mục tiêu của cột "Vượt", cột hôm nay ở ngữ pháp đoạn chờ, đường và ô vuông của sparkline cân, thanh tỉ phần món, biểu tượng đầu thẻ AI và đầu mục nhận xét, chấm 4dp dưới nhãn ngày hôm nay. Trên S30: cột mini "Đạt" và phần trong mục tiêu của cột mini "Vượt", viền trong 1.5dp của thẻ tuần này, vòng 28dp quanh số ngày hôm nay (số primary 700) và viền 1.5dp của cột hôm nay (không tô nền), cờ `flag` 16dp của ngày bắt đầu dùng app và `flag` 20dp của ghi chú người mới. Trên S31: phần đã đi của làn tiến độ cân, đường TB tuần 2dp và viền ô vuông 7dp (ô cuối tô đặc), đường dự kiến nét đứt 1.5dp, biểu tượng đầu dòng diễn giải (`flag`, `trending_down`, `speed`, `verified_user`), phần chênh "−10 kcal" của mục tiêu mới. Trên S33: ô chú giải "Đạt" trong chip loại ngày, chữ của nút thêm nhanh trong hộp nghi ghi thiếu, `sync` và "Tạo lại" của hàng dữ liệu đã đổi. Trên S11 (bản tối): nút filled "Cho phép" / "Mở Cài đặt" / "Phân tích" / "Thử lại", vạch quét 2dp trên ảnh lúc phân tích, huy hiệu bước đã xong và vòng 2dp quanh huy hiệu bước đang chạy, con trỏ 2dp của ô ghi chú, biểu tượng 18dp đầu các dòng thông tin. Trên S13 / S14 / S34: phần khớp của kết quả tìm (700, gạch chân 1.5dp), dòng phụ "Hôm nay · còn N kcal" của thanh sheet, ngôi sao yêu thích đang bật (FILL 1), `bookmark_added` của thẻ sửa món, nút bước "Tiếp" của Nhập tay, nút "Lưu vào Món của tôi", nút mở rộng "Sửa tên, kcal, macro" và biểu tượng đầu hộp thông tin. Trên S15: các đoạn của làn nước (một đoạn mỗi lần uống) và phần trên mục tiêu ở làn riêng 6dp, `water_drop` FILL 1 của dòng đầu thẻ và của nút "+250 ml", `check_circle` FILL 1 của "Đạt mục tiêu", biểu tượng chuông đầu hàng nhắc, nhãn "Đạt mục tiêu · N ml" của hàng lần uống chạm mục tiêu, nút filled "+250 ml", biểu tượng `add` của nút outlined "Thêm lượng khác", nút chữ "Xoá" của sheet sửa lần uống, và các biểu tượng 18dp của sheet mục tiêu (`calculate`, `monitor_weight`, `event_available`). Trên S40–S45: rãnh switch đang bật (nút 24dp on-primary), viền 2dp của hàng tốc độ đang chọn, dấu `check_circle` của "Đã kết nối" và "Tổng 100%". Trên S32: cột "Đạt" và phần trong mục tiêu của cột "Vượt" (tuần 20dp, tháng 6dp), viền trong 1.5dp của cột hôm nay (không tô nền, như S30), vạch TB của kỳ liền 1dp, chấm 4dp dưới trục ở hôm nay, phần tô của làn nước TB, đường TB tuần 2dp và ô vuông 7dp của thẻ cân tháng, biểu tượng đầu dòng (`swap_vert` 18dp của nhiều nhất / ít nhất, `flag` 20dp FILL 1 của ghi chú người mới, ba biểu tượng 20dp của "Sắp có số"), và nút chữ "Mở ngày" của hàng đọc số.
- **Chàm Sương** (primary-container #DEE0FF / tối #293CA0): nền FAB mở rộng, nền huy hiệu trạng thái trống, ô biểu tượng 40dp của thẻ thiết lập (S03), nhãn nhỏ "Khuyên dùng" trên hàng tốc độ chưa chọn (chuyển primary / on-primary khi hàng được chọn). Trên S35 là nền thẻ điểm; rãnh làn điểm là primary 16% pha vào primary-container. Trên S30 là ô biểu tượng 40dp góc 12dp của bốn hàng "Xem thêm". Trên S40 là ô biểu tượng 40dp góc 12dp của các hàng điều hướng (cùng ngữ pháp "Xem thêm" của S30); trên S45 là ô `backup` của thẻ sao lưu. Trên S32 là ô `insights` 40dp góc 12dp của hàng "Xem tổng kết tuần".
- **Chàm Lam Nhạt** (secondary-container #E0E1F9 / tối #43465A): nút tonal "Chat AI", chỉ báo tab đang chọn trên navigation bar, nút nhỏ trong ô Nước, nền thẻ chọn / hàng chọn đang chọn, dải chọn của bánh xe năm sinh, nút tonal "Kiểm tra" (S03), bong bóng tin nhắn của bạn trên S20 (căn phải). Trên S35: chip "Tạm tính · còn N ngày", huy hiệu số 24dp của "3 việc cho tuần sau", dải "dữ liệu đã đổi" của thẻ AI, nút tonal "Tạo nhận xét tạm tính". Trên S30: chip "Tạm tính · N" ở đầu thẻ tuần này. Trên S31: phần đang chọn của bộ chọn khoảng 7 / 30 / 90 ngày / Tất cả, chip ngày của sheet Ghi cân, nút sửa một chạm "Có phải 74,1 kg?", chip "Tuần này · tạm tính" của thẻ cân. Trên S33: chip "Tạm tính · còn N ngày" của thẻ tuần đang diễn ra. Trên S11 (bản tối): viên thuốc flash ở trạng thái "Bật", nút tonal "Bật đèn" của thanh thiếu sáng, chip giờ ăn · bữa, nút tonal "Chụp lại" / "Ảnh khác", huy hiệu bước phân tích đang chạy. Trên S13 / S34: phần đang chọn của bộ lọc Gần đây / Hay dùng / Yêu thích và bộ sắp xếp Hay dùng / Gần đây / A–Z (bộ chọn khoảng của S31), nút tonal "Hỏi AI". Trên S15: nút tonal "+500 ml" và chip giờ uống 32dp của sheet thêm / sửa. Trên S40–S45: dải thiếu quyền thông báo (S43, ngữ pháp dải "dữ liệu đã đổi"), chip giờ 32dp của khung nhắc, giờ gửi và giờ bữa, chip "Đã sửa" của system prompt, phần chọn của bộ chọn "Tự động / Tùy chỉnh" và chu kỳ nhắc, nền hàng tốc độ đang chọn, nút tonal "Kiểm tra kết nối", "Xuất file" và nút sửa một chạm "Dời Phụ bắt đầu từ 14:31". Trên S32: phần đang chọn của bộ chọn Tuần / Tháng, và chip "Tạm tính · còn N ngày" của hàng chọn kỳ (như S35).

### Tertiary (dữ liệu và bữa ăn)
- **Đạm, Xanh Mòng Két** (protein #006780 / tối #5FD4F6), **Tinh Bột, Xanh Ô Liu** (carb #4C6700 / tối #B1D46A), **Béo, Mận Chín** (fat #8E4585 / tối #FFAEE9): chấm 8dp và thanh tiến trình của ba macro, và ba đoạn của thanh chồng tỉ lệ năng lượng trên S02. Cố định, không đổi theo trạng thái.
- **Bốn tông bữa** (meal-sang, meal-trua, meal-toi, meal-phu): đều là chàm, mỗi bữa một độ sáng/độ đậm khác. Dùng cho đoạn vòng calo của bữa và ô biểu tượng 24dp ở đầu thẻ bữa, hai chỗ luôn khớp nhau. Ô tông nhỏ 10dp góc 2dp đứng trước tên bữa trên chip giờ ăn và trong nút chọn bữa cũng dùng đúng token đó.

### Màu dẫn xuất
- **Chàm Thiếu** (`--under` = primary 70% pha vào surface-low; tối: #BAC3FF 70% vào #1B1B21): không phải token mới, chỉ là một mức pha của primary. Dùng cho cột "Thiếu" (dưới mục tiêu quá 10%) trên biểu đồ calo, cột nước chưa đạt và ô chú giải của chúng trên S35. Trên S30 là cột mini "Thiếu" và ô chú giải "Thiếu"; trên S33 là ô 10dp của chip loại ngày "Thiếu"; trên S15 là cột ngày chưa đạt nước và ô chú giải "Chưa đạt" của biểu đồ 7 ngày (biểu đồ nước của S35 dùng lại). Trên S32 là cột "Thiếu" của biểu đồ tuần và tháng cùng ô chú giải. Thiếu là nhạt hơn, không phải màu trạng thái: không bao giờ hổ phách, không bao giờ đỏ.

### Trạng thái
- **Hổ Phách** (warn #8A5100, container #FFDCBE / tối #FFB86E, #693C00): vượt nhẹ, tối đa 10% mục tiêu; và "cần kiểm tra": cờ "Cần kiểm tra" (độ tin cậy thấp), "Số liệu lệch" (|kcal − (4P+4C+9F)| / (4P+4C+9F) > 20%, P / C / F là gram đạm / tinh bột / béo), "Có vẻ cao" (món trên 2.500 kcal), biểu tượng của ghi chú cảnh báo dưới món, và thẻ câu hỏi làm rõ của AI; và "đã điều chỉnh": tốc độ giảm cân bị kẹp về mức an toàn (chữ warn trong hàng tốc độ, hộp warn-container trên S02), cân nặng mục tiêu cho BMI dưới 18,5. Trên S35, trong nghĩa "cần kiểm tra": biểu tượng `warning` và nhãn giá trị màu warn trên cột ngày nghi ghi thiếu (dưới một nửa mục tiêu), cùng dòng ghi chú "có thể còn sót món"; và chip "Vượt nhẹ X%" của cả tuần. Trên S30: `warning` 14dp tô đặc màu warn ở góc trên phải số ngày nghi ghi thiếu (dưới 50% mục tiêu, không áp cho hôm nay), và mục "Nghi ghi thiếu N" của chú giải. Trên S33: `warning` 16dp tô đặc màu warn cuối chip "Thiếu" của ngày nghi ghi thiếu, và hộp warn-container nói có thể còn sót món; tâm vòng "Đạt · vượt nhẹ +N" của ngày vượt ≤10% (vẫn tính Đạt). Trên S41: dòng warn `info` 14dp "Đã chỉnh · ~0,52 kg/tuần" của hàng tốc độ bị kẹp, vẫn ≥4.5:1 trên hàng đang chọn (≈5,0:1 trên secondary-container sáng, ≈5,4:1 tối), và hộp warn-container nêu giới hạn 25% TDEE khi ghi đè hoặc khi sheet Đang dùng / Mới bị kẹp. Trên S32: chip "Vượt nhẹ X%" và Δ màu warn khi TB kỳ vượt ≤10%; trong nghĩa "cần kiểm tra", `warning` 16dp + nhãn giá trị warn trên cột tuần nghi ghi thiếu, `warning` 14dp tô đặc warn trên đỉnh cột tháng, dòng `warning` 14dp "Nghi ghi thiếu" của hàng đọc số, và dòng ghi chú "có thể còn sót món" (không áp cho hôm nay).
- **Đỏ Error** (error #BA1A1A, container #FFDAD6 / tối #FFB4AB, #93000A): vượt hơn 10%, và macro vượt mục tiêu. Lỗi nhập của ô nhập (ngoài khoảng cho phép, API key sai) dùng error cho viền 2dp, nhãn và dòng hỗ trợ có biểu tượng `error`. Ngoài trạng thái, error-container còn hai việc: nền lộ ra khi vuốt xoá món (hành động phá huỷ, có Hoàn tác), và bong bóng lỗi gửi tin trên S20 (không có kết nối), cùng đánh dấu "Chưa gửi" màu error kèm biểu tượng `error` trên tin nhắn của bạn. Trên S35: phần vượt của cột ngày và của thanh macro (sau khe 3dp), nhãn giá trị cột vượt (650), chip "Vượt X%" của tuần, và hộp lỗi của thẻ AI. Trên S30: phần vượt của cột mini (sau khe 3dp) và ô chú giải "Vượt". Trên S33: nhãn và số Δ ở tâm vòng của ngày vượt >10% ("Vượt mục tiêu +620"). Trên S31, error chỉ là lỗi khoảng của ô kg; thay đổi cân không bao giờ đỏ. Trên S11: hộp lỗi (error-container tối) của hai trạng thái kết thúc, mất kết nối và AI không trả lời sau 60 giây. Trên S15, error chỉ là lỗi khoảng của ô ml (khi rời ô) và nền error-container của vuốt xoá; lượng nước không bao giờ đỏ hay hổ phách, kể cả khi uống quá mục tiêu, và "Xoá" trong sheet sửa lần uống là chữ primary. Trên S40–S45: ô API key bị 401 (lỗi của ô nhập: viền, nhãn, dòng hỗ trợ), dòng lỗi trùng khung giờ trong TimeInput (S44), và nền nút filled "Xoá vĩnh viễn" ở bước 2 của Xoá toàn bộ dữ liệu (S45), nút filled error duy nhất của app. Hàng "Xoá toàn bộ dữ liệu" và bước 1 không đỏ. Trên S32: phần vượt của cột tuần (tối thiểu 4dp) và cột tháng (tối thiểu 3dp), nhãn giá trị cột vượt (650), Δ và chip "Vượt X%" khi TB kỳ vượt >10%, phần vượt của thanh macro. Mũi tên so với kỳ trước và thay đổi cân không bao giờ đỏ.
- **Chữ Trên Đỏ** (on-error #FFFFFF / tối #690005): đúng một chỗ dùng: chữ và biểu tượng `delete_forever` trên nút filled error "Xoá vĩnh viễn" (S45; ≈6,5:1 sáng, ≈7,7:1 tối). Không dùng ở nơi nào khác; chữ trên error-container vẫn là on-error-container.

### Neutral
- **Giấy Tím Nhạt** (surface #FBF8FF / tối #121318): nền màn hình, nền thanh trên lúc chưa cuộn.
- **Tầng Thấp** (surface-low #F5F2FA / tối #1B1B21): thẻ bữa, ô Nước / Cân nặng, khối trạng thái trống; trong onboarding: thẻ chọn chưa chọn, thẻ BMI, lưới chỉ số S02, thẻ thiết lập S03, nền bánh xe năm sinh. Trên S30: thẻ tuần, lưới tóm tắt, khối bốn hàng "Xem thêm" và ghi chú người mới. Trên S31: bốn thẻ (cân TB tuần, Xu hướng, BMI, Bản ghi), hàng bản ghi, và nền sheet Ghi cân / tính lại mục tiêu. Trên S33: thẻ tuần (thẻ tuần trước của S10 dùng lại). Trên S11 (tối): khung ảnh xem lại / phân tích / lỗi. Trên S15: thẻ tổng ngày, thẻ "Các lần uống" và hàng lần uống, thẻ tuần, nền sheet thêm / sửa / mục tiêu.
- **Tầng Giữa** (surface-cont #EFEDF4 / tối #1F1F25): navigation bar, thanh trên sau khi cuộn (trên S35 cả hàng chọn tuần, trên S30 cả hàng thứ T2…CN), rãnh vòng calo ở chế độ sáng; trên S20 là bong bóng AI, bong bóng đang gõ và ô soạn tin; trên S33 là nền chip loại ngày ở chế độ sáng (tối: surface-highest). Trên S11 (tối): chân ghim của bước xem lại và ô ghi chú (chat-field). Trên S15: chân ghim thêm nhanh (+250 / +500 / lượng khác). Trên S32: thanh trạng thái, thanh trên và hàng chọn kỳ ‹ › khi cuộn; nền hàng đọc số của biểu đồ tháng ở chế độ sáng (đó là rãnh vòng: tối là surface-highest).
- **Tầng Cao Nhất** (surface-highest #E4E1E9 / tối #35343A): rãnh thanh macro (cả thanh macro S35) và rãnh thanh tỉ phần món; rãnh vòng calo ở chế độ tối; rãnh thanh tiến độ bước, các đoạn thang BMI không phải đoạn hiện tại, ô biểu tượng của thẻ chọn chưa chọn; rãnh làn tiến độ cân (S31). Trên S11 (tối): hai ô 48dp Thư viện / Ghi chú cạnh nút chụp và ô 72dp của màn xin quyền. Trên S13 / S34: nền ô tìm 56dp ở cả hai chế độ (token sẵn có, không thêm token). Trên S15: rãnh làn nước 10dp ở cả hai chế độ (khác rãnh của làn S12, vốn là surface-cont ở chế độ sáng; trên thẻ surface-low, surface-cont gần như không thấy). Trên S40–S45: rãnh switch đang tắt (viền 2dp outline, nút 16dp outline), ô biểu tượng trung tính 40dp của Giới thiệu (S40) và "Xoá toàn bộ dữ liệu" (S45), nền ô giờ của TimeInput (S44).
- **Mực** (on-surface #1B1B21 / tối #E4E1E9) và **Mực Phụ** (on-surface-var #46464F / tối #C6C5D0): chữ chính và mọi đơn vị, nhãn, giờ ăn. Trên lịch S30, số ngày của tháng kề là Mực Phụ; số ngày tương lai và trước ngày bắt đầu dùng app là on-surface 38% (như nút tắt). Trên S11 (tối): vòng căn đĩa 2dp, vòng 4dp và lõi 54dp của nút chụp, chữ và biểu tượng trên lớp che.
- **Viền** (outline #767680, outline-var #C6C5D0): viền nút outlined, đường kẻ giữa các món (outline-var ở 55%), viền nét đứt của bữa chưa ghi, tay nắm 32×4dp của bottom sheet (outline-var), viền 1dp của thẻ chọn chưa chọn (outline-var), đường ngăn cột trong lưới chỉ số S02 (outline-var 60%), đường đáy 1dp của biểu đồ cột S35 (outline-var) và đường ngăn trong thẻ báo cáo (outline-var 55%). Vạch mục tiêu nét đứt 1dp của biểu đồ cột dùng on-surface-var; ô chưa ghi nét đứt 1.5dp dùng outline; dải ±10% là on-surface 6%. Trên S30: vạch mục tiêu chung của mỗi hàng tuần là nét đứt 1dp on-surface-var 70%, thụt 8dp hai đầu; ô chưa ghi 14×10dp nét đứt 1.5dp outline. Nút chọn bữa dạng segmented và viền lúc nghỉ của ô nhập outlined dùng outline đầy đủ. Trên S31: lưới và đường đáy biểu đồ xu hướng outline-var 55%, vạch mục tiêu nét đứt 1dp on-surface-var, đường "Hôm nay" 1dp outline, ô lần cân 5dp on-surface-var, bộ chọn khoảng viền và đường ngăn 1dp outline; đường ngăn hàng bản ghi, bảng tính lại và hàng dữ liệu đã đổi (S33) là outline-var 55%. Trên S11 (tối): nút outlined 56dp / 48dp và chip gợi ý ghi chú viền 1dp outline; huy hiệu bước chưa tới viền 1dp outline-var. Trên S14: phần kcal sắp bỏ khi sửa giảm khẩu phần là đoạn rỗng viền nét đứt 1.5dp outline trên làn ngày. Trên S32: bộ chọn Tuần / Tháng viền và đường ngăn 1dp outline; trục tháng là đường 1dp outline-var với vạch 1×5dp ở mỗi Thứ Hai; ô chưa ghi của tháng 8×8dp nét đứt 1.5dp outline; đường ngăn của hàng tuần, bảng so sánh và "Sắp có số" là outline-var 55%.
- **Mực Đảo** (inverse-surface, inverse-on-surface, inverse-primary): chỉ cho snackbar; nút hành động "Hoàn tác" màu inverse-primary.
- **Màn Che** (scrim): phủ dưới sheet chọn giờ / bữa. Trên Camera (S11), gốc đen của nó (#000, `ColorScheme.scrim` đặc) là nền viewfinder và panel dưới, và ở 62% (`--veil`) là lớp che dưới mọi nút và chữ đè lên ảnh trực tiếp. Mức 62% được chọn để chữ on-surface tối vẫn ≥4.5:1 kể cả trên ảnh trắng (#E4E1E9 trên #000 62% phủ trắng ≈4.8:1). Đây là ngoại lệ có ghi chép, không phải màu mới của bảng màu.

### Named Rules
**The Data-Series Lock Rule.** Đạm, tinh bột, béo luôn là ba màu đó, ở mọi màn hình. Không màu dữ liệu nào được dùng cho thương hiệu, trạng thái hay bữa ăn, và chúng được giữ xa chàm, hổ phách và đỏ.

**The Three States Rule.** Trong mục tiêu = primary, vượt ≤10% = warn, vượt >10% = error. Trạng thái không bao giờ chỉ là màu: luôn có chip với biểu tượng (`info` / `warning`) và chữ "Vượt nhẹ X%" / "Vượt X%", và số ở tâm vòng đổi thành "+N" (trên S33, nhãn và Δ ở tâm cùng mang màu trạng thái). Trên S32, TB kcal/ngày của kỳ theo cùng ba trạng thái: Δ so với mục tiêu là on-surface khi thiếu hoặc bằng, warn khi vượt ≤10%, error khi vượt >10%, và khi vượt luôn có chip ngay dưới số.

**The Amber Check Rule.** Hổ phách có đúng ba nghĩa: vượt nhẹ (≤10%), cần kiểm tra, và đã điều chỉnh (The Amber Adjust Rule). Mọi cờ kiểm tra số liệu (độ tin cậy thấp, kcal lệch macro, món trên 2.500 kcal) và thẻ câu hỏi làm rõ đều là hổ phách, luôn kèm biểu tượng (`help` cho độ tin cậy thấp, `warning` cho số liệu lệch hoặc cao) và chữ, và không bao giờ chặn Lưu. "Số liệu lệch" dùng đúng một công thức ở mọi nơi (S12, Nhập tay của S13, món đã lưu của S34; đặc tả 03): |kcal − (4P+4C+9F)| / (4P+4C+9F) > 20%. Chỉ kiểm tra khi có macro, hiện khi rời ô macro cuối (không trong lúc gõ), kèm một dòng Body Small nêu phép tính ("Macro cộng lại ~362 kcal (4·28 + 4·40 + 9·10), 520 kcal lệch 44% …"), và không bao giờ chặn Lưu hay Tiếp. Đỏ không bao giờ dùng để kiểm tra số liệu: đỏ chỉ là vượt >10%, macro vượt mục tiêu, lỗi nhập không hợp lệ của ô nhập, và lỗi gửi tin nhắn trong chat (luôn kèm biểu tượng và chữ). Trên biểu đồ tuần, hổ phách chỉ là cờ nghi ghi thiếu (một dạng "cần kiểm tra"); ngày "Thiếu" không bao giờ hổ phách, nó là Chàm Thiếu (`--under`). Danh sách ba nghĩa là đóng: thiếu sáng trên Camera (S11) không phải "cần kiểm tra" số liệu, nên nó là gợi ý trung tính (`brightness_low` trên lớp che), không bao giờ hổ phách. Nước (S15) không mang hổ phách ở đâu cả: uống quá mục tiêu không phải trạng thái (xem The Goal-Line Split Rule), nhắc không chạy được vì thiếu quyền là một hàng trung tính, và lỗi khoảng của ô ml là lỗi nhập nên là đỏ.

**The Amber Adjust Rule.** Khi app tự kẹp mục tiêu về mức an toàn, hoặc cân nặng mục tiêu cho BMI dưới 18,5, báo bằng hổ phách với biểu tượng `info` và chữ nêu giá trị thực ("Đã chỉnh · ~0,52 kg/tuần", "App đã điều chỉnh về 1.720 kcal/ngày (~0,52 kg/tuần)"). Trong hàng chọn là chữ warn kèm `info` 14dp; ở mức màn là hộp warn-container. Không bao giờ chặn Tiếp hay Bắt đầu.

**The No-Judgement Scale Rule.** Thang BMI không tô màu theo phân loại: các đoạn là rãnh surface-highest, chỉ đoạn chứa giá trị hiện tại là primary; tên phân loại ("Béo phì độ I", "Bình thường") là chữ on-surface-var. Không đỏ, không hổ phách, không xanh trên thang. Cùng luật cho thay đổi cân (S31, S35, S30): "−0,5 kg", "+0,3 kg" luôn là chữ trung tính (on-surface cho số, on-surface-var cho lời), dù tăng hay giảm, dù nhanh hay chậm hơn kế hoạch; đường xu hướng luôn là primary. Trên S32 cũng vậy, và luật mở rộng sang mọi so sánh với kỳ trước (S32, S35): mũi tên `arrow_upward` / `arrow_downward` 16dp và phần chênh là on-surface-var cho kcal, ngày đạt, đạm, nước và cân như nhau, kèm câu "Mũi tên chỉ tăng hay giảm, không phải tốt hay xấu."

**The Goal-Line Split Rule.** Thanh nào chạy qua mục tiêu thì tách tại mục tiêu: phần trong mục tiêu giữ màu của nó, khe 3dp, rồi phần vượt mang màu trạng thái. Trên S35 là cột ngày (primary đến vạch + khe + error), trên S30 là cột mini 14dp của lịch (cùng cách tách, phần vượt tối thiểu 3dp); trên S32 là cột tuần 20dp (như S35) và cột tháng 6dp (phần vượt tối thiểu 3dp); thanh macro trên S10 và S35 (màu dữ liệu đến vạch + khe + error). Vòng calo và thanh làn ngày theo cùng ý nhưng đưa phần vượt sang vòng / làn riêng 6dp. Không bao giờ tô cả thanh sang màu trạng thái khi chỉ phần dư là vượt. Nước (S15) tách hình nhưng không đổi màu: nước không có trạng thái vượt (08 §2: từ mục tiêu trở lên là Đạt), nên làn nước dừng ở vạch, phần trên mục tiêu chạy làn riêng 6dp trên rãnh primary 18% và vẫn là primary; chữ trung tính "Đạt mục tiêu · +N ml", không bao giờ hổ phách hay đỏ, không bao giờ "vượt". Cột nước trên biểu đồ 7 ngày cũng không tách: ngày trên mục tiêu là một cột primary liền.

**The Indigo Meal Rule.** Bữa ăn chỉ dùng tông chàm; đoạn vòng và ô màu đầu thẻ bữa luôn cùng một token. Bữa còn được nhận diện bằng tên và biểu tượng, không chỉ bằng tông. Ở chế độ sáng, các tông kề nhau (meal-sang / meal-trua / meal-phu) chỉ cách nhau khoảng 1.3:1; nếu cần tách rõ hơn trong Compose, dịch sắc độ (hue), không dịch độ sáng, để mỗi tông vẫn đạt ≥3:1 so với rãnh vòng.

**The Forced-Dark Camera Rule.** Camera (S11: viewfinder, xin quyền, xem lại, phân tích, lỗi) luôn dùng scheme tối, bất kể giao diện hệ thống; đây là màn duy nhất như vậy, vì camera tối theo bản chất. Viewfinder và panel dưới là #000 (gốc `ColorScheme.scrim`); mọi nút và chữ đè lên ảnh trực tiếp nằm trên lớp che #000 62% để chữ ≥4.5:1 kể cả trên ảnh trắng. #000 và lớp che là ngoại lệ có ghi chép của scrim, không phải màu bảng màu: không dùng ở màn nào khác, và mọi màu khác trên Camera vẫn là token `-dark` của frontmatter.

## Typography

**Display Font:** Roboto Flex (dự phòng Roboto, sans-serif)
**Body Font:** Roboto Flex
**Icon Font:** Material Symbols Rounded (FILL 0, wght 400, opsz 24; FILL 1 cho biểu tượng đang chọn, biểu tượng bữa và camera)

**Character:** Một họ chữ duy nhất, dùng trục `opsz` và `wdth` để cùng một font vừa làm số lớn chặt chẽ vừa làm nhãn nhỏ dễ đọc. Dấu tiếng Việt hiển thị đầy đủ; mọi số là tabular.

Cỡ chữ là sp, khoảng cách là dp (1px trong mockup = 1sp hoặc 1dp). Mockup nhân cỡ chữ với `--fs` để mô phỏng cỡ chữ hệ thống; trong Compose, `sp` tự co giãn, còn vòng calo 220dp thì giữ nguyên. Độ đậm 650 và các trục `opsz`/`wdth` cần Roboto Flex dạng variable (`FontVariation.Settings`), không dùng `FontFamily.Default`.

### Hierarchy
- **Display Large** (650, 57sp, line 60sp, -0.03em, opsz 72, wdth 92): ở tâm vòng calo: số kcal còn lại (hoặc "+N" khi vượt) trên S10, mục tiêu kcal/ngày trong vòng trống trên S02; và điểm tuần trong thẻ điểm S35 (màn không có vòng calo; dòng cao .9 để "/100 điểm" ngồi trên đường chân chữ). Trên S33 (ngày đã qua) là Δ có dấu so với mục tiêu ("−320", "+620"), hoặc mục tiêu ngày trong vòng trống khi ngày chưa ghi. Trên S31 là cân trung bình tuần ("74,1", dòng cao .9, "kg" Title Medium on-surface-var bên cạnh). Trên S15 là tổng nước của ngày ("1.250", dòng cao .9, "ml" Title Medium on-surface-var bên cạnh). Trên S32 là kcal TB mỗi ngày của kỳ ("1.533", dòng cao .9, "kcal" Title Medium on-surface-var bên cạnh), chỉ khi kỳ có ≥3 ngày ghi.
- **Display Medium** (600, 45sp, line 52sp, -0.01em, opsz 48): chỉ cho giờ và phút trong ô nhập giờ (TimeInput) của sheet giờ ăn & bữa. S44 dùng lại đúng TimeInput đó cho giờ bắt đầu / kết thúc bữa.
- **Display Small** (650, 36sp, line 40sp, -0.02em, opsz 48, wdth 92): tổng kcal trên thẻ xác nhận, cạnh thumbnail 64dp; mục tiêu kcal mới trên sheet tính lại mục tiêu (S31, F09), cạnh mục tiêu đang dùng ở Title Large on-surface-var. Trên S41: mục tiêu đang dùng trong thẻ đầu màn ("1.740" + "kcal/ngày" Label Medium on-surface-var), cùng cỡ với số Mới của sheet Đang dùng / Mới.
- **Headline Medium** (600, 28sp, line 35sp, -0.01em, opsz 32): tiêu đề mỗi bước onboarding và màn thiết lập ("Mức vận động", "Thiết lập nhanh"), canh trái, một dòng câu hỏi.
- **Title Large** (600, 22sp, line 28sp): "Hôm nay" trên thanh trên, số lớn trong ô Nước / Cân nặng, tiêu đề sheet "Giờ ăn & bữa"; số đang nhập trong ô nhập outlined (600, -0.01em), số đang chọn của bánh xe năm sinh (650). Trên S35: tiêu đề "Tổng kết tuần", xếp loại trong thẻ điểm ("Tốt"), cân TB tuần; câu chính của thẻ Calo / Nước ở độ đậm 500 với con số đậm 650 ("Vượt **420 kcal** so với mục tiêu"). Trên S30: "Lịch sử" trên thanh trên. Trên S31: "Cân nặng" trên thanh trên, tiêu đề sheet "Ghi cân nặng" / "Cập nhật mục tiêu calo?", mục tiêu đang dùng (on-surface-var) trên sheet tính lại. Trên S33: ngày trên thanh trên ("Thứ Tư, 12/11"). Trên S11: tiêu đề thanh trên "Xem lại ảnh" / "Ảnh từ thư viện" / "Chưa phân tích được", tiêu đề "Cần quyền dùng camera" và trạng thái "Đang nhận diện món ăn…". Trên S15: "Nước · Hôm nay" / "Nước · 12/11" trên thanh trên, tiêu đề sheet "Thêm lượng khác" / "Sửa lần uống" / "Mục tiêu nước", và mục tiêu "2.600" trên sheet mục tiêu. Trên S32: "Thống kê" trên thanh trên, nước TB "2,17" (L) và cân TB tuần / "76,0 → 74,6" của thẻ cân.
- **Title Medium** (600, 16sp, line 24sp): tên bữa, tổng kcal bữa, số Đã ăn / Mục tiêu, giá trị macro, tiêu đề mục "Bữa ăn"; tiêu đề thanh sheet "Xác nhận món", tên món và kcal món trên thẻ xác nhận; nhãn của thẻ chọn / hàng chọn, chữ nút bước 56dp, "BMI 26,0", giá trị trong lưới chỉ số và chú giải thanh năng lượng, câu "Mục tiêu mỗi ngày của bạn". Trên S35: "Tuần dd/mm – dd/mm" của hàng chọn tuần, "/100 điểm", tiêu đề thẻ báo cáo ("Calo", "Nước", "Macro · TB mỗi ngày"), giá trị macro, câu đầu của nhận xét AI. Trên S30: tiêu đề mục ("Tuần này · tạm tính", "Tháng N · tóm tắt", "Xem thêm"), giá trị lưới tóm tắt, tên bốn hàng "Xem thêm". Trên S15: phần trăm "48%" của nút mục tiêu, tiêu đề thẻ "Các lần uống" / "Tuần này · 10/11 – 16/11". Trên S40–S45: tiêu đề mục ("Của bạn", "Hồ sơ", "Tổng kết tuần"), tiêu đề hàng điều hướng S40, tên bữa ở S44, và giá trị số canh phải của hàng cài đặt ("170", "0,5", "×1,375"), tabular. Trên S32: "Tuần này · 10/11 – 16/11" / "Tháng 11, 2026" của hàng chọn kỳ, tiêu đề thẻ ("Calo · TB mỗi ngày", "Các tuần trong tháng", "So với tháng trước"), Δ so với mục tiêu và thay đổi cân, TB kcal của hàng tuần.
- **Body Large** (400, 16sp, line 24sp): chỉ số kg của hàng bản ghi trên S31, màu on-surface-var, để từng lần cân đọc nhỏ và nhạt hơn số TB tuần ở đầu nhóm. Trên S11: lời giải thích quyền camera (on-surface-var) và chữ trong ô ghi chú. Trên S13 / S34: chữ trong ô tìm (chữ mồi "Tìm trong N món đã lưu" on-surface-var). Trên S15: lượng ml của hàng lần uống, on-surface-var (đơn vị "ml" 12sp 500), như số kg của S31. Trên S41–S45: nhãn của hàng cài đặt và giá trị chữ canh phải ("Nam", "Giảm cân", 400); chữ trong ô ghi chú cá nhân của S42.
- **Body Medium** (500, 14sp, line 20sp): tên món, kcal từng món, lời dẫn một dòng dưới tiêu đề bước, hậu tố đơn vị trong ô nhập, dòng dự kiến đạt cân. Trên S31: ngày của hàng bản ghi, lời dẫn sheet tính lại, các ô của bảng Đang dùng / Mới. Trên S11: dòng của thanh thiếu sáng, "Đã chờ 0:07" (on-surface-var), các bước phân tích, ghi chú trích lại ở trạng thái lỗi. Trên S13 / S34: tên món của hàng món, kcal cùng cỡ ở 600 canh phải. Trên S15: giờ của hàng lần uống (600, wdth 90), chữ của hàng nhắc, hậu tố "ml" của ô nhập.
- **Body Small** (400, 12sp, line 16sp): khẩu phần, lời dẫn trạng thái trống, ghi chú y tế; dòng phụ của hàng chọn, dòng hỗ trợ / lỗi dưới ô nhập, nội dung hộp hổ phách, "Cách tính". Trên S30: dòng phụ của hàng "Xem thêm" và ghi chú người mới. Trên S15: nguồn của lần uống ("Trong app" / "Từ thông báo" / "Từ widget"), dòng hỗ trợ / lỗi của ô ml, các dòng của sheet mục tiêu, ghi chú ngày đã qua trên chân ghim.
- **Label Large** (600, 14sp, line 20sp): chữ trên nút, chip trạng thái, nhãn "Còn lại" / "Vượt mục tiêu" / "kcal mỗi ngày", nhãn nhóm trường ("Năm sinh", "Tốc độ mỗi tuần"). Trên S35: điểm từng phần "28/40", tiêu đề mục nhận xét AI ("Làm tốt", "Cần cải thiện", "Quy luật", "3 việc cho tuần sau"). Trên S30: số ngày trong ô lịch (600; hôm nay 700 primary; ngày tháng kề, tương lai và trước khi dùng app 500) và điểm tuần ở đầu thẻ tuần (650). Trên S11: chữ trạng thái của viên thuốc flash ("Tự động" / "Bật" / "Tắt"). Trên S15: dòng đầu thẻ "Đạt mục tiêu · +N ml" khi đã đạt (600 on-surface).
- **Label Medium** (500, 12sp, line 16sp): đơn vị (kcal, g, ml), ngày, giờ ăn (600, wdth 90), nhãn navigation bar, tên macro; "Bước n/5", số vạch thang BMI, nhãn lưới chỉ số, hệ số vận động "×1,375". Trên S35: dòng trạng thái tuần, tên phần điểm, mũi tên so với tuần trước, nhãn ngày dưới cột (hôm nay 700 on-surface), chú giải biểu đồ, thứ hạng món. Trên S30: tháng dưới tiêu đề, hàng thứ T2…CN, khoảng ngày ở đầu thẻ tuần (tuần này 600 on-surface), "· Xếp loại", chip "Tạm tính · N", chú giải, nhãn lưới tóm tắt. Trên S11: viên gợi ý trên viewfinder, nhãn "Ghi chú cho AI (không bắt buộc)". Trên S15: dòng đầu thẻ "Đã uống hôm nay · N lần", "của 2.600 ml", nhãn dưới làn ("Còn **1.350** ml · ~6 cốc 250 ml" / "Đạt lúc **21:10**"), dòng phụ ngày trên thanh trên, chú giải biểu đồ. Trên S40–S45: đơn vị của giá trị hàng cài đặt ("cm", "kg/tuần", 500 on-surface-var, cách số 3dp), "~N tuần" của hàng tốc độ, chú giải làn 24 giờ, "phút" trong bộ chọn chu kỳ, chip "Đã sửa".
- **Label Small** (500, 11sp, line 16sp): "còn X g" / "vượt X g" dưới thanh macro; trên S20 là giờ tin nhắn (tin của AI kèm `auto_awesome`) và nhãn ngày ("Hôm nay", "Hôm qua"). Trên S35: nhãn giá trị trên cột (nền surface-low góc 4dp che vạch mục tiêu), nhãn tuần dưới sparkline cân, thời điểm tạo nhận xét AI. Trên S30: "—" của ngày chưa ghi (nền surface-low góc 4dp che vạch mục tiêu). Trên S31: nhãn trục và vạch mục tiêu của biểu đồ xu hướng ("Hôm nay" đậm 700 on-surface), nhãn phụ dưới ngày của hàng bản ghi, đơn vị trong bảng tính lại. Trên S33: "thiếu X g" / "đạt" / "vượt X g" dưới thanh macro của ngày đã qua. Trên S11: nhãn "Thư viện" / "Ghi chú" dưới hai ô 48dp cạnh nút chụp. Trên S34: dòng "N lần · lần cuối hôm nay 07:15" on-surface-var của hàng món đã lưu. Trên S15: nhãn "Đạt mục tiêu · N ml" (600 primary, `check_circle` 14dp FILL 1) dưới nguồn của hàng lần uống chạm mục tiêu; nhãn giá trị trên cột nước. Trên S44: nhãn giờ 00:00 / 06:00 / 12:00 / 18:00 / 24:00 dưới làn 24 giờ. Trên S32: nhãn ngày dd/mm của trục tháng (ở các Thứ Hai), nhãn ngày 18dp trên lớp chọn (600, nền on-surface, chữ surface), "—" của ô chưa ghi tháng, nhãn tuần dưới đường cân của tháng.

### Named Rules
**The Remaining-First Rule.** Display Large chỉ có một chỗ trên màn. Khi màn có vòng calo, nó luôn ở tâm vòng: số calo còn lại trên S10 (hôm nay), hoặc mục tiêu kcal/ngày khi vòng còn trống trên S02 và trên S33 cho ngày chưa ghi (chính con số người dùng sẽ thấy mỗi sáng). Ngày đã qua (S33) không còn gì để "còn lại", nên tâm là Δ có dấu so với mục tiêu kèm loại ngày. Tổng kết tuần (S35) không có vòng calo, nên chỗ duy nhất đó là điểm tuần trong thẻ điểm; Cân nặng (S31) cũng không có vòng, chỗ đó là cân trung bình tuần, không bao giờ là một lần cân lẻ. Nước (S15) cũng không có vòng: chỗ đó là tổng nước của ngày, không bao giờ một lần uống lẻ; mục tiêu chỉ nói một lần (vạch cuối làn + "của 2.600 ml"). Thống kê (S32) cũng không có vòng: chỗ đó là kcal TB mỗi ngày của kỳ đang xem, không bao giờ một ngày lẻ hay tổng kỳ, và chỉ hiện khi kỳ có ≥3 ngày ghi; dưới mức đó không có số lớn giả mà là ghi chú `flag` của người mới. Không con số nào khác được lớn ngang nó. Thẻ xác nhận không có số còn lại, nên số lớn nhất của nó là tổng kcal ở Display Small, một bậc dưới; Display Medium chỉ sống trong ô nhập giờ. Headline Medium là chữ, không bao giờ là số liệu.

**The Tabular Rule.** `tnum` bật cho toàn app; số và đơn vị luôn tách cỡ: số ở vai trò lớn hơn, đơn vị ở Label Medium màu Mực Phụ.

## Layout

Màn hình 360dp cột đơn, cuộn dọc. Thanh trạng thái 32dp và top app bar 64dp dính ở trên (lưới 48dp / 1fr / 48dp); khi cuộn, cả hai đổi nền từ `surface` sang `surface-cont` trong 200ms. Navigation bar 4 tab ở dưới (80dp cộng vùng thanh cử chỉ).

Mọi mockup dùng chung một lịch giả để số liệu khớp giữa các màn: 13/11/2026 là Thứ Năm và là "hôm nay" (lịch thật là Thứ Sáu). Code dùng lịch thật; bố cục và quy tắc không đổi.

Nhịp khoảng cách theo bội 4dp: 4, 8, 12, 16, 20, 24. Nội dung chữ và hàng nút canh lề 20dp; thẻ bữa và ô dữ liệu canh lề 12dp để nền tonal của chúng rộng hơn cột chữ. Giữa các thẻ 8dp, giữa các khối lớn 16 đến 20dp. Hàng nút: filled co giãn 1.25, tonal 1, outlined vuông 48dp, cách nhau 8dp. Ba macro chia ba cột đều, cách 12dp. Hai ô Nước / Cân nặng chia đôi, cách 8dp.

Khi hàng nút cuộn khuất, FAB mở rộng "Chụp món" hiện ở góc phải dưới, cách mép phải 16dp và nằm trên navigation bar, để thao tác chính luôn cách một chạm.

Bottom sheet toàn màn hình (thẻ xác nhận) bắt đầu cách đỉnh 40dp, để thanh trạng thái còn thấy phía trên. Từ trên xuống: tay nắm 32×4dp cách 12dp, thanh 52dp (lưới 48dp / 1fr / 48dp: đóng, tiêu đề giữa, menu), thân cuộn, và chân ghim đáy nền surface-cont (đệm 12dp / 16dp / 28dp) chứa nút Lưu 56dp toàn chiều rộng. Thân giữ quy ước lề của S10: chữ 20dp, thẻ 12dp, giữa các thẻ món 8dp. Khối tóm tắt là lưới 64dp / 1fr cách 14dp; thẻ "Hôm nay sau khi lưu" cách 16dp phía trên, tiêu đề danh sách món cách 20dp.

Sheet phụ (giờ ăn & bữa) neo đáy trên màn che, đệm 0 / 20dp / 28dp; hai nút cuối canh phải, cách 8dp.

Onboarding (S01–S03) không có navigation bar. Dưới thanh trạng thái là thanh trên 56dp (lưới 48dp / 1fr / 48dp): nút quay lại (ẩn ở bước 1), giữa là thanh tiến độ 6dp với "Bước n/5" bên dưới (cách 6dp, đệm ngang 8dp); S02 và S03 chỉ có nút quay lại. Nội dung đệm 8dp trên, lề 20dp; tiêu đề Headline, lời dẫn cách 6dp, khối chọn cách 18 đến 20dp. Chân ghim đáy đệm 12 / 20 / 28dp chứa nút bước 56dp toàn chiều rộng (S03 thêm nút chữ 48dp "Để sau" bên dưới, cách 4dp); khi bàn phím số mở, chân nằm ngay trên bàn phím với đệm đáy 10dp. Hai thẻ chọn đứng cặp chia đôi, cách 10dp; hàng chọn xếp dọc cách 8dp. S02 cuộn trong vùng giữa thanh trên và chân, chân Bắt đầu luôn ghim.

Chat AI (S20) giữ khung màn chính: thanh trên 64dp (‹ / "Chat AI" Title Large với dòng phụ "Thứ Năm, 13/11 ▾ · còn N kcal" Label Medium / ›), luồng tin nhắn cuộn lề 12dp, khoảng cách giữa tin 12dp (4dp giữa bong bóng và giờ), ô soạn tin dính trên navigation bar (đệm 8 / 8 / 8 / 4dp, cách 4dp). Bong bóng của bạn rộng tối đa 86% và căn phải, bong bóng AI tối đa 96% và căn trái; thẻ món cố định 304dp. Tin nhắn nhóm theo ngày ăn (eaten day), ngăn bằng nhãn ngày canh giữa.

Tổng kết tuần (S35) là màn con: thanh trên 64dp chỉ có nút quay lại và "Tổng kết tuần" Title Large, không navigation bar. Ngay dưới là hàng chọn tuần dính (lưới 48dp / 1fr / 48dp, đệm 0 / 4 / 8dp). Khi cuộn, thanh trạng thái, thanh trên và hàng chọn tuần cùng đổi sang surface-cont trong 200ms (`cubic-bezier(.2,0,0,1)`). Thân cuộn lề 12dp, các thẻ cách nhau 8dp, theo thứ tự đọc: điểm, Calo, Nước, Macro, Cân nặng, Món nhiều calo nhất, Nhận xét của AI, rồi một dòng chú thích Body Small canh giữa. Biểu đồ cột tràn hết bề ngang thẻ (lề âm 16dp) để bảy cột mỗi cột đúng 48dp. Màn đầu (mở từ thông báo) chứa thẻ điểm và đầu thẻ Calo cùng biểu đồ.

Lịch sử (S30) là tab cấp cao: navigation bar với Lịch sử được chọn. Thanh trên 64dp (lưới 48dp / 1fr / 48dp): ‹ / nút tiêu đề góc 12dp với "Lịch sử" Title Large và dòng "Tháng 11, 2026" + `arrow_drop_down` 18dp Label Medium on-surface-var (chạm chọn tháng) / ›; › tắt ở tháng hiện tại (on-surface 38%, `aria-disabled`). Ngay dưới là hàng thứ T2…CN dính, lưới 7 × 48dp, lề 12dp, đệm đáy 6dp, để mỗi thứ thẳng cột với ô ngày bên dưới. Khi cuộn, thanh trạng thái, thanh trên và hàng thứ cùng đổi sang surface-cont trong 200ms (`cubic-bezier(.2,0,0,1)`). Thân cuộn lề 12dp, đệm đáy 16dp, các khối cách nhau 8dp, theo thứ tự: các tuần của tháng (từ tuần chứa ngày 1 đến tuần chứa ngày cuối, Thứ Hai đầu tuần), chú giải, rồi tóm tắt: tháng đang diễn ra là "Tuần này · tạm tính" (TB mỗi ngày, Ngày đạt tính cả hôm nay, Nước TB), tháng đã qua là "Tháng N · tóm tắt" (TB mỗi ngày, Ngày đạt n/số ngày của tháng, Cân TB); người mới thay lưới tóm tắt bằng ghi chú `flag`. Cuối cùng là "Xem thêm" với bốn hàng, ở mọi trạng thái. Tiêu đề mục Title Medium, đệm 12dp trên và 8dp ngang. Màn đầu chứa thanh trên, hàng thứ, toàn bộ lưới tháng và chú giải.

Cân nặng (S31) là màn con, mở từ ô Cân nặng của S10 hoặc hàng "Cân nặng" của S30: thanh trên 64dp chỉ có nút quay lại và "Cân nặng" Title Large, không navigation bar; khi cuộn, thanh trạng thái và thanh trên đổi sang surface-cont trong 200ms. Thân cuộn lề 12dp, các thẻ cách nhau 8dp, đệm đáy 96dp để FAB không che bản ghi cuối, theo thứ tự: thẻ cân TB tuần, Xu hướng, BMI, Bản ghi, rồi một dòng Body Small canh giữa "Chạm một bản ghi để sửa; vuốt trái để xoá." Màn không có hàng nút, nên FAB mở rộng "Ghi cân" (`add`) luôn hiện, cách mép phải 16dp; khi có snackbar, FAB nâng lên trên nó. Biểu đồ xu hướng và hàng bản ghi tràn hết bề ngang thẻ (lề âm 16dp). Ghi cân và tính lại mục tiêu (F09) là sheet phụ neo đáy kiểu S12 (xem Sheet giờ ăn & bữa), không phải sheet toàn màn hình; khi bàn phím số mở, đệm đáy sheet còn 12dp. Màn đầu chứa thẻ cân TB tuần và đầu thẻ Xu hướng.

Chi tiết ngày (S33) là S10 dùng lại cho một ngày đã qua, mở từ ô ngày của S30 hoặc cột ngày của S35. Là màn con: không navigation bar. Thanh trên 64dp lưới 48dp / 1fr / 48dp / 48dp: quay lại; nút tiêu đề góc 12dp (đệm 2 / 8dp) với ngày "Thứ Tư, 12/11" Title Large và nhãn tương đối ("Hôm qua", "Tuần này", "Tuần trước", "3 tuần trước") Label Medium on-surface-var + `arrow_drop_down` 18dp, chạm mở lịch; ‹ ngày trước; › ngày sau (từ hôm qua, › sang Hôm nay tức S10). Thân giữ thứ tự và lề của S10: vòng, hàng "Đã ăn / Mục tiêu" (cách 40dp, ẩn khi chưa ghi), chip loại ngày (cách 10dp), hộp nghi ghi thiếu nếu có (lề 12dp, cách 12dp), macro (lề 20dp, cách 16dp), hàng nút, thẻ tuần (lề 12dp, cách 16dp), "Bữa ăn" + thẻ bữa, hai ô Nước / Cân nặng, miễn trừ. FAB mở rộng "Thêm món" (`add`) hiện khi hàng nút cuộn khuất, như S10.

Camera (S11) mở từ "Chụp món" của S10 và xong thì sang sheet xác nhận S12. Không navigation bar, không top app bar dạng surface: viewfinder tràn cả màn trên nền #000. Thanh trạng thái 32dp, ngay dưới là hàng điều khiển 64dp (đệm ngang 8dp): đóng bên trái; bên phải viên thuốc flash và nút đổi camera, cách 8dp. Viên gợi ý một dòng canh giữa, cách đỉnh màn 100dp, lề 12dp. Vòng căn đĩa 264dp canh giữa theo chiều ngang. Panel dưới #000, góc trên 28dp, đệm 16 / 16 / 0dp, nằm trọn trong vùng ngón cái: chip giờ ăn · bữa canh giữa; cách 12dp là hàng nút chụp cao 72dp, lưới 1fr / 72dp / 1fr (Thư viện sát trái, nút chụp giữa, Ghi chú sát phải); rồi 12dp và vùng thanh cử chỉ. Thanh thiếu sáng nổi ngay trên panel, lề 12dp, trong tầm ngón cái. Các bước còn lại (xin quyền, xem lại, phân tích, lỗi) là màn đặc nền surface tối: thanh trên 64dp (`close` hoặc `arrow_back` + tiêu đề Title Large), khung ảnh lề 12dp (xem lại 272dp, ảnh thư viện 250dp, phân tích 240dp, lỗi 208dp), chữ lề 16dp, chân ghim đáy. Chân của bước xem lại là surface-cont đệm 12 / 16 / 4dp với tonal "Chụp lại" và filled "Phân tích" co giãn, cách 8dp; chân của phân tích, lỗi và xin quyền trong suốt, nút xếp dọc cách 8dp. Màn xin quyền canh trái, lề 24dp.

Thêm nhanh (S13) là sheet toàn màn hình của S12, không phải màn mới: cách đỉnh 40dp, tay nắm, thanh 52dp (lưới 48dp / 1fr / 48dp: `close` / "Thêm nhanh" Title Medium với dòng phụ "Hôm nay · còn N kcal" Label Medium 600 primary / ô trống), thân cuộn, chân ghim surface-cont (đệm 12 / 16 / 28dp) chứa hai nút 48dp chia đều cách 8dp. Trong thân từ trên xuống: ô tìm 56dp lề 8 / 12dp; hàng chip giờ ăn · bữa đệm 12 / 20dp, nguồn bằng chữ cách 12dp; bộ chọn ba phần 48dp lề 12 / 20dp; khối danh sách lề 12dp, cách 12dp. Khi đang tìm, bộ chọn nhường chỗ cho tiêu đề mục "N món khớp" Title Medium với "tìm không dấu · hay dùng trước" Label Medium on-surface-var bên phải (đệm 16 / 20 / 8dp) và một dòng Body Small canh giữa cuối danh sách. Nhập tay giữ cùng sheet, `arrow_back` thay `close`, các ô lề 20dp cách nhau 14dp; hàng Khẩu phần / Gram là lưới 1fr / 112dp, ba ô macro chia ba cột đều, cách 8dp; chân ghim nút bước 56dp.

Sửa món (S14) là thẻ S12 mở lại trong cùng sheet: thanh `close` / "Sửa món" / `delete`; thân là chip giờ ăn · bữa, thẻ món (lề 12dp), hàng mở rộng 48dp lề 4 / 12dp, các ô (lề 20dp; Kcal / Đạm / Tinh bột / Béo là lưới 1.2fr / 1fr / 1fr / 1fr cách 8dp) khi mở, rồi thẻ làn ngày cách 16dp; chân ghim nút Lưu 56dp "Lưu thay đổi" + kcal mới.

Món của tôi (S34) là màn con mở từ Lịch sử › Xem thêm: thanh trạng thái 32dp, thanh trên 64dp với `arrow_back` và "Món của tôi" Title Large, không navigation bar. Thân cuộn đệm đáy 96dp để FAB không che hàng cuối: ô tìm (lề 0 / 12dp), bộ sắp xếp 48dp (lề 12 / 20dp), mục "Yêu thích" ghim đầu rồi "N món khác", mỗi mục là tiêu đề Title Medium + lời Label Medium on-surface-var bên phải ("luôn ghim đầu", "dùng nhiều nhất trước") trên một khối danh sách lề 12dp. FAB mở rộng "Thêm món" (`add`) luôn hiện, cách mép phải 16dp; khi có snackbar (lề 12dp), FAB nâng lên trên nó. Tạo và sửa món đã lưu là sheet toàn chiều cao nền surface trên màn che (cách đỉnh 40dp, cùng tay nắm, thanh và chân ghim của S12), không phải sheet phụ neo đáy, vì form có tới bảy ô.

Nước (S15) là màn con mở từ ô Nước của S10 (hoặc nút "Sửa" trên ô Nước của S33): không navigation bar, không FAB. Thanh trên 64dp như S33 (lưới 48dp / 1fr / 48dp / 48dp): quay lại; nút tiêu đề góc 12dp (đệm 2 / 8dp) với "Nước · Hôm nay" Title Large và dòng "Thứ Năm, 13/11" Label Medium on-surface-var + `arrow_drop_down` 18dp, chạm chọn ngày (ngày đã qua: "Nước · 12/11" và "Thứ Tư · Hôm qua"); ‹ ngày trước; › ngày sau, tắt ở hôm nay (on-surface 38%, `aria-disabled`). Khi cuộn, thanh trạng thái và thanh trên đổi sang surface-cont trong 200ms. Thân cuộn lề 12dp, đệm đáy 16dp, các thẻ cách nhau 8dp, theo thứ tự: thẻ tổng ngày, "Các lần uống", tuần này (biểu đồ nước của S35), rồi một dòng Body Small canh giữa "Chạm một lần uống để sửa giờ hoặc lượng; vuốt trái để xoá." Chân thêm nhanh ghim đáy ngoài vùng cuộn, trong vùng ngón cái: nền surface-cont, đệm 12 / 16 / 32dp (gồm vùng thanh cử chỉ), lưới 1.25fr / 1fr / 48dp cách 8dp như hàng nút của S10. Snackbar nổi trên chân (lề 12dp). Thêm lượng khác, sửa lần uống và mục tiêu nước là sheet phụ neo đáy kiểu S31 (đệm 0 / 20 / 28dp; khi bàn phím số mở, đệm đáy 12dp). Màn đầu chứa thẻ tổng ngày và đầu "Các lần uống".

Thống kê (S32) là màn con, mở từ Lịch sử › Xem thêm › Thống kê: thanh trên 64dp chỉ có nút quay lại và "Thống kê" Title Large, không navigation bar, không FAB. Đầu thân cuộn là bộ chọn Tuần / Tháng (viên thuốc 48dp hai phần, đệm đáy 4dp), cuộn đi cùng nội dung; chỉ hàng chọn kỳ ngay dưới được ghim (lưới 48dp / 1fr / 48dp tràn bề ngang thân, đệm 0 / 4 / 8dp). Khi cuộn, thanh trạng thái, thanh trên và hàng chọn kỳ cùng đổi sang surface-cont trong 200ms (`cubic-bezier(.2,0,0,1)`). Thân cuộn lề 12dp, đệm đáy 16dp, các thẻ cách nhau 8dp, theo thứ tự: Calo (số lớn, biểu đồ, chú giải, ghi chú, nhiều nhất / ít nhất); ở chế độ Tuần là hàng sang S35, ở chế độ Tháng là "Các tuần trong tháng"; Macro, Nước, Cân nặng, so với kỳ trước; rồi một dòng Body Small canh giữa "Ngày chưa ghi không tính vào tổng và trung bình. Điểm tuần và nhận xét AI ở Tổng kết tuần." Người mới (dưới 3 ngày ghi) chỉ có thẻ Calo và "Sắp có số". Cả hai biểu đồ tràn hết bề ngang thẻ (lề âm 16dp): tuần là 7 cột 48dp, tháng là N khe đều 336/N dp. Màn đầu chứa bộ chọn, hàng chọn kỳ và thẻ Calo cùng biểu đồ; hàng đọc số của tháng nằm ngay dưới biểu đồ.

Cài đặt (S40) là tab cấp cao: navigation bar với Cài đặt được chọn (`settings` FILL 1). Thanh trên 64dp chỉ có "Cài đặt" Title Large canh giữa, không nút. Thân cuộn lề 12dp, đệm đáy 20dp, các khối cách nhau 8dp; mỗi nhóm mở bằng tiêu đề mục Title Medium (lề trên 12dp, ngang 8dp: "Của bạn", "Ghi món & nhắc", "Dữ liệu", "Giới thiệu") rồi một khối surface-low góc 16dp chứa các hàng 64dp, ngăn bằng đường 1dp outline-var 55%. Không FAB, không hàng nút.

Năm màn con S41–S45 (Hồ sơ & mục tiêu, AI, Nước & nhắc nhở, Bữa ăn, Dữ liệu) theo khung màn con của S31: thanh trên 64dp với `arrow_back` và tiêu đề Title Large, không navigation bar, không FAB; khi cuộn, thanh trạng thái và thanh trên đổi sang surface-cont trong 200ms. Thân cùng lề 12dp và nhịp 8dp: thẻ surface-low (đệm 14 / 16 / 16dp) và khối hàng xen nhau, tiêu đề mục Title Medium, lời dẫn và dòng chú thích Body Small lề 8dp trong cột. Ô nhập trong thẻ cách nhau 20dp (8dp cho ô đầu, cặp ô cạnh nhau cách 12dp, bộ ba cách 8dp). Sheet phụ (Tốc độ, Đang dùng / Mới, TimeInput) là sheet S12 / S31 trên màn che, đệm 0 / 20 / 28dp, hành động 48dp canh phải cách 8dp. Trình soạn system prompt (S42) là màn toàn màn hình: thanh 64dp lưới 48dp / 1fr / auto (`close` bỏ thay đổi / "System prompt" / nút chữ "Lưu"), hàng trạng thái (chip "Đã sửa" trái, "Khôi phục mặc định" phải, đệm 0 / 8 / 8 / 16dp), thân soạn lề 20dp ngăn bằng đường 1dp outline-var 55% phía trên, chân surface-cont dính ngay trên bàn phím. Hộp thoại xoá toàn bộ (S45) canh giữa màn trên màn che, rộng 312dp.

## Elevation & Depth

Hệ thống phẳng, chiều sâu bằng tầng tonal: surface → surface-low (thẻ) → surface-cont (thanh điều hướng, thanh trên khi cuộn) → surface-highest (rãnh). Thẻ, ô và nút không có bóng. Ngoại lệ là FAB và snackbar, vì chúng thực sự nổi trên nội dung đang cuộn; trong Compose dùng độ nổi mặc định của `ExtendedFloatingActionButton` và `Snackbar`. Bottom sheet không có bóng: nó tách khỏi nền bằng góc trên 28dp và màn che (scrim); chân ghim đáy tách khỏi thân bằng tầng surface-cont. Bong bóng chat và thẻ món thu gọn cũng phẳng (tầng tonal; thẻ món có thêm viền 1dp outline-var). Thẻ chọn trong onboarding cũng phẳng: tách khỏi nền bằng surface-low và viền 1dp, trạng thái chọn bằng màu nền và viền, không nổi lên. Thẻ S35 cũng phẳng: thẻ báo cáo là surface-low, thẻ điểm tách ra bằng tông primary-container, không bằng bóng. Lịch S30 cũng phẳng: tuần đang diễn ra tách ra bằng viền trong 1.5dp primary, không bằng bóng hay nền khác. S31 và S33 phẳng như S10: thẻ surface-low, sheet phụ tách bằng góc 28dp và màn che, hàng "dữ liệu đã đổi" của thẻ tuần tách bằng đường ngăn chứ không lồng hộp. Camera (S11) cũng không có bóng: thứ đè lên ảnh tách ra bằng lớp che #000 62%, panel dưới bằng nền #000 đặc và góc 28dp. Vòng căn đĩa có thêm một viền tương phản 1.5dp #000 60% sát bên ngoài nét 2dp on-surface; đó là một nét (không mờ, không lệch) để mép vòng còn đọc được trên đĩa sáng, không phải bóng. S13, S14 và S34 cũng phẳng: hàng món nằm trong khối surface-low góc 16dp, ngăn nhau bằng đường 1dp outline-var 55%; sheet tạo / sửa món đã lưu tách bằng góc 28dp và màn che; chỉ FAB "Thêm món" và snackbar có bóng. S15 cũng phẳng: chân thêm nhanh tách bằng tầng surface-cont, hàng nhắc là một hàng có đường ngăn của thẻ tổng ngày (không lồng hộp), sheet tách bằng góc 28dp và màn che; màn không có FAB, chỉ snackbar có bóng. Hộp thoại xoá toàn bộ (S45) cũng không bóng: surface-low góc 28dp trên màn che, như sheet. Thống kê (S32) cũng phẳng: thẻ surface-low, hàng chọn kỳ ghim tách bằng tầng surface-cont khi cuộn, lớp chọn của biểu đồ tháng là phủ on-surface 8% và hàng đọc số là tầng rãnh vòng, không bóng.

### Shadow Vocabulary
- **FAB nổi** (`box-shadow: 0 1px 3px rgba(0,0,0,.18), 0 4px 10px -2px rgba(0,0,0,.16)`): chỉ cho FAB mở rộng.
- **Snackbar** (`box-shadow: 0 2px 6px rgba(0,0,0,.2)`): chỉ cho snackbar Hoàn tác.

### Named Rules
**The Tonal-Not-Shadow Rule.** Phân tầng bằng surface container. Bóng chỉ dành cho thứ trôi trên nội dung cuộn (FAB, snackbar); không thẻ, không sheet nào có bóng. Trên Camera (S11), lớp che 62% và viền tương phản 1dp của vòng căn đĩa là màu và nét, không phải bóng; không thứ gì trên viewfinder có bóng.

## Shapes

Thang góc M3: small 8dp (chip trạng thái, ô biểu tượng bữa), medium 12dp (nút nhỏ trong ô, vùng chạm tiêu đề ngày), large 16dp (thẻ bữa, ô dữ liệu, FAB, huy hiệu trạng thái trống), extra-large 28dp (góc trên của bottom sheet và sheet phụ), full (nút 48dp, nút Lưu 56dp, icon button, chỉ báo tab, thanh tiến trình 6dp, nút chọn bữa segmented). Ngoài thang: snackbar và ô nhập outlined 4dp (extra-small của M3), ô tông bữa nhỏ 2dp. Viền có bốn việc: 1dp outline-var cho nút outlined và thẻ / hàng chọn chưa chọn; 2dp primary cho thẻ / hàng chọn đang chọn; ô nhập outlined 1dp outline lúc nghỉ, 2dp primary khi focus, 2dp error khi lỗi; nét đứt 1.5dp chỉ cho thứ không có trong sổ: ô biểu tượng của bữa chưa ghi (S10), ô ngày chưa ghi (S35, S30), và phần kcal sắp bỏ khi sửa giảm một món (S14, đoạn rỗng không tô). Viền vẽ vào trong (không làm thẻ to ra khi chọn). Thẻ chọn, hàng chọn, thẻ BMI, lưới chỉ số và thẻ thiết lập đều góc 16dp; ô biểu tượng trong chúng góc 12dp.

Chat: bong bóng góc 20dp với một góc đuôi 6dp về phía người gửi: của bạn 20 / 20 / 6 / 20 (đuôi dưới phải), của AI và thẻ món 20 / 20 / 20 / 6 (đuôi dưới trái). Ô soạn tin góc 24dp, nút gửi và nút đính ảnh tròn 48dp, hàng gợi ý góc 16dp viền 1dp outline-var.

Vòng calo: bán kính 98dp, nét 14dp, đầu đoạn vuông, khe 3dp giữa các đoạn, cung tối thiểu 4dp. Vòng vượt mục tiêu nằm trong, nét 6dp, đầu tròn, trên rãnh cùng màu ở 18%.

Thanh làn "Hôm nay sau khi lưu" là vòng đó duỗi thẳng (dùng nguyên trong thẻ món chat S20): làn cao 10dp trên rãnh vòng (surface-cont sáng / surface-highest tối), đầu đoạn vuông, khe 3dp, toàn chiều dài làn = mục tiêu ngày; vạch mục tiêu 2dp on-surface nhô 4dp trên và dưới làn. Làn vượt riêng cao 6dp, cách 4dp bên dưới, đầu tròn, trên rãnh màu trạng thái 18%. Khi sửa giảm một món (S14), phần bị bỏ là một đoạn rỗng viền nét đứt 1.5dp outline (vẽ vào trong), đứng ngay sau phần giữ lại của món đó và trước các đoạn sau nó.

Thanh chồng tỉ lệ năng lượng (S02) và thang BMI theo cùng ngữ pháp làn: đầu vuông, khe 3dp giữa các đoạn. Thanh năng lượng cao 10dp, ba đoạn đạm / tinh bột / béo theo tỉ lệ kcal. Thang BMI cao 8dp, năm đoạn theo ngưỡng châu Á trên khoảng 15–35 (15–18,5 / 18,5–23 / 23–25 / 25–30 / 30–35); kim chỉ giá trị là vạch 4×18dp góc 2dp màu on-surface, nhô 5dp trên thang; số vạch 18,5 / 23 / 25 / 30 đặt dưới thang, cách 6dp.

Biểu đồ S35 theo cùng ngữ pháp: làn điểm 10dp chia bốn đoạn flex 40 / 20 / 20 / 20, khe 3dp; cột ngày rộng 20dp đầu vuông, khe 3dp tại vạch mục tiêu; thanh macro 6dp đầu vuông; thanh tỉ phần món 4dp đầu vuông; ô vuông 7dp (không tròn) trên sparkline cân; ô chú giải 10dp vuông. Chỉ những thứ không phải dữ liệu mới tròn: chip 8dp, huy hiệu số 24dp tròn, nhãn giá trị che nền góc 4dp, chấm hôm nay 4dp.

Lịch S30 là biểu đồ cột S35 thu nhỏ: cột mini rộng 14dp đầu vuông, đáy cách đáy ô 8dp, thang 30dp cho 155% mục tiêu, khe 3dp tại vạch mục tiêu. Số ngày nằm trong vòng tròn 28dp; chỉ hôm nay có viền trong 1.5dp primary. Thẻ tuần góc 16dp, viền trong 1.5dp primary chỉ cho thẻ tuần này. Ô chưa ghi 14×10dp nét đứt 1.5dp outline, nhãn "—" che nền góc 4dp; ô chú giải 10dp vuông. Ô biểu tượng của hàng "Xem thêm" 40dp góc 12dp.

Biểu đồ xu hướng S31 theo cùng ngữ pháp dữ liệu vuông: đường TB tuần 2dp nối góc tròn (`stroke-linejoin: round`), điểm TB tuần là ô vuông 7dp viền 1.5dp primary (như sparkline S35), từng lần cân là ô vuông 5dp tô đặc; đích dự kiến là ô vuông 7dp viền nét đứt. Làn tiến độ cân 10dp đầu vuông, một đoạn duy nhất, không khe. Bộ chọn khoảng là viên thuốc 48dp chia bốn phần, như nút chọn bữa. Ô "chưa cân tuần này" là ô nét đứt 14×10dp như ô chưa ghi của S30. Trên S33: chip loại ngày góc 8dp với ô 10dp vuông (nét đứt 1.5dp outline cho "Chưa ghi"), hộp nghi ghi thiếu góc 16dp, nút thêm nhanh viên thuốc 48dp.

Camera (S11): vòng căn đĩa là vòng tròn 264dp nét 2dp, không góc ngắm, không lưới. Nút chụp tròn 72dp: vòng 4dp quanh lõi đặc 54dp. Nút trên lớp che tròn 48dp; viên thuốc flash 48dp, viên gợi ý tối thiểu 32dp và thanh thiếu sáng tối thiểu 56dp đều tròn hết cỡ (full). Panel dưới góc trên 28dp như sheet. Khung ảnh góc 16dp; chip gợi ý ghi chú 32dp góc 8dp viền 1dp outline; huy hiệu bước 24dp tròn. Ô Thư viện / Ghi chú 48dp góc 16dp; ô biểu tượng xin quyền 72dp góc 28dp.

Món (S13, S14, S34): ô tìm 56dp tròn hết cỡ (full); khối danh sách món, thẻ không khớp, hộp thông tin và khối trạng thái trống góc 16dp; hàng món trong khối không có góc riêng, ngăn bằng đường 1dp outline-var 55%; bộ lọc / sắp xếp là viên thuốc 48dp chia ba phần (như bộ chọn khoảng S31); ngôi sao, chevron và hàng mở rộng 48dp tròn; nền vuốt xoá vuông theo khối.

Nước (S15): làn nước là thanh làn của S12 không có tông bữa: cao 10dp trên rãnh surface-highest, mỗi lần uống một đoạn primary đầu vuông, khe 3dp giữa các đoạn (không khe tại vạch), đoạn tối thiểu 4dp, toàn chiều dài làn = mục tiêu nước; vạch mục tiêu 2dp on-surface nhô 4dp trên và dưới. Làn trên mục tiêu cao 6dp, cách 4dp bên dưới, đầu tròn 3dp, phần tô primary trên rãnh primary 18%; vạch mục tiêu nối xuống tới nó. Nút mục tiêu ("48% / của 2.600 ml") và hàng nhắc là vùng chạm 48dp góc 12dp; hàng lần uống tràn bề ngang thẻ (lề âm 16dp), không góc riêng, ngăn bằng đường 1dp outline-var 55%; nền vuốt xoá vuông. Chip giờ uống 32dp góc 8dp (vùng chạm 48dp). Ba nút chân ghim tròn hết cỡ 48dp; nút outlined vuông 48dp.

Thống kê (S32): biểu đồ tuần giữ hình của S35 (cột 20dp đầu vuông, khe 3dp tại vạch, ô chưa ghi 20×8dp nét đứt); biểu đồ tháng là cùng ngữ pháp ở cỡ hẹp: cột 6dp đầu vuông giữa khe 336/N dp, khe 3dp tại vạch, phần vượt tối thiểu 3dp, ô chưa ghi 8×8dp nét đứt 1.5dp outline + "—" che nền góc 4dp. Vạch TB của kỳ là đường liền 1dp primary chạy suốt bề ngang biểu đồ, cạnh vạch mục tiêu nét đứt 1dp. Lớp chọn ngày góc 4dp, cao từ đỉnh nhãn ngày tới đường đáy; nhãn ngày cao 18dp góc 4dp. Hàng đọc số tối thiểu 56dp góc 12dp, đệm 4dp. Làn nước TB là làn S15 một đoạn: 10dp đầu vuông, không khe, toàn chiều dài là mục tiêu, vạch 2dp on-surface nhô 4dp ở cuối làn. Hàng tuần 56dp và hàng sang S35 64dp không góc riêng, ngăn bằng đường 1dp outline-var 55%. Bộ chọn Tuần / Tháng là viên thuốc 48dp hai phần như bộ chọn khoảng S31. Chip "Vượt" / "Vượt nhẹ" dưới số lớn cao 32dp góc 8dp.

Cài đặt (S40–S45): khối nhóm hàng và thẻ góc 16dp; hàng trong khối không có góc riêng, ngăn bằng đường 1dp outline-var 55%; ô biểu tượng 40dp góc 12dp như "Xem thêm" của S30. Switch M3 52×32dp tròn hết cỡ: bật là rãnh primary với nút tròn 24dp on-primary bên phải; tắt là rãnh surface-highest viền trong 2dp outline với nút tròn 16dp outline bên trái. Chip giờ 32dp góc 8dp, vùng chạm nới tới 48dp theo chiều dọc. Bộ chọn "Tự động / Tùy chỉnh" và chu kỳ 45 / 60 / 90 / 120 là viên thuốc 48dp viền 1dp outline chia phần bằng đường 1dp outline (như bộ chọn khoảng S31). Hàng tốc độ góc 16dp theo ngữ pháp chọn. Làn 24 giờ của S44 là thanh làn 10dp đầu vuông, khe 3dp, mỗi đoạn dài theo số phút; vạch bây giờ 2×18dp on-surface nhô 4dp trên và dưới làn. Hộp thoại xoá toàn bộ góc 28dp (extra-large); mọi nút trong hộp là viên thuốc 48dp.

## Components

### Buttons
Chắc tay và rõ thứ bậc: một filled, một tonal, một outlined.
- **Shape:** viên thuốc (full), cao 48dp.
- **Filled "Chụp món":** primary / on-primary, biểu tượng camera FILL 1 cỡ 20dp, Label Large. Thao tác chính duy nhất.
- **Tonal "Chat AI":** secondary-container / on-secondary-container, biểu tượng `auto_awesome`.
- **Outlined icon "+":** nền trong, biểu tượng primary, viền 1dp outline-var, vuông 48dp.
- **Icon button:** 48dp tròn, biểu tượng on-surface-var (primary trong đầu thẻ bữa); hover phủ on-surface 8%.
- **Focus:** vòng 2dp primary, cách 2dp, cho mọi phần tử tương tác.
- **Outlined có chữ** ("Thêm món", "Từ Món của tôi"): viên thuốc 48dp, viền 1dp outline-var, chữ và biểu tượng primary; hai nút chia đều một hàng, cách 8dp.
- **Nút bước (onboarding):** viên thuốc 56dp toàn chiều rộng, primary / on-primary, chữ giữa Title Medium ("Tiếp", "Xem kết quả", "Bắt đầu", "Xong"), ghim ở chân. Tắt khi dữ liệu bước chưa hợp lệ: nền on-surface 12%, chữ on-surface 50%, `aria-disabled`. Trong lúc gõ, nút chỉ tắt, không kèm lỗi. Nhập tay (S13) dùng lại nút này: "Tiếp" + `arrow_forward` 20dp sau chữ, mở S12 với món vừa nhập; đó là một bước, không phải lưu.
- **Nút chữ "Để sau":** 48dp, chữ primary Label Large, không nền, ngay dưới nút bước.
- **Tonal "Kiểm tra" (S03):** viên thuốc 48dp secondary-container, biểu tượng `network_check` 20dp (`check` sau khi thành công); tắt như nút bước khi ô key còn trống.
- **Liên kết hành động** ("Khôi phục từ bản sao lưu", "Cách tính"): chữ primary Label Large với biểu tượng, vùng chạm 48dp.
- **Bookmark toggle:** icon button 48dp; `bookmark_add` (on-surface-var, FILL 0) khi chưa lưu, `bookmark_added` (primary, FILL 1) khi đã lưu vào Món của tôi. `menu_book` chỉ dùng cho lối thêm món từ Món của tôi; hai biểu tượng không đổi vai cho nhau.
- **Cặp lối thoát "Hỏi AI" / "Nhập tay" (S13):** tonal `auto_awesome` 20dp trước, outlined `edit_note` 20dp sau (viền 1dp outline-var, chữ primary), 48dp, chia đều, cách 8dp. Ghim ở chân sheet; khi tìm không khớp, chân ẩn và cặp này vào thẻ không khớp ngay dưới ô tìm; cả hai mở với chuỗi tìm điền sẵn (Hỏi AI sang S20, Nhập tay sang form).
- **Ngôi sao yêu thích (S34):** icon button 48dp `star`; tắt FILL 0 on-surface-var, bật FILL 1 primary, `aria-pressed`. Trong form món đã lưu là hàng 48dp ngôi sao + "Yêu thích" Body Medium với dòng phụ Body Small "Ghim lên đầu Món của tôi và Thêm nhanh"; cả hàng là vùng chạm. Ngôi sao là yêu thích, dấu trang là lưu: không đổi vai cho nhau.
- **"Lưu vào Món của tôi" (S34):** nút Lưu 56dp của S12 với `bookmark_add` 20dp + "Lưu vào Món của tôi" bên trái, kcal đậm 650 bên phải; chỉ tạo món đã lưu, không ghi nhật ký.

### Chips
- **Chip trạng thái:** cao tối thiểu 32dp, góc 8dp, nền `*-container` và chữ `on-*-container` của trạng thái, biểu tượng 18dp. Chỉ xuất hiện khi vượt mục tiêu.
- **Chip giờ ăn · bữa:** cao 32dp (vùng chạm mở rộng 8dp trên và dưới thành 48dp), góc 8dp, secondary-container; `schedule` 18dp, giờ, ô tông bữa 10dp, tên bữa, `arrow_drop_down`. Chạm mở sheet giờ ăn & bữa. Nguồn (ví dụ "Theo tin nhắn …") ghi bằng chữ Body Small on-surface-var cạnh chip, không thành chip.
- **Cờ kiểm tra:** cao 24dp, góc 8dp, warn-container / on-warn-container, Label Medium, biểu tượng 16dp, đứng cạnh tên món. Ba loại: "Cần kiểm tra" (`help`), "Số liệu lệch" và "Có vẻ cao" (`warning`). Theo The Amber Check Rule.

### Cards / Containers
- **Thẻ bữa:** góc 16dp, nền surface-low, không bóng, không viền. Đầu thẻ cao 52dp: ô biểu tượng 24dp màu tông bữa, tên bữa và số món, tổng kcal, nút thêm. Mỗi món một hàng tối thiểu 52dp: giờ ăn (cột 44dp), tên món với biểu tượng nguồn và khẩu phần, kcal; ngăn nhau bằng đường 1dp outline-var 55%. Bữa chưa ghi: chữ on-surface-var, ô biểu tượng nét đứt.
- **Ô dữ liệu (Nước, Cân nặng):** góc 16dp, nền surface-low, đệm 14dp, nút nhỏ 48dp góc 12dp.
- **Trạng thái trống:** khối surface-low góc 16dp, huy hiệu camera 48dp nền primary-container, một câu mời chụp bữa đầu.
- **Thẻ món (xác nhận):** mỗi món một thẻ riêng, góc 16dp, nền surface-low, đệm 12 / 4 / 2 / 16dp, không viền. Hàng 1: tên món Title Medium + cờ, kcal Title Medium canh phải với đơn vị Mực Phụ. Hàng 2: khẩu phần và ba macro với chấm 6dp (Body Small, on-surface-var). Ghi chú "AI giả định: …" và cảnh báo là dòng chữ Body Small với biểu tượng 16dp (warn khi cần kiểm tra, primary khi chỉ là thông tin). Hàng cuối: bộ chọn khẩu phần bên trái, bookmark toggle bên phải. Món cần kiểm tra luôn xếp lên đầu danh sách.
- **Thẻ câu hỏi làm rõ:** góc 16dp, nền warn-container, câu hỏi Body Medium với `help`, ô trả lời 48dp góc 12dp (viền on-warn-container 35%, nền surface 70%) và nút gửi tròn 48dp secondary-container, dưới cùng một dòng "bỏ qua và lưu luôn".
- **Hộp hổ phách (điều chỉnh):** góc 16dp, nền warn-container, chữ on-warn-container Body Small, biểu tượng `info` 20dp, đệm 12 / 14dp, cách 10dp. Theo The Amber Adjust Rule.
- **Thẻ BMI:** góc 16dp, nền surface-low, đệm 14 / 16dp; dòng trên "BMI 26,0" Title Medium bên trái, "Béo phì độ I · ngưỡng châu Á" Label Medium on-surface-var bên phải; dưới là thang BMI. Tính lại ngay khi đủ chiều cao và cân nặng; khi chưa đủ, thay bằng một dòng Body Small "BMI sẽ hiện khi đủ chiều cao và cân nặng."
- **Thẻ thiết lập (S03):** góc 16dp, nền surface-low, đệm 16dp; đầu thẻ là ô biểu tượng 40dp góc 12dp primary-container / on-primary-container + tiêu đề Title Medium; mô tả Body Small on-surface-var. Công tắc là Switch M3 52×32dp, bật = primary với núm on-primary.
- **Nhận xét AI một dòng:** `auto_awesome` 20dp primary + Body Small on-surface-var, một dòng, không nền.

### Bottom sheet xác nhận
- Sheet toàn màn hình nền surface, góc trên 28dp, tay nắm 32×4dp outline-var, thanh 52dp: `close` (huỷ toàn bộ), tiêu đề "Xác nhận món" Title Medium ở giữa, `more_vert`.
- **Tóm tắt:** thumbnail 64dp góc 16dp (ảnh món; khi ghi từ chat là ô surface-highest với biểu tượng `chat`), tổng kcal Display Small + "kcal" on-surface-var, dưới là ba macro inline với chấm 8dp, Label Medium, giá trị đậm on-surface.
- **Nút Lưu (ghim đáy):** viên thuốc 56dp toàn chiều rộng, primary / on-primary, `check` 20dp + "Lưu vào bữa X" bên trái, tổng "N kcal" đậm 650 bên phải. Luôn nêu tên bữa và tổng kcal. Khi không có món, chân và nút Lưu ẩn; trạng thái trống đưa hai lối thoát (tonal "Chụp lại", outlined "Nhập tay").

### Thẻ chọn (selection grammar)
Ngữ pháp chọn duy nhất cho mọi câu hỏi một lựa chọn trong onboarding (`role="radiogroup"`).
- **Thẻ đứng cặp** (giới tính): hai cột đều, cách 10dp, góc 16dp, đệm 16dp, ô biểu tượng 44dp góc 12dp trên nhãn Title Medium.
- **Hàng chọn** (vận động, mục tiêu, tốc độ): tối thiểu 64dp (76dp khi có dòng phụ dài, 60dp cho tốc độ), góc 16dp, đệm 10 / 14dp, lưới ô biểu tượng 40dp / chữ / giá trị cuối. Nhãn Title Medium với dòng phụ Body Small on-surface-var; giá trị cuối (hệ số "×1,375", "1.740 kcal" + thời gian) Label Medium canh phải.
- **Chưa chọn:** nền surface-low, viền trong 1dp outline-var, ô biểu tượng surface-highest / on-surface-var.
- **Đang chọn:** nền secondary-container, viền trong 2dp primary, chữ và dòng phụ on-secondary-container, ô biểu tượng chuyển primary / on-primary.
- Không dấu tick, không radio tròn: nền, viền và ô biểu tượng đã nói đủ. Chọn không bao giờ tự sang bước; luôn bấm nút bước, để một chạm nhầm còn sửa được.

### Ô nhập
- **OutlinedTextField M3:** cao 56dp, góc 4dp, viền 1dp outline lúc nghỉ. Nhãn nằm trong khe viền (Label Medium, nền màu của mặt phía sau), giá trị Title Large, hậu tố đơn vị ("cm", "kg") Body Medium on-surface-var canh phải. Focus: viền 2dp primary, nhãn primary, con trỏ 2dp primary. Lỗi: viền 2dp error, nhãn error, dòng hỗ trợ error với `error` 18dp (ví dụ "Nhập cân nặng từ 30 đến 250 kg"). Dòng hỗ trợ thường (Body Small on-surface-var, lề 16dp) nêu hệ quả của giá trị, ví dụ "BMI mục tiêu 23,5 · Thừa cân".
- **Thời điểm báo lỗi:** lỗi khoảng chỉ hiện khi rời ô hoặc bấm Xong trên bàn phím; trong lúc gõ, chỉ nút bước tắt. Số dùng bàn phím số của hệ thống.
- **Biến thể có đuôi (API key):** hai icon button (`visibility`, `content_paste`) ở cuối ô; giá trị che bằng dấu chấm, giữ bốn ký tự cuối.
- **Bánh xe năm sinh:** khối surface-low góc 16dp cao 144dp, ba dòng 48dp; dải chọn 48dp góc 12dp secondary-container cách lề 12dp; dòng chọn Title Large 650 on-secondary-container kèm tuổi ("1996 · 30 tuổi"), dòng kề on-surface-var 16sp.

### Tiến độ bước
- Thanh 6dp toàn chiều rộng cột giữa của thanh trên, rãnh surface-highest, phần đã đi primary, đầu tròn; dưới là "Bước n/5" Label Medium on-surface-var canh giữa.
- Chuyển bước dùng shared-axis X của M3: tiến thì nội dung trượt sang trái, lùi thì sang phải; Back hệ thống về bước trước. Bước bị bỏ qua (Giữ cân bỏ bước 5) được nói rõ ngay trên hàng chọn.

### Kết quả mục tiêu (S02)
- Từ trên xuống: "Mục tiêu mỗi ngày của bạn" Title Medium canh giữa; vòng calo trống với mục tiêu ở tâm; dòng dự kiến (`flag` 20dp primary + "Đạt 68 kg khoảng 19/02/2027 · ~14 tuần" Body Medium); hộp hổ phách nếu đã điều chỉnh; thanh chồng năng lượng; lưới chỉ số; "Cách tính"; miễn trừ y tế Body Small on-surface-var canh giữa, đẩy xuống cuối.
- **Thanh chồng năng lượng:** ba đoạn đạm / tinh bột / béo (xem Shapes); chú giải ba cột đều cách 8dp, mỗi cột chấm 8dp + tên Label Medium, giá trị Title Medium "122 g · 28%" với phần đơn vị 12sp on-surface-var.
- **Lưới chỉ số:** khối surface-low góc 16dp, đệm 12 / 4dp, bốn cột đều BMI / BMR / TDEE / Nước, ngăn bằng đường 1dp outline-var 60%; mỗi cột nhãn Label Medium, giá trị Title Medium, đơn vị / phân loại Label Medium on-surface-var.
- **Cách tính:** hàng 48dp chữ primary Label Large với `expand_more` / `expand_less`; mở ra là các đoạn Body Small, số liệu đậm on-surface. Mở rộng không đẩy thanh năng lượng hay lưới ra khỏi luồng: nội dung cuộn, chân ghim giữ nguyên.

### Bộ chọn khẩu phần
- Năm ô 48×48dp liền nhau: ½, ×1, 1½, 2 và `edit` (nhập gram), Label Large on-surface-var. Chỉ ô đang chọn có viên thuốc 40×32dp secondary-container / on-secondary-container; các ô khác không nền, không viền, không đường ngăn. Đổi hệ số tính lại trên máy.

### Vuốt xoá và snackbar
- Vuốt trái thẻ món lộ nền error-container (112dp) với `delete` 20dp + "Xoá" Label Large on-error-container; thẻ trượt sang trái và mất góc phải. Thả quá ngưỡng là xoá, không có hộp thoại xác nhận.
- Snackbar ngay trên chân sheet, lề 12dp, cao tối thiểu 48dp, góc 4dp, inverse-surface / inverse-on-surface, "Đã xoá “…”" Body Medium, nút "Hoàn tác" Label Large inverse-primary.

### Sheet giờ ăn & bữa
- Sheet phụ nền surface-low, góc trên 28dp, trên màn che; tiêu đề "Giờ ăn & bữa" Title Large.
- **TimeInput M3 nội tuyến:** hai ô 96×72dp góc 8dp, số Display Medium, nhãn "Giờ" / "Phút" Label Medium bên dưới, dấu hai chấm Display Medium giữa. Ô đang nhập: primary-container / on-primary-container với viền trong 2dp primary; ô kia surface-highest. Nguồn giờ ghi bằng chữ cạnh bên ("Lúc bấm chụp").
- **Chọn bữa segmented:** bốn phần đều (Sáng, Trưa, Phụ, Tối), cao 48dp, viên thuốc viền 1dp outline, đường ngăn 1dp outline; mỗi phần có ô tông bữa 10dp + tên; phần đang chọn secondary-container. Dưới là gợi ý `sync` khi bữa đang tự gán theo giờ.
- Hành động: nút chữ "Huỷ" (primary) và filled "Xong", 48dp, canh phải.

### Navigation
- **Top app bar:** ‹ / tiêu đề "Hôm nay" + ngày có mũi tên thả / ›; chạm tiêu đề mở lịch.
- **Navigation bar:** nền surface-cont, 4 tab (Hôm nay, Chat AI, Lịch sử, Cài đặt). Tab đang chọn: chỉ báo 64×32dp secondary-container, biểu tượng FILL 1, nhãn on-surface đậm 700; tab khác on-surface-var.

### FAB mở rộng
- primary-container / on-primary-container, cao 56dp, góc 16dp, biểu tượng camera + "Chụp món". Ẩn khi hàng nút còn thấy; hiện bằng mờ dần và trượt lên 12dp (200 đến 250ms, `cubic-bezier(.2,0,0,1)`).

### Macro
- Tên macro với chấm màu 8dp, giá trị Title Medium kèm "/ mục tiêu g", thanh 6dp đầu vuông trên rãnh surface-highest, thang tới 125% mục tiêu với vạch mục tiêu 2dp on-surface ở 80% (nhô 3dp trên và dưới), dòng "còn X g" / "vượt X g". Khi vượt: giá trị và dòng dưới chuyển sang error; thanh giữ màu dữ liệu tới vạch, khe 3dp, rồi phần vượt error (The Goal-Line Split Rule, giống S35).

### Thẻ tổng kết tuần trước (S10, Thứ Hai và Thứ Ba)
- Một nút toàn thẻ dưới hàng nút, trên mục "Bữa ăn" (không đẩy vòng calo): nền surface-low góc 16dp, lề 12dp, cao tối thiểu 72dp, lưới 48dp / 1fr / 48dp. Ô điểm 48dp góc 12dp primary-container / on-primary-container, số 22sp 650 wdth 92; tiêu đề "Tổng kết tuần dd/mm – dd/mm" Title Medium, dòng phụ Body Small on-surface-var "Xếp loại · vượt/thiếu N kcal · n/7 ngày đạt"; `chevron_right` on-surface-var. Chạm mở S35. Chỉ hiện Thứ Hai và Thứ Ba (08 §1).

### Vòng calo (chữ ký)
- 220dp ở giữa, tâm là nhãn "Còn lại", số Display Large, đơn vị "kcal". Món xếp theo giờ ăn; các món liên tiếp cùng bữa gộp thành một đoạn, nên một bữa có thể xuất hiện hai lần (ví dụ Phụ chiều và Phụ tối). Phần dư khi vượt chạy vòng trong màu trạng thái. Trên S10, chuyển động duy nhất được dàn dựng: các đoạn vẽ lần lượt theo thứ tự giờ ăn (700ms, `cubic-bezier(.2,0,0,1)`, trễ 120ms + 140ms mỗi đoạn); tắt khi người dùng giảm chuyển động.
- **Tâm vòng của ngày đã qua (S33):** nhãn Label Large, Δ có dấu Display Large, đơn vị "kcal" Label Medium on-surface-var; nhãn là loại ngày của 08 §2. "Thiếu mục tiêu −320" và "Đạt mục tiêu −50": nhãn on-surface-var, số on-surface. "Vượt mục tiêu +620" (>10%): nhãn và số màu error, phần dư chạy vòng trong như S10. "Đạt · vượt nhẹ +10" (vượt ≤10%, vẫn tính Đạt): nhãn và số màu warn, vòng trong ở cung tối thiểu. Ngày chưa ghi: vòng trống của S02, tâm "Mục tiêu ngày" + mục tiêu kcal. Đoạn vòng vẫn vẽ lần lượt theo giờ ăn khi mở màn, như S10.
- **Trạng thái trống / mục tiêu (S02):** cùng vòng 220dp, bán kính 98dp, nét 14dp, chỉ có rãnh (surface-cont sáng / surface-highest tối), không đoạn, không vạch chia. Tâm là mục tiêu kcal/ngày Display Large và "kcal mỗi ngày" Label Large on-surface-var. Chuyển động duy nhất: số đến một lần khi mở màn (600ms, `cubic-bezier(.2,0,0,1)`, trễ 100ms, mờ dần lên + trượt 8dp + scale .96 về 1); tắt khi giảm chuyển động.

### Thanh làn "Hôm nay sau khi lưu"
- Vòng calo duỗi thẳng, nằm trong thẻ surface-low góc 16dp, lề 12dp, đệm 12 / 16 / 14dp. Dòng trên: nhãn "Hôm nay sau khi lưu" (ngày khác hôm nay: "dd/mm sau khi lưu", ví dụ "12/11 sau khi lưu") Label Medium on-surface-var bên trái; bên phải "còn N kcal" Label Large on-surface, hoặc khi vượt là biểu tượng (`info` / `warning`) + "Vượt nhẹ X% · +N kcal" / "Vượt X% · +N kcal" màu trạng thái.
- Các đoạn đã ăn xếp theo giờ, mỗi đoạn tông meal-* của bữa. Đoạn sắp thêm (pending) cùng tông bữa được lưu: nền tông 40% pha vào rãnh, viền trong 1.5dp đúng tông; mọc từ trái (700ms, `cubic-bezier(.2,0,0,1)`, trễ 150ms), tắt khi giảm chuyển động. Đoạn mới dừng ở vạch mục tiêu; phần vượt không bao giờ nằm trên làn chính mà chạy trên làn vượt riêng 6dp màu trạng thái (warn ≤10%, error >10%), như vòng trong của S10.
- **Ngày đã qua (từ S33):** đoạn mới chèn đúng chỗ theo giờ ăn trong các đoạn đã có (bữa Sáng 07:30 đứng đầu làn), không nối vào cuối. Khi lưu làm ngày đổi loại, cuối thẻ có một dòng kết quả ngăn bằng đường 1dp outline-var 55%: `event_available` 18dp primary + Body Small "Lưu xong, ngày 12/11 chuyển từ **Thiếu** sang **Đạt** (1.750 kcal, trong ±10% mục tiêu)."; ngày không đổi loại thì không có dòng.
- **Sửa món (S14):** các món khác và phần cũ của món đang sửa giữ đoạn đặc như cũ. Tăng: phần chênh là đoạn chờ (40% có viền 1.5dp) nối ngay sau đoạn cũ của món. Tăng quá mục tiêu: đoạn chờ dừng ở vạch, phần dư chạy làn vượt 6dp (warn ≤10% với `info`, error >10% với `warning`), đầu thẻ "Vượt nhẹ 3% · +60 kcal", không bao giờ "còn −60". Giảm: phần bị bỏ là đoạn rỗng nét đứt 1.5dp outline ngay sau phần giữ lại, "còn N kcal" tăng theo. Dưới làn, cách 10dp, một dòng Label Medium on-surface-var hai đầu: "Trước khi sửa **1.310** kcal" / "Món này **+245** kcal" (giảm: "**−245**", dấu trừ thật), số đậm 600 on-surface.
- **Sửa món của ngày đã qua (S14 từ S33):** chip mang ngày ("19:00 · Tối · 12/11"), nhãn làn "12/11 sau khi lưu", và dòng kết quả `event_available` dựng theo loại ngày sau khi lưu (chỉ khi loại ngày đổi).

### Luồng chat (S20)
Tin nhắn là bong bóng, kết quả của AI cũng là bong bóng: không form, không trang kết quả.
- **Bong bóng của bạn:** secondary-container / on-secondary-container, Body Medium, đệm 10 / 14dp, góc 20 / 20 / 6 / 20, căn phải. Ảnh đính kèm là khối 200×150dp cùng góc đuôi, phía trên bong bóng chữ.
- **Bong bóng AI:** surface-cont, góc 20 / 20 / 20 / 6, căn trái, đoạn cách 8dp, số đậm 650. Giờ tin nhắn Label Small on-surface-var dưới bong bóng, đệm ngang 6dp, tin AI kèm `auto_awesome` 16dp đứng trước giờ. Nhãn ngày canh giữa Label Small on-surface-var.
- **Bong bóng đang gõ:** surface-cont, ba chấm 7dp on-surface-var nhảy lệch pha 150ms (chu kỳ 1,2s, mờ 0.3 đến 1, nâng 3dp); giảm chuyển động thì đứng yên ở độ mờ 0.6. Đây là chuyển động duy nhất của chat, vì đặc tả S20 đòi bong bóng đang gõ.
- **Bong bóng lỗi:** error-container / on-error-container, góc như bong bóng AI, `cloud_off` 20dp + chữ nêu rõ chuyện gì xảy ra, nút "Thử lại" viền 1dp on-error-container 45% cao 48dp. Tin nhắn chưa gửi của bạn mang đánh dấu "Chưa gửi ·" kèm `error` 16dp màu error thay chỗ giờ.
- **Trạng thái trống:** huy hiệu 48dp góc 16dp primary-container, một câu mời, và ba hàng gợi ý (≥48dp, góc 16dp, nền surface-low, viền 1dp outline-var, biểu tượng primary).
- **Ô soạn tin:** `add_photo_alternate` icon button 48dp, ô chữ 48dp góc 24dp nền surface-cont (chữ mồi on-surface-var, con trỏ 2dp primary), nút gửi 48dp tròn primary / on-primary. Tắt khi ô trống: on-surface 12% / 50%, như nút bước. Ảnh chờ gửi hiện trên ô soạn tin trong thẻ surface-low góc 16dp, thumbnail 44dp góc 12dp.
- **Sheet đính ảnh:** sheet phụ surface-low góc trên 28dp trên màn che; hai hàng 56dp (camera, thư viện), ô biểu tượng 40dp góc 12dp primary-container; mỗi hàng nói rõ giờ ăn (eaten_at) lấy từ đâu, theo nguyên tắc nguồn giờ ghi bằng chữ.
- **Thanh trên:** "còn N kcal" luôn thấy ở dòng phụ, màu primary Label Medium 600; chạm tiêu đề mở lịch như S10.

### Thẻ món thu gọn trong chat
Vừa đủ để quyết định, rồi chuyển sang sheet xác nhận (S12).
- **Khung:** rộng cố định 304dp ở cả ba trạng thái (chờ, đã lưu, bỏ qua) để đổi trạng thái tại chỗ, không nhảy bố cục. Nền surface-low, góc 20 / 20 / 20 / 6, viền trong 1dp outline-var (trong suốt khi đã lưu), đệm 14 / 14 / 6dp, không bóng.
- **Đầu thẻ:** chip 28dp góc 8dp secondary-container với ô tông bữa 10dp góc 2dp + "Bữa · giờ" (ghi cho hôm qua: "Bữa · Hôm qua · giờ"), tổng kcal Title Medium canh phải với đơn vị Mực Phụ.
- **Thân:** danh sách món Body Medium (khẩu phần Label Medium on-surface-var cạnh tên, kcal 600), hàng macro với chấm 6dp ba màu dữ liệu, cách nhau 10dp. Cờ cần kiểm tra theo The Amber Check Rule.
- **Làn "Hôm nay / Ngày này sau khi lưu":** ngữ pháp làn S12 ở 10dp, khe 3dp, đoạn mới 40% có viền 1.5dp, vạch mục tiêu 2dp nhô 3dp; dòng trên "còn N kcal" Label Large on-surface.
- **Hành động (chờ):** "Xem & lưu" tonal 48dp và "Bỏ qua" nút chữ 48dp, cách 4dp, lề âm 6dp để vùng chạm thẳng với lề thẻ.
- **Đã lưu:** dòng trạng thái `check_circle` primary + "Đã lưu vào Bữa" và nút chữ "Xem lại" 48dp. **Đã bỏ qua:** biểu tượng outline + "Đã bỏ qua" và "Hoàn tác" 48dp; món và kcal đổi sang on-surface-var, không dùng opacity.

### Tổng kết tuần (S35)
Bảy ngày của vòng calo xếp cạnh nhau; điểm tính trên máy, AI chỉ viết lời.
- **Hàng chọn tuần:** ‹ / giữa / ›, mỗi nút 48dp. Giữa là "Tuần 03/11 – 09/11" (tuần đang diễn ra: "Tuần này · 10/11 – 16/11") Title Medium, dưới cách 2dp là dòng trạng thái: tuần đã xong "Đã kết thúc · ghi n/7 ngày" Label Medium on-surface-var với `event_available` 16dp; tuần đang diễn ra là chip 24dp góc 8dp secondary-container `hourglass_top` + "Tạm tính · còn N ngày". Nút › tắt ở tuần hiện tại (on-surface 38%, `aria-disabled`).
- **Thẻ điểm:** primary-container / on-primary-container, góc 16dp, đệm 14 / 16 / 16dp. Hàng trên canh đáy: điểm Display Large, "/100 điểm" Title Medium (on-primary-container 72%), bên phải xếp loại Title Large ("Xuất sắc" ≥85, "Tốt" ≥70, "Cần cố gắng" ≥50, "Chưa tốt") với dòng so sánh mũi tên `arrow_upward` / `arrow_downward` 16dp + "N so với tuần trước" Label Medium (80%).
- **Làn điểm:** cách 16dp dưới, cao 10dp, bốn đoạn flex 40 / 20 / 20 / 20 (Calo, Đạm, Nước, Ghi chép) cách nhau 3dp; phần đã đạt primary trên rãnh primary 16% pha vào primary-container, đầu vuông. Dưới mỗi đoạn cách 8dp: tên phần Label Medium (76%) và "28/40" Label Large. Chuyển động duy nhất được dàn dựng của S35: các đoạn đầy lần lượt từ trái (scaleX, 700ms `cubic-bezier(.2,0,0,1)`, trễ 120ms + 140ms mỗi đoạn), như vòng calo của S10; tắt khi giảm chuyển động.
- **Thẻ báo cáo:** surface-low góc 16dp, đệm 14 / 16 / 16dp; đầu thẻ là tiêu đề Title Medium + so sánh với tuần trước (mũi tên 16dp + Label Medium on-surface-var) bên phải. Câu chính Title Large 500 với số 650, kèm chip trạng thái của tuần khi vượt (Three States); dòng phụ Body Small. Các dòng diễn giải cuối thẻ ngăn bằng đường 1dp outline-var 55%, mỗi dòng biểu tượng 18dp primary + Body Small, số đậm on-surface. Ở tuần đang diễn ra, mọi so sánh với tuần trước là so với cùng các thứ đã qua của tuần trước, không với cả tuần (The Same-Days Rule), và dòng dưới chú giải biểu đồ ghi "Cùng kỳ tuần trước đạt N ngày" thay cho "Tuần trước đạt N ngày".
- **Biểu đồ cột ngày:** tràn hết bề ngang thẻ, 7 cột 48dp, mỗi cột là một nút chạm mở chi tiết ngày (S33) với nhãn đọc màn hình đầy đủ. Cột 20dp vuông, cao 120dp (Nước 96dp). Vạch mục tiêu nét đứt 1dp on-surface-var; dải ±10% on-surface 6% (chỉ calo). Đạt = primary; Thiếu = Chàm Thiếu (`--under`); Vượt = primary đến vạch + khe 3dp + phần vượt error (tối thiểu 4dp). Hôm nay ở ngữ pháp đoạn chờ (primary 40% pha vào rãnh, viền trong 1.5dp primary). Ngày chưa ghi: ô 20×8dp nét đứt 1.5dp outline + "—". Ngày tương lai: không cột, nhãn ngày on-surface 38%. Nhãn giá trị Label Small on-surface-var trên đỉnh cột, che nền surface-low góc 4dp; cột vượt nhãn error 650; ngày nghi ghi thiếu (dưới 50% mục tiêu) có `warning` 16dp và nhãn warn. Dưới là hàng nhãn ngày trên đường 1dp outline-var; hôm nay đậm 700 on-surface với chấm 4dp primary.
- **Chú giải biểu đồ:** ô vuông 10dp + tên + số đếm đậm ("Đạt **4**", "Vượt **2**", "Thiếu **1**", "Chưa ghi **0**"), nên chú giải cũng là bảng đếm; ô "Chưa ghi" nét đứt, thêm "(gồm hôm nay)" ở loại chứa hôm nay, và mẫu vạch nét đứt "Mục tiêu 1.740 ± 10%". Ngày chưa ghi và nghi ghi thiếu có dòng giải thích Body Small riêng (`info` on-surface-var / `warning` warn).
- **Thanh macro (S35):** tên với chấm 8dp, giá trị Title Medium, "/ mục tiêu g · %" Label Medium. Thanh 6dp trên rãnh surface-highest, thang đến 125% mục tiêu, vạch mục tiêu 2dp on-surface ở 80% nhô 3dp. Vượt: phần trong mục tiêu màu dữ liệu + khe 3dp + phần vượt error; giá trị và dòng "vượt X g mỗi ngày" (`warning`) chuyển error. Dưới là hai thanh chồng tỉ lệ năng lượng 10dp "Thực tế" / "Mục tiêu" (lưới 64dp / 1fr / 92dp, phần trăm canh phải).
- **Thẻ cân nặng:** cân TB tuần Title Large + "kg TB tuần" Label Medium, thay đổi so với tuần trước Body Medium, dự kiến theo lượng ăn so với thực tế Body Small. Bên phải sparkline 4 tuần 132×56dp: đường 2dp primary, ô vuông 7dp viền 1.5dp primary (tuần hiện tại tô đặc); nhãn tuần là chữ HTML Label Small, tuần hiện tại 700. Không có lần cân: ô biểu tượng 40dp góc 12dp surface-highest + một câu Body Small + nút outlined toàn chiều rộng "Ghi cân nặng".
- **Món nhiều calo nhất:** danh sách đánh số 5 món; hàng là thứ hạng Label Medium, tên Body Medium + "×n" Label Medium, kcal 600; dưới là thanh tỉ phần 4dp primary trên rãnh surface-highest (so với món đầu); ngăn bằng đường 1dp outline-var 55%.
- **Thẻ nhận xét AI:** đầu thẻ `auto_awesome` 20dp primary + "Nhận xét của AI" Title Medium + thời điểm tạo Label Small. Câu đầu Title Medium, rồi các mục Làm tốt (`check_circle`), Cần cải thiện (`flag`), Quy luật (`insights`) với tiêu đề Label Large + biểu tượng 20dp primary và danh sách chấm 4dp Body Medium; "3 việc cho tuần sau" (`lightbulb`) là danh sách có huy hiệu số 24dp tròn secondary-container, ngăn bằng đường 1dp outline-var 55%. Các mục phân bằng khoảng trắng và đường ngăn, không lồng hộp. Cuối là miễn trừ Body Small on-surface-var.
- **Trạng thái thẻ AI:** đang tạo = bong bóng đang gõ của chat + một dòng nói các phần trên đã đủ; lỗi = hộp error-container góc 16dp với `cloud_off` 20dp, chữ nêu số liệu vẫn đủ, nút outlined "Thử lại" 48dp; dữ liệu đã đổi = dải secondary-container góc 12dp (`sync` + câu + "Tạo lại") và nhận xét cũ vẫn đọc được ở on-surface-var; chưa tạo (tuần đang diễn ra) = một câu + nút tonal "Tạo nhận xét tạm tính"; không có API key = ẩn hẳn thẻ.

### Thống kê (S32)
Một kỳ (Tuần hoặc Tháng) là các ngày của vòng calo xếp cạnh nhau, đọc bằng trung bình: số trung tính, không điểm, không xếp loại, không chữ AI, không top món (những thứ đó ở S35).
- **Bộ chọn Tuần / Tháng:** viên thuốc 48dp hai phần (`role="radiogroup"`), viền và đường ngăn 1dp outline, chữ 14sp 600 on-surface; phần đang chọn secondary-container. Cuộn đi cùng thân, không ghim.
- **Hàng chọn kỳ (ghim):** ngữ pháp hàng chọn tuần của S35: ‹ / giữa / › 48dp. Giữa là "Tuần này · 10/11 – 16/11", "Tuần 03/11 – 09/11" hoặc "Tháng 11, 2026" Title Medium; dưới cách 2dp: kỳ đang diễn ra là chip 24dp góc 8dp secondary-container `hourglass_top` 16dp "Tạm tính · còn N ngày", kỳ đã xong là `event_available` 16dp + "Đã kết thúc · ghi n/N ngày" Label Medium on-surface-var. › tắt ở kỳ hiện tại (on-surface 38%, `aria-disabled`).
- **Thẻ Calo:** đầu thẻ "Calo · TB mỗi ngày" Title Medium + "**n**/N ngày ghi" Label Medium. Hàng số canh đáy: TB kcal/ngày Display Large + "kcal" Title Medium on-surface-var; bên phải Δ so với mục tiêu Title Medium (dấu trừ thật; màu theo The Three States Rule) trên "so với 1.740" Label Medium on-surface-var. Dòng Body Small dưới: khi vượt, chip 32dp góc 8dp ("Vượt nhẹ X%" `info` warn-container, "Vượt X%" `warning` error-container) đứng trước "Tổng N kcal / n ngày" (+ " · gồm hôm nay (đang ghi)"). Số lớn chỉ hiện khi có ≥3 ngày ghi. Sau biểu đồ và chú giải: một dòng Body Small cho từng ngày chưa ghi (`info` 16dp on-surface-var) và từng ngày nghi ghi thiếu (`warning` 16dp warn); rồi sau đường ngăn, `swap_vert` 18dp primary + "Nhiều nhất **dd/mm N** · ít nhất **dd/mm N** kcal", không tính hôm nay khi kỳ đang diễn ra.
- **Biểu đồ tuần:** biểu đồ cột ngày của S35 (7 cột 48dp là nút sang S33, cột 20dp, cao 120dp, vạch, dải ±10%, nhãn giá trị, cờ nghi ghi thiếu, nhãn ngày), trừ cột hôm nay: chỉ viền trong 1.5dp primary, không tô nền, như cột hôm nay của S30, vì nền chờ 40% đọc giống Thiếu. Ngày tương lai và ngày trước khi dùng app: không cột, nhãn on-surface 38%.
- **Biểu đồ tháng:** 28–31 khe đều 336/N dp, cột 6dp cùng ngữ pháp Đạt / Thiếu / Vượt (The Goal-Line Split Rule, phần vượt tối thiểu 3dp), cao 120dp, vạch mục tiêu và dải ±10% như tuần. Không nhãn giá trị trên cột; nghi ghi thiếu là `warning` 14dp tô đặc warn trên đỉnh cột; chưa ghi là ô 8×8dp nét đứt + "—" Label Small che nền; hôm nay chỉ viền. Trục là đường 1dp outline-var với vạch 1×5dp và nhãn dd/mm Label Small ở mỗi Thứ Hai (tương lai on-surface 38%), chấm 4dp primary dưới hôm nay.
- **Vạch TB của kỳ:** cả hai biểu đồ có thêm một đường liền 1dp primary ở TB kcal/ngày, cạnh vạch mục tiêu nét đứt; chú giải thêm mẫu đường liền + "TB N". Chỉ khi kỳ có ≥3 ngày ghi.
- **Chạm biểu đồ tháng:** cột quá hẹp để chạm từng cột, nên cả vùng vẽ là một `role="slider"` (`aria-valuemin` 1, `aria-valuemax` số ngày tới hôm nay, `aria-valuenow`, `aria-valuetext` đọc ngày, kcal và loại ngày), `touch-action: pan-y` để cuộn dọc vẫn chạy. Chạm hoặc kéo bắt dính vào một ngày: khe đó phủ on-surface 8% góc 4dp và có nhãn ngày 18dp (nền on-surface, chữ surface) trên đỉnh.
- **Hàng đọc số:** cố định ngay dưới biểu đồ để ngón tay không che, tối thiểu 56dp, góc 12dp, nền rãnh vòng (surface-cont sáng / surface-highest tối), `aria-live="polite"`: ‹ 48dp / ngày Label Large ("Thứ Tư, 08/10") trên Body Small "800 kcal · Thiếu 940" ("Đạt", "Vượt 660", "Chưa ghi · không tính vào TB", "hôm nay, đang ghi"; số không mang dấu vì chữ đã nói chiều), thêm dòng `warning` 14dp warn "Nghi ghi thiếu" khi có / › 48dp / nút chữ 48dp "Mở ngày" sang S33. ‹ › bước từng ngày và tắt ở hai đầu (on-surface 38%, `aria-disabled`). Chưa chọn ngày: một dòng Body Small on-surface-var hướng dẫn nằm giữa ‹ ›.
- **Hàng sang S35 (chỉ chế độ Tuần):** hàng "Xem thêm" của S30 (64dp, lưới 40dp / 1fr / 24dp): ô `insights` 40dp primary-container, "Xem tổng kết tuần" Title Medium, dòng phụ Body Small on-surface-var "66 điểm, tạm tính · món ăn và nhận xét AI" (tuần đã xong: "71 điểm · Tốt · món ăn và nhận xét AI"). Điểm chỉ ở dòng phụ, không bao giờ lớn.
- **Các tuần trong tháng (chỉ chế độ Tháng):** thẻ "Các tuần trong tháng" + "chạm để xem tuần" Label Medium on-surface-var; hàng nút 56dp tràn bề ngang thẻ (lưới 1fr / auto / 24dp, đệm 6 / 12 / 6 / 16dp), ngăn bằng đường 1dp outline-var 55%: khoảng ngày Body Medium (tuần này 600) trên Body Small ("7 ngày · 1 chưa ghi · đạt 4", "2 ngày trong tháng · đạt 1", "Tuần này · 4 ngày, tạm tính · đạt 2"), TB Title Medium + "kcal TB" Label Medium, `chevron_right` 20dp. Chạm mở chế độ Tuần của tuần đó.
- **Macro:** hai thanh chồng năng lượng "Thực tế" / "Mục tiêu" và ba thanh macro của S35, đầu thẻ "Macro · TB mỗi ngày" + "đạt đạm **n**/N ngày".
- **Nước:** một làn TB kiểu S15, không cột theo ngày: "Nước · TB mỗi ngày" + "đạt **n**/N ngày"; số Title Large "2,17" + "L" Label Medium, "của 2,6 L" canh phải; làn 10dp surface-highest, một đoạn primary dài theo TB / mục tiêu, toàn làn là mục tiêu, vạch 2dp on-surface ở cuối; dòng Label Medium "Tổng **15,2** / 18,2 L · thiếu 3,0 L". Không có trạng thái vượt.
- **Cân nặng:** đầu thẻ "Cân nặng" + `chevron_right` 48dp mở S31. Tuần: TB tuần Title Large + "kg TB tuần · N lần cân", thay đổi so với tuần trước Title Medium on-surface bên phải; chưa cân: ô nét đứt 14×10dp + Body Small nêu TB tuần gần nhất + nút chữ `add` "Ghi cân". Tháng: ngữ pháp xu hướng S31 theo tuần: "76,0 → 74,6" Title Large + "kg TB tuần", thay đổi bên phải so với TB tuần cuối của tháng trước (không có thì từ tuần đầu); đường 2dp primary 304×56dp với ô vuông 7dp (ô cuối tô đặc), tuần chưa cân là ô nét đứt 14×10dp "chưa cân". Một tuần thuộc tháng chứa ngày Thứ Năm của nó, và một dòng Body Small nói điều đó ("Tuần tính theo ngày Thứ Năm: tuần 29/09 – 05/10 thuộc tháng 10"). Thay đổi không bao giờ mang màu (The No-Judgement Scale Rule).
- **So với kỳ trước:** "So với tuần trước" / "So với tháng trước" Title Medium, dòng Label Medium on-surface-var nêu khoảng được so ("Cùng 4 ngày đầu: T2–T5 tuần trước (03–06/11)", "Cùng 13 ngày đầu: 01–13/10"). Bảng lưới 1fr / 64dp / 64dp / 74dp, hàng ngăn bằng đường 1dp outline-var 55%: tên Body Medium, kỳ này 600 on-surface, kỳ trước on-surface-var, cột "Chênh" Label Medium on-surface-var với `arrow_upward` / `arrow_downward` 16dp (bằng nhau là "="); các hàng TB kcal/ngày, Ngày đạt, Đạm TB, Nước TB (chênh làm tròn 10 ml), Cân TB tuần ("—" và "chưa cân" khi kỳ này chưa có). Đơn vị 11sp on-surface-var. Cuối là dòng Body Small "Mũi tên chỉ tăng hay giảm, không phải tốt hay xấu." cùng điều kiện so. Kỳ trước không đủ dữ liệu: không bảng, chỉ `info` 18dp + Body Small nói vì sao và cần gì (The Same-Days Rule).
- **Người mới (dưới 3 ngày ghi):** không Display Large; thẻ Calo có `flag` 20dp FILL 1 primary + Body Small "Bạn bắt đầu dùng Cali từ **12/11** …" nêu ngày sớm nhất có trung bình (ngày bắt đầu + 2); biểu đồ chỉ có cột từ ngày bắt đầu, ngày trước đó không tính là chưa ghi. Thay các thẻ khác là "Sắp có số": danh sách ngăn bằng đường 1dp outline-var 55%, mỗi dòng biểu tượng 20dp primary (`pie_chart`, `monitor_weight`, `compare_arrows`) + Body Small nêu đúng ngày từng phần có số, suy từ luật ≥3 ngày ghi, ≥2 lần cân một tuần và The Same-Days Rule.
- **Chuyển động:** chuyển động duy nhất được dàn dựng của S32: các cột mọc từ đường đáy khi mở một kỳ (scaleY 0 → 1, 500ms `cubic-bezier(.2,0,0,1)`, trễ 80ms + 40ms mỗi cột ở tuần, 14ms mỗi cột ở tháng); tắt khi giảm chuyển động.
- **Lớp trạng thái:** hàng sang S35 và hàng tuần phủ on-surface 8% khi hover; focus là vòng 2dp primary cách 2dp.

### Lịch sử (S30)
Lịch tháng xếp theo tuần, vì tuần là đơn vị của tổng kết: mỗi tuần là một hàng S35 thu nhỏ, nên một tuần đọc như biểu đồ 7 cột.
- **Thẻ tuần:** surface-low góc 16dp, rộng đúng 7 × 48dp. Đầu thẻ là một nút 48dp (đệm 0 / 4 / 0 / 14dp, cách 8dp) mở S35: khoảng ngày "dd/mm – dd/mm" Label Medium on-surface-var; bên phải điểm Label Large 650 on-surface + "· Xếp loại" Label Medium on-surface-var; cuối là `chevron_right` 20dp on-surface-var. Tuần đang diễn ra: viền trong 1.5dp primary, nhãn "Tuần này · dd/mm – dd/mm" 600 on-surface, và chip 24dp góc 8dp secondary-container "Tạm tính · N" thay chỗ điểm (người mới chưa có điểm: "Tạm tính").
- **Ô ngày:** nút 48×72dp mở chi tiết ngày (S33), nhãn đọc màn hình đầy đủ ("Thứ Ba, 11/11: 1.690 kcal, đạt. Mở chi tiết ngày"). Số ngày trong vòng 28dp, Label Large 600, đệm trên 4dp. Hôm nay: viền trong 1.5dp primary, số primary 700. Ngày của tháng kề: on-surface-var 500, vẫn có cột để tuần đọc trọn. Ngày tương lai và ngày trước khi bắt đầu dùng app: on-surface 38% 500, không cột, không bấm được.
- **Cột mini:** 14dp, đầu vuông, đứng cách đáy ô 8dp trên thang 30dp tới 155% mục tiêu. Mỗi hàng tuần có đúng một vạch mục tiêu nét đứt 1dp on-surface-var 70% chạy suốt hàng (thụt 8dp hai đầu), chỉ khi hàng có ít nhất một ngày có cột. Đạt (trong ±10%) = primary; Thiếu = Chàm Thiếu (`--under`); Vượt = primary đến vạch + khe 3dp + phần vượt error (tối thiểu 3dp, đã trừ khe để tổng chiều cao vẫn đúng tỉ lệ), theo The Goal-Line Split Rule.
- **Cột hôm nay:** chỉ viền trong 1.5dp primary, không tô nền. Khác đoạn chờ của S35 (nền primary 40% pha vào rãnh): ở cỡ 14dp và nhất là ở chế độ tối, nền 40% gần như trùng Chàm Thiếu, nên một ngày đang ghi sẽ đọc thành một ngày Thiếu đã chốt. Ô rỗng có viền đọc là "chưa xong", không phải một trạng thái.
- **Chưa ghi** (đã dùng app nhưng ngày đó không ghi gì): ô 14×10dp nét đứt 1.5dp outline + "—" Label Small on-surface-var trên nền surface-low góc 4dp (che vạch mục tiêu). Không bao giờ là cột 0.
- **Nghi ghi thiếu** (dưới 50% mục tiêu, trừ hôm nay): `warning` 14dp tô đặc màu warn ở góc trên phải vòng số ngày; cột vẫn giữ màu trạng thái của nó (The Amber Check Rule).
- **Ngày bắt đầu dùng app:** `flag` 16dp tô đặc primary ở góc trên trái vòng số ngày. Ngày trước đó chỉ có số mờ, không tính là "chưa ghi" và không vào chú giải.
- **Hàng không thẻ:** tuần tương lai và tuần nằm trọn trước ngày bắt đầu dùng app thu thành một hàng 40dp chỉ có số ngày mờ (7 × 48dp): không thẻ, không đầu thẻ, không vạch.
- **Chú giải:** Label Medium on-surface-var, ô 10dp vuông + tên + số đếm 600 on-surface ("Đạt **7**", "Vượt **2**", "Thiếu **4**", "Chưa ghi **0**"), đếm các ngày của tháng tới hôm nay, nên chú giải cũng là bảng đếm; loại chứa hôm nay thêm "(gồm hôm nay)"; "Nghi ghi thiếu **N**" với `warning` 14dp warn khi N > 0; mẫu vạch nét đứt "Mục tiêu 1.740" (không có ±10%, vì lịch không vẽ dải).
- **Tóm tắt:** lưới chỉ số ba cột như S02 (surface-low góc 16dp, đệm 12 / 4dp, đường ngăn 1dp outline-var 60%); nhãn Label Medium on-surface-var, giá trị Title Medium với đơn vị 12sp 500 on-surface-var.
- **Xem thêm:** khối surface-low góc 16dp chứa bốn hàng nút tối thiểu 64dp (lưới 40dp / 1fr / 24dp, cách 14dp, đệm 10 / 14dp), ngăn bằng đường 1dp outline-var 55%: ô biểu tượng 40dp góc 12dp primary-container / on-primary-container (`insights` Tổng kết tuần, `monitor_weight` Cân nặng, `bar_chart` Thống kê, `menu_book` Món của tôi), tên Title Medium, dòng phụ Body Small 400 on-surface-var nêu số mới nhất ("Tuần trước 71 điểm · Tốt", "74,1 kg · −0,5 kg so với tuần trước"), `chevron_right` on-surface-var. Có ở mọi trạng thái; với người mới, dòng phụ nói khi nào có số.
- **Ghi chú người mới:** surface-low góc 16dp, đệm 12 / 14dp, cách 10dp: `flag` 20dp primary + Body Small on-surface-var (ngày bắt đầu đậm on-surface), nói ngày trước đó không tính là chưa ghi và khi nào có tổng kết chính thức đầu tiên.
- **Lớp trạng thái:** ô ngày, đầu thẻ tuần và hàng "Xem thêm" phủ on-surface 8% khi hover, 12% khi nhấn; focus là vòng 2dp primary cách 2dp.
- **Ghi chú dữ liệu:** mockup dùng một lịch giả để khớp các màn khác (13/11/2026 là Thứ Năm; lịch thật là Thứ Sáu). Code dùng lịch thật; bố cục và quy tắc không đổi.

### Cân nặng (S31)
Mọi thứ xếp theo tuần, cùng đơn vị với S35, vì cân dao động 0,5–1 kg mỗi ngày do nước và muối: số lớn là cân TB tuần, từng lần cân là chi tiết nhỏ và nhạt.
- **Thẻ cân TB tuần:** surface-low góc 16dp, đệm 14 / 16 / 16dp. Dòng đầu Label Medium on-surface-var: "TB tuần 03/11 – 09/11 · 3 lần cân"; khi tuần này đã có ≥2 lần cân thì là chip 24dp góc 8dp secondary-container `hourglass_top` 16dp "Tuần này · tạm tính" + "n lần cân"; người mới là `flag` 16dp "Lần cân đầu · Thứ Tư, 12/11". Hàng số canh đáy: TB tuần Display Large (dòng cao .9), "kg" Title Medium on-surface-var, bên phải thay đổi so với tuần trước Title Medium on-surface ("−0,5 kg") trên "so với tuần trước" Label Medium on-surface-var; không có tuần trước thì một dòng "Chưa có tuần trước để so". Thay đổi không bao giờ mang màu.
- **Tuần đang diễn ra chỉ thành điểm tuần khi có ≥2 lần cân.** Khi tuần này mới có 1 lần, số lớn vẫn là tuần trọn gần nhất, và dưới hàng số là một dòng Body Small on-surface-var với `hourglass_top` 16dp: "Tuần này (tạm tính) **73,8 kg** · 1 lần cân · cần 2 lần để thành TB tuần".
- **Làn tiến độ:** cách 18dp, cao 10dp, rãnh surface-highest, phần đã đi primary, đầu vuông, một đoạn; từ TB tuần đầu tiên tới cân mục tiêu. Dưới cách 8dp, Label Medium on-surface-var hai đầu: "Tuần đầu **76,0** kg" / "Mục tiêu **68,0** kg" (người mới: "Bắt đầu").
- **Dòng diễn giải:** sau đường ngăn 1dp outline-var 55%, `flag` 18dp primary + Body Small: "Đã giảm **1,9 kg** · Còn **6,1 kg** · dự kiến **~07/02/2027**". Đã giảm, còn lại và dự kiến tính theo lần cân mới nhất (02 §1); khi số đó khác TB tuần ở trên, thêm "(theo lần cân dd/mm)", nếu không thì "(~N tuần)". Ngày dự kiến tính từ hôm nay (02 §9), luôn có dấu "~" và năm: "~dd/mm/yyyy".
- **Thẻ Xu hướng:** tiêu đề "Xu hướng" Title Medium; bộ chọn khoảng; biểu đồ; chú giải; đường ngăn; các dòng diễn giải Body Small với biểu tượng 18dp (`trending_down` "30 ngày: TB tuần **75,4 → 74,1 kg** (**−1,3 kg**)", `speed` "Tốc độ TB **−0,43 kg/tuần** · kế hoạch −0,5 kg/tuần", `info` on-surface-var giải thích đường tuần, và ở Tất cả `flag` cho đường dự kiến).
- **Bộ chọn khoảng:** viên thuốc 48dp, bốn phần đều "7 ngày" / "30 ngày" / "90 ngày" / "Tất cả", viền và đường ngăn 1dp outline, Label Large on-surface; phần đang chọn secondary-container (`role="radiogroup"`). Mặc định 30 ngày.
- **Biểu đồ xu hướng:** tràn bề ngang thẻ, cao 172dp; nhãn trục Label Small on-surface-var bên phải (bước .5 / 1 / 2 / 5 kg sao cho ≤6 vạch), lưới và đường đáy outline-var 55%. Trục ngày: 7 ngày là thứ (T2…CN), 30 ngày là các Thứ Hai dd/mm, 90 ngày cách 14 ngày, Tất cả là đầu tháng "thg N". Đường TB tuần 2dp primary, điểm đặt ở Thứ Năm của tuần (không quá hôm nay); ô vuông 7dp viền 1.5dp primary nền surface-low, ô cuối tô đặc. Từng lần cân: ô 5dp on-surface-var, ẩn ở Tất cả. Điểm tuần gần nhất nằm ngoài khung được nối vào và đường bị cắt tại mép, để đường luôn đi vào từ trái. Khi khung không chứa điểm tuần nào (7 ngày), TB của tuần đó vẽ thành một đoạn ngang 2dp primary trên phần tuần nằm trong khung, kèm nhãn "TB tuần 03/11 – 09/11: 74,1 kg"; không phải trạng thái trống. Vạch mục tiêu nét đứt 1dp on-surface-var kèm "Mục tiêu 68 kg" chỉ vẽ khi 68 nằm trong khung; nếu không, chú giải ghi "Mục tiêu 68 kg · dưới khung" + `south` 14dp. Tất cả kéo khung xuống tới mục tiêu, thêm đường "Hôm nay" dọc 1dp outline với nhãn đậm 700 on-surface, và đường dự kiến nét đứt 1.5dp primary từ điểm tuần cuối tới ô đích nét đứt ở mục tiêu, nhãn "~dd/mm". Đường TB tuần vẽ một lần khi mở (700ms, `cubic-bezier(.2,0,0,1)`, trễ 120ms), tắt khi giảm chuyển động. Chưa đủ 2 tuần có số: không vẽ đường giả, một ghi chú surface-low góc 8dp `info` primary nói khi nào có đường.
- **Chú giải:** Label Medium on-surface-var: mẫu đường + ô "TB tuần", ô 5dp "Lần cân **N**" (không có ở Tất cả), mẫu nét đứt primary "Dự kiến" (chỉ Tất cả), mẫu nét đứt "Mục tiêu 68 kg".
- **Thẻ BMI:** thẻ BMI của S02 dùng lại, theo lần cân mới nhất, thêm dòng Body Small "Theo lần cân 08/11 (74,1 kg) và chiều cao 170 cm. Ở 68 kg: BMI 23,5 · Thừa cân." (The No-Judgement Scale Rule).
- **Bản ghi:** thẻ surface-low, đầu thẻ "Bản ghi" Title Medium + "N lần cân" Label Medium on-surface-var. Nhóm theo tuần, mới nhất trên. Đầu nhóm Label Medium on-surface-var: khoảng ngày (tuần này: "Tuần này · dd/mm – dd/mm" 600 on-surface) và bên phải "TB **74,1** kg · −0,5 kg", trong đó TB Title Medium on-surface là số mạnh nhất của nhóm ("· tạm tính" khi tuần này mới có 1 lần). Hàng ngày cao tối thiểu 48dp, tràn bề ngang thẻ, đệm 4 / 16dp, ngăn bằng đường 1dp outline-var 55%: ngày bên trái (Body Medium on-surface, "Hôm nay, 13/11"), số kg Body Large on-surface-var + "kg" 12sp bên phải; nhãn phụ Label Small dưới ngày khi cần ("Mục tiêu 1.740 kcal tính theo số này", `flag` 14dp primary "Lần cân đầu"). Tuần này chưa cân: hàng ô nét đứt 14×10dp + "Chưa cân tuần này" Body Small. Chạm hàng để sửa; vuốt trái để xoá (nền error-container 112dp) + snackbar "Đã xoá cân 06/11 · 73,9 kg" / Hoàn tác; TB tuần tính lại ngay.
- **Sheet Ghi cân nặng:** sheet phụ surface-low góc trên 28dp trên màn che, đệm 0 / 20 / 28dp, tay nắm, tiêu đề Title Large. Chip ngày 32dp góc 8dp secondary-container (vùng chạm 48dp): `calendar_today` 18dp + "Hôm nay · Thứ Năm, 13/11" + `arrow_drop_down`; mặc định hôm nay. Ngày đã có bản ghi: dòng `info` 16dp Body Small "08/11 đã có **74,1 kg**. Mỗi ngày một bản ghi: lưu sẽ thay số này." và nút lưu đổi thành "Thay bản ghi". Ô nhập là OutlinedTextField "Cân nặng" với hậu tố "kg", bàn phím số; dòng hỗ trợ nêu hệ quả "BMI 25,5 · Thừa cân · −0,3 kg so với lần cân 08/11". Lỗi khoảng (khi rời ô) theo ô nhập M3; nếu bỏ một dấu phẩy là ra số hợp lệ, ngay dưới là nút tonal 48dp `edit` "Có phải 74,1 kg?" sửa một chạm. Hành động: "Huỷ" chữ + filled "Lưu", 48dp canh phải; tắt khi lỗi.
- **Sheet tính lại mục tiêu (F09):** cùng sheet phụ, hiện sau khi lưu cân. Tiêu đề "Cập nhật mục tiêu calo?", lời dẫn Body Medium on-surface-var nói mục tiêu đang dùng tính theo số nào. Hàng so sánh lưới 1fr / 24dp / 1fr canh đáy: "Đang dùng" Label Medium + mục tiêu cũ Title Large on-surface-var + "kcal/ngày"; `arrow_forward` on-surface-var; "Theo 73,8 kg" + mục tiêu mới Display Small + "−10 kcal" primary 600 · "kcal/ngày". Bảng ba cột tên / Đang dùng / Mới (1fr / 80dp / 88dp), tiêu đề Label Medium on-surface-var, hàng TDEE / Đạm / Tinh bột / Béo / Nước ngăn bằng đường 1dp outline-var 55%, Body Medium; cột cũ on-surface-var; giá trị mới đổi thì 650 on-surface, không đổi thì 500 on-surface-var; đơn vị Label Small. Dòng an toàn trung tính: `verified_user` 18dp primary + Body Small nêu thâm hụt so với giới hạn 25% TDEE và sàn (BMR / 1.500); chỉ khi bị kẹp mới thành hộp hổ phách (The Amber Adjust Rule). Hàng 48dp "Tự tính lại khi ghi cân" + Switch M3 52×32dp (tắt: viền 2dp outline, rãnh surface-highest), dòng Body Small "Áp dụng từ hôm nay 13/11; các ngày trước vẫn so với 1.740 kcal.", rồi "Giữ nguyên" chữ + filled "Cập nhật mục tiêu".

### Chi tiết ngày (S33)
S10 cho một ngày đã qua: cùng vòng, thẻ bữa, macro, ô Nước / Cân nặng; khác ở thanh trên, tâm vòng và lời của macro.
- **Thanh trên:** xem Layout. Chạm tiêu đề mở lịch; ‹ › bước một ngày.
- **Tâm vòng:** Δ có dấu và loại ngày (xem Vòng calo). Dưới vòng là hàng "Đã ăn" / "Mục tiêu" (Label Medium on-surface-var trên Title Medium), cách nhau 40dp.
- **Chip loại ngày:** cao tối thiểu 32dp, góc 8dp, đệm 4 / 12dp, nền surface-cont (tối: surface-highest), chữ Label Large on-surface, lời phụ 500 on-surface-var. Đầu chip là ô 10dp vuông lấy đúng từ chú giải biểu đồ tuần: primary "Đạt · trong ±10% mục tiêu", Chàm Thiếu "Thiếu" + phần trăm thiếu so với mục tiêu, nét đứt 1.5dp outline "Chưa ghi không tính vào tổng tuần". Thiếu không bao giờ là hổ phách. Khi vượt, chip này nhường chỗ cho chip trạng thái ("Vượt 36% so với mục tiêu" `warning`, "Vượt nhẹ 1% · vẫn tính Đạt" `info`). Ngày chưa ghi có thêm một dòng Body Small canh giữa nói món thêm sẽ lấy giờ giữa khung bữa.
- **Nghi ghi thiếu** (dưới 50% mục tiêu): ngày vẫn là "Thiếu", chip thêm `warning` 16dp tô đặc màu warn; dưới chip là hộp warn-container góc 16dp (đệm 12 / 8 / 8 / 14dp, lề 12dp): `warning` 20dp + Body Small "**Nghi ghi thiếu.** Ngày này chỉ ghi 690 kcal, chưa tới một nửa mục tiêu. Có thể còn sót món; …", rồi các nút thêm nhanh cho từng bữa trống, canh phải, cách 8dp: viên thuốc 48dp nền surface, chữ primary Label Large, `add` 20dp + "Tối · 19:45" / "Phụ · 15:45" (giờ giữa khung bữa). Theo The Amber Check Rule.
- **Macro của ngày đã qua:** như S10, nhưng không bao giờ "còn": dưới mục tiêu là "thiếu X g", đạm từ 90% mục tiêu (hoặc macro đúng bằng mục tiêu) là "đạt", trên mục tiêu là "vượt X g" màu error với thanh tách tại vạch.
- **Hàng nút:** filled "Thêm món" (`add`, co giãn 1.25), tonal "Chat AI" (1), outlined vuông 48dp `add_photo_alternate` (thêm từ ảnh thư viện). Ngày đã qua không chụp: camera không có mặt.
- **Thẻ bữa:** như S10; bữa trống ghi giờ mặc định giữa khung bữa ở chỗ tổng kcal (`schedule` 14dp + "07:30" Label Medium on-surface-var) cạnh nút +. Ô Nước dùng nút "+250" / "Sửa" cho ngày đó; ô Cân nặng không có số thì "—" + "Không cân ngày này" + nút outlined "Ghi cân".
- **Thẻ tuần:** thẻ tổng kết tuần của S10 dùng lại (ô điểm 48dp, "Tuần 03/11 – 09/11" Title Medium, dòng phụ Body Small, `chevron_right`), nằm dưới hàng nút. Tuần đang diễn ra: "Tuần này · 10/11 – 16/11" và chip 24dp góc 8dp secondary-container "Tạm tính · còn N ngày" thay dòng phụ. Khi sửa một tuần đã có nhận xét AI, thẻ có thêm một hàng ngăn bằng đường 1dp outline-var 55% (thụt 12dp trái): `sync` 20dp primary trong cột 48dp, Body Small on-surface-var "Dữ liệu tuần đã thay đổi sau nhận xét AI (10/11 · 08:02).", và nút chữ 48dp primary "Tạo lại". Là một hàng của thẻ, không phải hộp lồng.
- **Thêm món cho ngày đã qua:** sheet xác nhận S12 nguyên vẹn; chip giờ ghi cả ngày "07:30 · Sáng · 12/11" với nguồn Body Small "`history` Giữa khung bữa Sáng"; làn "12/11 sau khi lưu" (xem Thanh làn); nút Lưu "Lưu vào Sáng · 12/11".

### Camera (S11)
Một lần chụp là một ảnh, và giờ ăn sẽ ghi luôn thấy trước khi bấm. Toàn màn theo The Forced-Dark Camera Rule: mọi màu là token `-dark`, cộng #000 và lớp che 62%.
- **Thanh trên (trên lớp che):** `close` tròn 48dp nền lớp che, biểu tượng on-surface. Bên phải viên thuốc flash 48dp (đệm 0 / 16 / 0 / 12dp, cách 6dp, Label Large) luôn ghi chữ trạng thái: `flash_auto` "Tự động", `flash_on` FILL 1 "Bật", `flash_off` "Tắt"; ở "Bật" viên thuốc đổi sang secondary-container / on-secondary-container tối. Cạnh đó `cameraswitch` tròn 48dp trên lớp che.
- **Vòng căn đĩa:** vòng tròn 264dp, nét 2dp on-surface với viền tương phản 1.5dp #000 60% sát bên ngoài (một nét, ~5,7:1 trên nền trắng, không phải bóng), để mép vòng còn đọc được trên đĩa sáng. Phía trên là một viên gợi ý duy nhất: tối thiểu 32dp, đệm 6 / 14dp, lớp che, `center_focus_weak` 16dp + Label Medium "Chụp từ trên xuống, thấy cả đĩa".
- **Thiếu sáng (trung tính):** không bao giờ hổ phách (The Amber Check Rule là danh sách đóng). Đèn đã bật: viên gợi ý đổi thành `brightness_low` 16dp + "Thiếu sáng · đèn đã bật, đưa món lại gần nguồn sáng". Đèn tắt, hoặc Tự động mà đèn chưa kích: thanh trên lớp che ngay trên panel, tối thiểu 56dp, đệm 4 / 4 / 4 / 16dp, `brightness_low` 20dp + Body Medium "Thiếu sáng, ảnh có thể khó nhận món" + nút tonal 48dp `flash_on` 18dp "Bật đèn" (secondary-container tối, chữ 600). Không chặn việc chụp.
- **Panel dưới:** nền #000 góc trên 28dp. Chip giờ ăn · bữa của S12 (giờ lúc bấm chụp, bữa tự gán theo giờ, `expand_more` 18dp ở cuối) canh giữa ngay trên nút chụp; chạm mở sheet giờ ăn & bữa của S12. Nút chụp 72dp: vòng 4dp on-surface quanh lõi tròn đặc 54dp on-surface. Hai bên là hai ô có nhãn: Thư viện (`photo_library`, trái) và Ghi chú (`edit_note`, phải), mỗi ô 48dp surface-highest tối với biểu tượng on-surface, nhãn Label Small on-surface-var bên dưới, cột 56dp.
- **Xin quyền:** màn đặc surface tối, `close` ở thanh trên. Ô 72dp surface-highest với `no_photography` 36dp on-surface-var; "Cần quyền dùng camera" Title Large; lời Body Large on-surface-var nói vì sao cần và ảnh được xử lý thế nào; một dòng Body Medium chỉ lối thư viện không cần quyền camera. Chân: filled 56dp "Cho phép" (`photo_camera`, gọi hộp thoại hệ thống); khi người dùng đã chọn "Không hỏi lại", nút đổi thành "Mở Cài đặt" (`settings`) và lời nói rõ bật ở đâu (Quyền → Camera). Dưới là outlined 56dp "Chọn từ thư viện" (`photo_library`).
- **Xem lại:** thanh trên `arrow_back` + "Xem lại ảnh"; khung ảnh surface-low góc 16dp. Hàng giờ: chip giờ ăn · bữa (chạm để sửa) + nguồn giờ bằng chữ Body Small on-surface-var với biểu tượng 16dp ("Lúc bấm chụp" `photo_camera`). Ô ghi chú là chat-field của S20: nhãn Label Medium "Ghi chú cho AI (không bắt buộc)", ô tối thiểu 48dp góc 24dp surface-cont, `edit_note` on-surface-var, chữ Body Large, chữ mồi "Ví dụ: ăn một nửa, ít cơm", con trỏ 2dp primary khi focus. Khi ô trống, ba chip gợi ý 32dp góc 8dp viền 1dp outline, chữ 12sp 600 on-surface-var, vùng chạm 48dp: "ăn một nửa", "ít cơm", "không đường". Các dòng Body Small với biểu tượng 18dp primary (`info`, `lock`) nói ghi chú giúp gì và ảnh được thu nhỏ còn 1024 px, xóa vị trí. Chân surface-cont: tonal 56dp "Chụp lại" (`photo_camera`) + filled 56dp co giãn "Phân tích" (`auto_awesome`).
- **Ảnh từ thư viện:** cùng màn xem lại, tiêu đề "Ảnh từ thư viện", nút tonal "Ảnh khác" (`photo_library`). Giờ lấy từ EXIF (đọc trước khi xóa), nguồn ghi "Giờ chụp ảnh" với `photo_library` 16dp. Khi giờ đó rơi vào ngày khác, chip ghi cả ngày ("Hôm qua 19:05 · Tối") và ngay dưới là một dòng `event` 18dp Body Small nêu rõ ngày và bữa đích: "Món sẽ ghi vào **Thứ Tư 12/11**, bữa Tối. Chạm chip để đổi giờ." (ngày đậm 600 on-surface). Không có EXIF thì dùng lúc chọn ảnh và dòng nguồn đổi theo. Mỗi lần chọn đúng một ảnh.
- **Đang phân tích:** thanh trên chỉ có `close` (hủy). Ảnh 240dp hạ còn 55% dưới vạch quét 2dp primary; vạch quét là chuyển động duy nhất của Camera (lên xuống qua lại 6% → 94%, 2,8s `cubic-bezier(.4,0,.2,1)`), tắt khi giảm chuyển động. Một tiêu đề trạng thái "Đang nhận diện món ăn…" Title Large, dưới là "Đã chờ 0:07" Body Medium on-surface-var: đồng hồ đã chờ thật, không phần trăm giả. Danh sách hai bước thật (thu nhỏ ảnh; AI nhận diện), mỗi hàng tối thiểu 48dp, huy hiệu 24dp tròn: xong = primary / on-primary với `check`; đang chạy = secondary-container với vòng 2dp primary, chữ on-surface; chưa tới = viền 1dp outline-var; dòng phụ Body Small dưới mỗi bước. Một dòng `schedule` nói chờ tối đa 60 giây và app tự thử lại. Chân: outlined 56dp "Hủy" + Body Small canh giữa "Hủy thì không lưu gì cả."
- **Lỗi (mất kết nối; AI không trả lời sau 60 giây):** thanh trên `close` + "Chưa phân tích được". Ảnh giữ nguyên, không làm mờ; ghi chú trích lại (`edit_note` 20dp + chữ trong ngoặc kép on-surface). Hộp lỗi ai-error-box bản tối (error-container / on-error-container, góc 16dp, lề 12dp, đệm 14 / 16dp): `cloud_off` hoặc `hourglass_disabled` 20dp, tiêu đề 16sp 600 và một câu nói ảnh và ghi chú vẫn được giữ. Chân: filled 56dp toàn chiều rộng "Thử lại" (`refresh`), dưới là hai outlined 48dp chia đôi "Nhập tay" (`edit`) / "Món của tôi" (`menu_book`), lối đi không cần AI. Hai lỗi cùng bố cục, chỉ khác biểu tượng và chữ.

### Thêm nhanh (S13)
Một chạm vào món quen là sang S12 với món đó; không gọi AI, không lưu tại đây.
- **Sheet:** xem Layout. Dòng phụ "Hôm nay · còn N kcal" của thanh luôn thấy (như thanh trên S20), chỉ đổi khi lưu (The Ledger Rule).
- **Ô tìm:** 56dp tròn hết cỡ, nền surface-highest ở cả sáng và tối (token sẵn có), `search` on-surface-var, chữ Body Large, chữ mồi "Tìm trong N món đã lưu", con trỏ 2dp primary; có chữ thì `close` 48dp xoá ô. Tìm không dấu (qua `name_normalized`, đ = d): phần khớp tô từng ký tự trên tên gốc có dấu, primary 700, gạch chân 1.5dp cách 3dp. Kết quả xếp theo số lần dùng.
- **Chip giờ ăn · bữa:** chip của S12, nguồn bằng chữ Body Small on-surface-var với biểu tượng 16dp cạnh chip. Mở từ "+" của S10: giờ lúc bấm, bữa tự gán, nguồn `touch_app` "Lúc bấm thêm". Mở từ "+" của một thẻ bữa: bữa đặt sẵn, giờ là giữa khung bữa (cùng quy tắc S33), nguồn `history` "Theo bữa Sáng". Chip vẫn chạm để đổi.
- **Bộ lọc:** bộ chọn khoảng 48dp của S31 với ba phần Gần đây / Hay dùng / Yêu thích (`role="radiogroup"`), mặc định Gần đây. Gần đây nhóm theo ngày dùng cuối, đầu nhóm Label Medium on-surface-var ("Hôm nay", "Hôm qua", "Thứ Hai, 10/11") đệm 10 / 16 / 2dp.
- **Hàng món (Thêm nhanh):** dòng thẻ xác nhận thu gọn, tối thiểu 64dp, lưới 1fr / auto / 48dp, đệm 6 / 4 / 6 / 16dp: tên Body Medium (một dòng, cắt "…"), dưới là khẩu phần Body Small on-surface-var + ba macro chấm 6dp ("2 22 4 g"); kcal Body Medium 600 canh phải + "kcal" Label Medium on-surface-var; cuối là `chevron_right` on-surface-var. Cả hàng là một nút mở S12 với món đó ở ×1; không có nút thêm tức thì. Hover phủ on-surface 8%.
- **Không khớp:** chân ghim ẩn; thẻ surface-low góc 16dp (đệm 20 / 16 / 16dp, canh giữa): `search_off` 32dp on-surface-var, "Chưa có “bun cha” trong Món của tôi" Title Medium, một câu Body Small nói khi nào chọn lối nào (Hỏi AI tốn 1 lượt AI) và giờ ăn vẫn giữ; rồi cặp lối thoát Hỏi AI / Nhập tay. Dưới thẻ một dòng Body Small "Cả hai lối đều điền sẵn “bun cha”."

### Nhập tay (S13)
- Cùng sheet, `arrow_back` quay về Thêm nhanh; chip giờ ăn · bữa giữ nguyên. Các ô là ô nhập outlined M3: "Tên món" (chữ Body Large 500) với dòng hỗ trợ "Bắt buộc"; hàng "Khẩu phần" / "Gram" (hậu tố "g"); "Kcal" với "Bắt buộc · cho đúng khẩu phần trên"; nhãn nhóm "Macro" Label Large + "không bắt buộc" Label Medium on-surface-var; ba ô Đạm / Tinh bột / Béo hậu tố "g".
- Rời ô Béo thì kiểm tra "Số liệu lệch" (The Amber Check Rule): cờ hổ phách 24dp + dòng `warning` 16dp Body Small nêu phép tính; không chặn. Món trên 2.500 kcal thêm cờ "Có vẻ cao".
- Chân là nút bước "Tiếp" (xem Buttons) mở S12; lưu và dấu trang Món của tôi làm ở S12.

### Sửa món (S14)
Chạm một món trên S10 (hoặc S33) mở lại đúng thẻ S12 của nó.
- **Thanh:** `close` (đóng, không lưu) / "Sửa món" / `delete`. Xoá không có hộp thoại: sheet đóng và S10 hiện snackbar "Đã xoá …" / Hoàn tác.
- **Thẻ món:** thẻ xác nhận S12 (Title Medium tên và kcal mới, khẩu phần mới + chấm macro), dòng `calculate` 16dp primary "1½ × 1 tô vừa (490 kcal), tính lại trên máy", dòng "AI giả định: …" khi món đến từ AI, bộ chọn khẩu phần và bookmark toggle (`bookmark_added` khi đã có trong Món của tôi). Chip giờ ăn · bữa giữ nguồn gốc của lần ghi ("Lúc bấm chụp", "Ghi từ chat 13/11 07:50").
- **Hàng mở rộng:** 48dp tròn, `edit_note` 20dp + "Sửa tên, kcal, macro" Label Large primary + `expand_more` / `expand_less`. Mở ra là ô "Tên món" và hàng bốn ô Kcal / Đạm / Tinh bột / Béo (1.2fr / 1fr / 1fr / 1fr). Gõ số khác thì khẩu phần chuyển sang ô bút (tự nhập).
- **Làn:** xem Thanh làn, mục Sửa món. Nút Lưu "Lưu thay đổi" (`check`) + kcal mới của món.

### Món của tôi (S34)
Sổ món đã đo sẵn: thêm lại một chạm, ghim món hay ăn, giữ kho gọn.
- **Ô tìm và sắp xếp:** ô tìm của S13 ("Tìm trong N món"); bộ chọn 48dp Hay dùng / Gần đây / A–Z, mặc định Hay dùng. Yêu thích luôn ghim đầu, bất kể cách sắp.
- **Hàng món đã lưu:** hàng của S13 thêm một dòng, tối thiểu 72dp, lưới 1fr / 48dp: vùng chính (đệm 8 / 4 / 8 / 16dp) là tên Body Medium, khẩu phần chuẩn + chấm macro Body Small, "21 lần · lần cuối 11/11" Label Small on-surface-var, kcal 600 canh phải; cuối là ngôi sao 48dp. Chạm vùng chính để sửa; ngôi sao bật / tắt yêu thích tại chỗ.
- **Vuốt xoá:** như thẻ món S10: nền error-container 112dp với `delete` 20dp + "Xoá", thả quá ngưỡng là xoá, không hộp thoại; snackbar "Đã xoá “Trà sữa trân châu”" / Hoàn tác. Xoá món đã lưu không đụng nhật ký: các lần đã ghi giữ nguyên số, chỉ mất liên kết (`my_food_id` về null).
- **Trống:** chỉ có FAB, không nút thứ hai trong thân. Khối surface-low góc 16dp: huy hiệu 48dp góc 16dp primary-container `menu_book`, "Chưa có món nào" Title Medium, một câu lợi ích Body Small, rồi hai dòng cách 10dp (lưới 24dp / 1fr, biểu tượng primary): `bookmark_add` "Trên thẻ xác nhận, chạm biểu tượng dấu trang ở món bạn hay ăn." và `edit_note` "Hoặc chạm **Thêm món** …".
- **Tạo món đã lưu (FAB "Thêm món"):** sheet "Món mới" với đúng các ô của Nhập tay ("Khẩu phần chuẩn"), hàng Yêu thích, rồi hộp thông tin surface-low góc 16dp (`info` 20dp primary, Body Small): "Chỉ lưu vào Món của tôi, **không ghi vào nhật ký hôm nay**." Chân là "Lưu vào Món của tôi" (`bookmark_add`).
- **Sửa món đã lưu:** sheet "Sửa món đã lưu" (`delete` trên thanh): ô Tên, Khẩu phần chuẩn / Gram, hàng Kcal / Đạm / Tinh bột / Béo (1.2fr / 1fr / 1fr / 1fr); dưới là "Trước khi sửa 650 kcal" Body Small on-surface-var và kết quả kiểm tra (dưới ngưỡng: một dòng on-surface-var nêu phần trăm lệch; trên ngưỡng: cờ hổ phách); hàng Yêu thích; rồi hộp thông tin `history` nói số lần đã dùng và rằng **các lần đã ghi giữ nguyên số cũ**, chỉ lần thêm sau dùng số mới. Chân "Lưu món" (`check`) + kcal.

### Nước (S15)
Một ngày nước là thanh làn của S12 duỗi theo mục tiêu; số lớn là tổng ngày, từng lần uống là chi tiết nhỏ và nhạt. Không hổ phách, không đỏ cho lượng nước.
- **Thanh trên:** xem Layout. Chạm tiêu đề mở lịch; ‹ › bước một ngày; › tắt ở hôm nay.
- **Thẻ tổng ngày:** surface-low góc 16dp, đệm 14 / 16 / 16dp. Dòng đầu Label Medium on-surface-var: `water_drop` 16dp FILL 1 primary + "Đã uống hôm nay · 4 lần" (ngày đã qua: "Đã uống ngày này"); khi đạt là Label Large 600 on-surface `check_circle` 18dp FILL 1 primary + "Đạt mục tiêu · +250 ml". Hàng số canh đáy, cách 8dp: tổng ngày Display Large (dòng cao .9) + "ml" Title Medium on-surface-var; bên phải là nút 48dp góc 12dp canh phải "48%" Title Medium trên "của 2.600 ml" Label Medium on-surface-var + `info` 14dp, mở sheet mục tiêu. Đây là chỗ duy nhất nói mục tiêu bằng chữ.
- **Làn nước:** cách 18dp, xem Shapes. Một đoạn mỗi lần uống theo thứ tự giờ; đạt thì làn đầy tới vạch và phần trên mục tiêu chạy làn 6dp vẫn primary (The Goal-Line Split Rule). Các đoạn đầy lần lượt từ trái (scaleX 700ms `cubic-bezier(.2,0,0,1)`, trễ 120ms + 90ms mỗi đoạn), tắt khi giảm chuyển động. Một nhãn đọc màn hình cho cả làn ("4 lần uống, tổng 1.250 trên 2.600 ml", thêm "trên mục tiêu N ml" khi có). Dưới làn cách 8dp, Label Medium on-surface-var: "Còn **1.350** ml · ~6 cốc 250 ml"; khi đạt: "Đạt lúc **21:10**".
- **Hàng nhắc:** một hàng 48dp của thẻ, ngăn bằng đường 1dp outline-var 55% (cách 12dp), không lồng hộp; lưới 20dp / 1fr / 20dp cách 12dp, góc 12dp, chữ Body Medium on-surface-var, giờ 600 on-surface, biểu tượng đầu primary, `chevron_right` 20dp cuối. Cả hàng mở S43 Nước & nhắc nhở. Ba dạng: `notifications` "Nhắc tiếp lúc **14:05**"; đã đạt `notifications_paused` "Đủ mục tiêu · nhắc lại **07:00** mai"; bật nhắc mà chưa có quyền thông báo `notifications_off` "Không nhắc được · **chưa có quyền**" (trung tính, không hổ phách). Ngày đã qua không có hàng nhắc. Giờ nhắc tiếp theo 06 §4: max(lần uống gần nhất, lần nhắc trước) + khoảng nhắc (mẫu 90 phút), kẹp trong khung nhắc (mẫu 07:00–22:00); ra ngoài khung hoặc đã đạt mục tiêu thì là đầu khung hôm sau ("07:00 mai"). Xoá một lần uống không phải lần gần nhất thì giờ nhắc không đổi.
- **Các lần uống:** thẻ surface-low, đầu thẻ "Các lần uống" Title Medium. Hàng tối thiểu 48dp, tràn bề ngang thẻ, đệm 4 / 16dp, lưới 48dp / 1fr / auto cách 8dp, ngăn bằng đường 1dp outline-var 55%: giờ Body Medium 600 (wdth 90); nguồn bằng chữ Body Small on-surface-var với biểu tượng 16dp theo `water_entry.source` (`water_drop` "Trong app", `notifications` "Từ thông báo", `widgets` "Từ widget"); lượng Body Large on-surface-var + "ml" 12sp 500 canh phải. Hàng của lần uống làm tổng chạm mục tiêu có thêm dưới nguồn nhãn Label Small 600 primary `check_circle` 14dp FILL 1 "Đạt mục tiêu · 2.850 ml" (tổng tới lần đó). Hover phủ on-surface 8%. Chạm hàng mở sheet sửa; vuốt trái để xoá: nền error-container 112dp với `delete` 20dp + "Xoá" Label Large; chưa thả thì chưa xoá (tổng không đổi, không snackbar); thả quá ngưỡng là xoá ngay, không hộp thoại, snackbar "Đã xoá 250 ml · 10:20" / Hoàn tác, và tổng, phần trăm, đoạn làn, cột hôm nay tính lại ngay khi hàng biến mất.
- **Chân thêm nhanh:** ghim đáy, xem Layout. Filled "+250 ml" với `water_drop` 20dp FILL 1 (co giãn 1.25), tonal "+500 ml" (1), outlined vuông 48dp `add` 20dp primary viền 1dp outline-var, nhãn đọc màn hình "Thêm lượng khác". Bút chì chỉ dành cho "Sửa"; lượng khác là `add`. Màn không có FAB; snackbar nổi trên chân. Ngày đã qua: trên hàng nút là một dòng Body Small on-surface-var `history` 16dp "Ghi cho 12/11 lúc 14:30 · chạm lần uống để đổi giờ"; thêm cho ngày đã qua mặc định 14:30 (giữa khung 07:00–22:00), và nhãn đọc màn hình của nút nói ngày đó.
- **Sheet thêm lượng khác:** sheet phụ surface-low góc trên 28dp trên màn che, tay nắm, tiêu đề "Thêm lượng khác" Title Large. Chip giờ 32dp góc 8dp secondary-container `schedule` 18dp + "Bây giờ · 12:40" + `arrow_drop_down`, chạm mở TimeInput của S12. Ô là OutlinedTextField "Lượng nước" (số 22sp 600, hậu tố "ml" Body Medium on-surface-var), bàn phím số; dòng hỗ trợ Body Small nêu khoảng và hệ quả: "10–3.000 ml · sau khi lưu **1.580 / 2.600 ml**". Ngoài khoảng: lỗi chỉ hiện khi rời ô (bàn phím đóng) theo ô nhập M3, viền 2dp error, nhãn error, dòng `error` 18dp "Nhập từ 10 đến 3.000 ml"; trong lúc gõ chỉ tắt nút. Lỗi nhập là đỏ (The Amber Check Rule), không hổ phách. Hành động 48dp canh phải: "Huỷ" chữ + filled "Thêm 330 ml"; tắt (on-surface 12% nền, chữ mờ, `aria-disabled`) và chỉ ghi "Thêm" khi lỗi.
- **Sheet sửa lần uống:** cùng sheet, tiêu đề "Sửa lần uống". Chip giờ mang giờ của bản ghi ("08:40"); cạnh chip là nguồn và ngày của chính bản ghi bằng chữ Body Small ("Trong app · 13/11"). Dòng hỗ trợ thêm phần chênh "(−100)". Hành động: "Xoá" là nút chữ primary bên trái với `delete` 20dp (đỏ không phải màu xoá; đỏ chỉ là nền vuốt xoá), rồi "Huỷ" + filled "Lưu". Xoá đóng sheet và hiện snackbar Hoàn tác.
- **Sheet mục tiêu nước:** mở từ nút "của 2.600 ml". Tiêu đề "Mục tiêu nước", "2.600" Title Large + "ml mỗi ngày · tự tính" Label Medium on-surface-var; ba dòng Body Small với biểu tượng 18dp primary, số đậm on-surface: `calculate` công thức "75,0 kg × 35 ml = **2.625 ml**, làm tròn 100 ml → **2.600 ml**" (02 §8, §10), `monitor_weight` lần cân làm gốc (cùng số với mục tiêu calo), `event_available` "Uống từ mục tiêu trở lên là **ngày đạt nước** trong tổng kết tuần." Hành động "Đóng" chữ + filled "Đổi mục tiêu" (mở S43).
- **Tuần này:** biểu đồ nước của S35 dùng lại (7 × 48dp, cột 20dp, cao 96dp), đầu thẻ "Tuần này · 10/11 – 16/11" Title Medium + "đạt n/m ngày" Label Medium on-surface-var. Đạt = primary (ngày trên mục tiêu là một cột liền, không tách); chưa đạt = Chàm Thiếu (`--under`); hôm nay ở ngữ pháp đoạn chờ; tương lai không cột. Nhãn giá trị theo lít ("2,7"). Chú giải: "Đạt **3**", "Chưa đạt **0**", "Hôm nay", mẫu vạch nét đứt "Mục tiêu 2,6 L". Cột chạm mở nước ngày đó; ngày đang xem mang dấu chấm 4dp.
- **Ghi chú dữ liệu:** mockup dùng lịch giả chung (13/11/2026 là Thứ Năm, bây giờ 12:40) và số khớp S10 (1.250 / 2.600 ml) và S33 (12/11: 2.700 ml).

### Cài đặt (S40–S45)
Bảng thông số của chính đồng hồ đo: mỗi hàng nói giá trị đang dùng bằng số tabular canh phải, và mọi thay đổi chạm tới mục tiêu calo đọc như một phép đo được tính lại, không như một form.
- **Hàng điều hướng (S40):** ngữ pháp hàng "Xem thêm" của S30: tối thiểu 64dp, lưới 40dp / 1fr / 24dp cách 14dp, đệm 10 / 14dp; ô biểu tượng 40dp góc 12dp primary-container / on-primary-container; tiêu đề Title Medium; dòng phụ Body Small on-surface-var là giá trị đang dùng ("1.740 kcal/ngày · giảm còn 68 kg", "Anthropic · key …3f9A · đã kết nối", "2,6 L/ngày · nhắc 90 phút, 07:00–22:00", "Sao lưu gần nhất 09/11 · 18 món đã lưu"); `chevron_right` on-surface-var cuối hàng. Dòng phụ của Bữa ăn liệt kê bốn bữa theo giờ bắt đầu ("Sáng 05:00 · Trưa 10:00 · Phụ 14:00 · Tối 17:30"); mỗi cặp bữa + giờ không bao giờ ngắt dòng ở giữa, chỉ xuống dòng tại " · ". Mục Giới thiệu là hàng không chạm: ô biểu tượng trung tính surface-highest / on-surface-var `info`, "Cali 1.0" và ghi chú y tế, không chevron. Hover phủ on-surface 8%.
- **Trạng thái "Để sau" (S40):** khi đã bỏ qua thiết lập ở S03, dòng phụ nói phần còn thiếu bằng lời trung tính: `key_off` 16dp + "Chưa có API key · cần để chụp món và chat", "2,6 L/ngày · nhắc đang tắt", "Chưa sao lưu lần nào · 0 món đã lưu". Không đỏ, không hổ phách: chưa cấu hình không phải lỗi. Chạm AI khi chưa có key mở thẳng S42.
- **Hàng cài đặt (S41–S45):** "nhãn ← giá trị". Tối thiểu 56dp, lưới 1fr / auto / 20dp cách 10dp, đệm 8 / 12 / 8 / 16dp; nhãn Body Large, dòng phụ Body Small on-surface-var dưới nhãn; giá trị canh phải, không ngắt dòng: số Title Medium tabular, đơn vị Label Medium on-surface-var cách 3dp ("170 cm", "0,5 kg/tuần", "1996 · 30 tuổi"), giá trị chữ Body Large ("Nam", "Giảm cân"); `chevron_right` 20dp on-surface-var. Hàng không mở gì thì bỏ chevron (lưới 1fr / auto). Hover phủ on-surface 8%.
- **Hàng công tắc:** 64dp, lưới 1fr / 52dp, đệm phải 16dp; cả hàng là `role="switch"` và là vùng chạm. Switch M3 52×32dp (xem Shapes). Dòng phụ nói điều công tắc làm hoặc số đang tự tính ("Tắt: hỏi sau mỗi lần ghi cân (Cân nặng)", "Đang tự tính: 1.740 kcal/ngày").
- **Mục tiêu đang dùng (S41):** thẻ surface-low mở đầu màn: "Mục tiêu đang dùng · từ 21/10" Label Medium on-surface-var; số kcal Display Small + "kcal/ngày" Label Medium on-surface-var; hàng macro Label Medium với chấm 8dp màu dữ liệu và số 600 on-surface, cộng nước `water_drop` 14dp; đường ngăn 1dp; hai dòng Body Small biểu tượng 18dp: `flag` primary "Đạt 68 kg khoảng ~dd/mm/yyyy (theo lần cân 08/11, ~N tuần)" và `info` on-surface-var nêu gốc tính "Tính theo 75,0 kg (lần cân 21/10)" cùng lần cân mới nhất sẽ dùng khi sửa. Dưới thẻ: nhóm Hồ sơ (Cân nặng chỉ đọc, dòng phụ chỉ sang Cân nặng S31) và nhóm Mục tiêu (Mục tiêu, Cân mong muốn kèm BMI, Tốc độ kèm "Khuyên dùng · bớt 550 kcal/ngày", công tắc "Tự tính lại khi ghi cân", cùng tên với S31).
- **Sheet Tốc độ (S41):** sheet phụ "Tốc độ mỗi tuần" Title Large, lời dẫn nêu lần cân làm gốc. Bốn hàng chọn hai dòng theo ngữ pháp chọn (tối thiểu 60dp, góc 16dp, đệm 10 / 14dp): trái là "0,75 kg" Title Medium + tên tốc độ Body Small (nhãn "Khuyên dùng" ở 0,5) trên "~N tuần" Label Medium; phải là kcal 600 trên dòng kẹp. Hàng bị kẹp mang dòng warn `info` 14dp "Đã chỉnh · ~0,52 kg/tuần" (The Amber Adjust Rule); hàng không kẹp giữ chỗ trống cùng cao để các hàng thẳng nhau. Trên hàng đang chọn chữ warn giữ màu warn, ≥4.5:1 trên secondary-container. Hành động "Huỷ" chữ + filled "Xong"; Xong mở sheet Đang dùng / Mới, chưa áp dụng.
- **Mọi thay đổi calo qua sheet Đang dùng / Mới:** sửa hồ sơ, đổi tốc độ hay ghi đè đều mở sheet tính lại của S31: "Áp dụng mục tiêu mới?", lời dẫn nêu thay đổi và hai lần cân (đang dùng / mới), Đang dùng Title Large on-surface-var → Mới Display Small với Δ primary, bảng TDEE / Đạm / Tinh bột / Béo / Nước, hộp hổ phách khi bị kẹp, dòng `flag` "… Áp dụng từ hôm nay 13/11; các ngày trước vẫn so với 1.740 kcal." Huỷ bỏ thay đổi (ô ghi đè trở về giá trị cũ); Áp dụng tạo mục tiêu mới từ hôm nay. Không thay đổi calo nào áp dụng thẳng từ một hàng.
- **Ghi đè thủ công (S41):** hai công tắc "Tự đặt calo mục tiêu" và "Tự đặt tỉ lệ macro". Khi bật, khối dưới công tắc (đệm 12 / 16 / 14dp) có ô OutlinedTextField "Calo mục tiêu" hậu tố "kcal/ngày", dòng hỗ trợ "Từ 1.000 đến 5.000 kcal". Số ghi đè đi qua đúng thứ tự kẹp của 02 §5: trần thâm hụt 25% TDEE trước, rồi sàn max(1.500, BMR), làm tròn 10 kcal. Bị kẹp là hộp warn-container `info` 20dp nêu giới hạn và giá trị thực ("1.400 kcal là thâm hụt 880 kcal/ngày, quá giới hạn 25% TDEE (570 kcal). App đã điều chỉnh về 1.710 kcal/ngày (~0,52 kg/tuần)."), không chặn. Macro là ba ô % (lưới ba cột cách 8dp), dòng `check_circle` 18dp FILL 1 primary "Tổng 100% · theo N kcal/ngày", và thanh tỉ lệ năng lượng 10dp của S02 với chú giải gram. Tổng ≠ 100% hay calo ngoài 1.000–5.000 là lỗi đỏ của ô nhập, hiện khi rời ô. Chú thích cuối: khi ghi đè, Hôm nay và Tổng kết tuần ghi nhãn "Tùy chỉnh".
- **Kết nối AI (S42):** thẻ surface-low: ô "Hãng AI" (OutlinedTextField chỉ chọn, `arrow_drop_down`); ô "API key" che bằng chấm giữ 4 ký tự cuối (chữ 15sp 500 giãn .12em), hai nút đuôi `visibility` / `content_paste`, dòng hỗ trợ "Chỉ hiện 4 ký tự cuối. Mã hoá trên máy, không vào bản sao lưu." Key đã lưu không bao giờ hiện lại đầy đủ. Nút tonal "Kiểm tra kết nối"; thành công là dòng `check_circle` FILL 1 primary "Đã kết nối lúc 20:14 · cả model ảnh và model chat đều trả lời". 401 là lỗi của ô: viền 2dp, nhãn và dòng hỗ trợ error với `error` 18dp ("API key không hợp lệ (401) …"). Mất mạng hay 429 không phải lỗi của key: chỉ báo bằng dòng chữ dưới nút, ô không đỏ.
- **Model và lời dặn (S42):** mục "Model" là thẻ hai ô OutlinedTextField chữ sửa được ("Model phân tích ảnh", "Model chat") với dòng hỗ trợ "Mặc định · …". Hàng "System prompt" mang trạng thái ở dòng phụ ("Bản mặc định v1 · chưa sửa") và mở trình soạn toàn màn hình (xem Layout): chữ 14sp 400 dòng cao 1.55, tiêu đề mục 650; chip "Đã sửa" 24dp góc 8dp secondary-container `edit` 16dp (như chip "Tạm tính"); nút chữ "Khôi phục mặc định" `restart_alt` không hỏi lại, hiện snackbar Hoàn tác; đóng khi chưa Lưu là bỏ thay đổi; chân surface-cont nói phần `<user_context>` app tự nối. Ghi chú cá nhân là OutlinedTextField nhiều dòng (tối thiểu 112dp, Body Large) sửa tại chỗ, bộ đếm "N/500" canh phải trên dòng hỗ trợ. Công tắc nhận xét AI tuần không lặp ở S42: chỉ một dòng Body Small `insights` on-surface-var "Nhận xét AI tuần tự tạo khi mở: bật/tắt ở Nước & nhắc nhở."
- **Nước & nhắc nhở (S43):** thiếu quyền thông báo là dải trung tính đầu màn theo ngữ pháp dải "dữ liệu đã đổi" của S35: secondary-container góc 12dp, lưới 20dp / 1fr / auto cách 10dp, đệm 6 / 6 / 6 / 12dp, `notifications_off` 20dp, Body Small, nút chữ "Cho phép" on-secondary-container ("Mở Cài đặt" khi đã bị từ chối hẳn); không hổ phách, không đỏ. Mục tiêu nước: bộ chọn hai phần 48dp "Tự động / Tùy chỉnh", số Title Large + "ml/ngày", dòng `calculate` nêu công thức và lần cân chung với mục tiêu calo. Hai nút nhanh: cặp ô ml ("Nút nhỏ" 250, "Nút lớn" 500). Nhắc uống nước: công tắc, hàng "Khung giờ" với hai chip giờ "07:00 – 22:00", khối "Chu kỳ" với bộ chọn bốn phần 45 / 60 / 90 / 120 "phút", dòng cách nhắc và dòng nhắc tiếp ("Nhắc tiếp lúc 14:05", hoặc trung tính "Chưa nhắc được · thiếu quyền thông báo"). Nhóm Tổng kết tuần: công tắc "Thông báo sáng Thứ Hai" (điểm tính trên máy, không gọi AI), hàng "Giờ gửi" với chip 08:00 và lần gửi tới, công tắc "Tự tạo nhận xét tuần khi mở" với dòng phụ nói rõ "tốn 1 lượt model chat".
- **Bữa ăn (S44):** thẻ "Một ngày 24 giờ" (+ "bây giờ 20:15 → Tối" Label Medium on-surface-var): làn 10dp vẽ bốn khung giờ theo ngữ pháp đoạn của vòng calo (tông meal-*, đầu vuông, khe 3dp, độ dài theo phút): Phụ 00:00–04:59, Sáng, Trưa, Phụ, Tối, Phụ 22:00–23:59. Phụ xuất hiện nhiều đoạn vì giờ không thuộc bữa nào là Phụ. Vạch bây giờ 2dp on-surface; nhãn giờ Label Small; chú giải ô tông 10dp. Dưới thẻ là khối bốn hàng (ô tông 10dp, tên Title Medium, chip giờ bắt đầu – kết thúc) và hàng Phụ tự động on-surface-var "Giờ không thuộc bữa nào · 22:00 – 04:59", không sửa được. Lời dẫn đầu màn: đổi giờ chỉ áp dụng cho món ghi từ giờ; món đã ghi giữ bữa của nó. Nút chữ "Khôi phục giờ mặc định".
- **TimeInput trùng khung (S44):** chạm chip mở TimeInput của S12 trong sheet phụ ("Trưa kết thúc lúc", bên cạnh "Đang là 13:59"). Giờ đè lên bữa khác: dòng `error` 18dp đỏ "Trùng với Phụ 14:00 – 17:29. Mỗi phút chỉ thuộc một bữa.", Xong tắt (on-surface 12% nền, `aria-disabled`), kèm nút sửa một chạm tonal `edit` "Dời Phụ bắt đầu từ 14:31" (như "Có phải 74,1 kg?" của S31).
- **Dữ liệu (S45):** thẻ "Sao lưu ra file" (ô 40dp `backup`): lần xuất gần nhất và tên file, dòng nói file gồm gì và không gồm gì (API key, ảnh thu nhỏ), tonal "Xuất file" + outlined "Nhập từ file" chia đều, dòng "Nhập sẽ thay toàn bộ dữ liệu hiện tại". Mục "Trên máy": lưới chỉ số ba cột của S02 (Ngày đã ghi / Lần cân / Món đã lưu), công tắc "Lưu ảnh thu nhỏ" kèm dung lượng, dòng `cloud_done` về Auto Backup của Android (trừ API key). Mục "Xoá": một hàng điều hướng trung tính (ô surface-highest `delete_forever`) "Xoá toàn bộ dữ liệu · Về lại màn chào như lần đầu · hỏi 2 lần". Hàng không đỏ.
- **Hộp thoại xoá toàn bộ (S45):** hộp thoại xác nhận duy nhất của app (05 §5; The One Dialog Rule). 312dp, surface-low, góc 28dp, đệm 24 / 24 / 16dp, canh giữa trên màn che, không bóng; `delete_forever` 24dp on-surface-var canh giữa, tiêu đề Title Large canh giữa, thân Body Medium on-surface-var. Bước 1 "Xoá toàn bộ dữ liệu?": danh sách cái sẽ mất bằng số đếm thật (biểu tượng 18dp), dưới đường ngăn là dòng bản sao lưu gần nhất và nút chữ "Xuất bản sao lưu trước"; hành động là hai nút chữ "Huỷ" / "Tiếp tục" (chưa xoá, chưa đỏ). Bước 2 "Gõ XOÁ để xác nhận": ô OutlinedTextField "Gõ XOÁ" ("XOA" không dấu cũng được); chỉ khi khớp mới bật nút filled error "Xoá vĩnh viễn" (error / on-error, `delete_forever` 20dp, viên thuốc 48dp). Bước chỉ nằm trong `aria-label`, không có dòng "Bước 2/2".

### Named Rules
**The Ledger Rule.** Thẻ món đang chờ không bao giờ đổi "còn N kcal" trên thanh trên; chỉ khi lưu mới đổi. Làn trong thẻ nói trước điều sẽ xảy ra, thanh trên nói điều đã xảy ra. Tin nhắn nhóm theo ngày ăn, và nhãn làn theo ngày của bữa: "Hôm nay sau khi lưu" cho hôm nay; cho ngày khác, làn ghi đúng ngày "dd/mm sau khi lưu" (S33; thẻ món chat S20 dùng "Ngày này").

**The One Food Row Rule.** Một món đọc giống nhau ở mọi chỗ: tên Body Medium (Title Medium trên thẻ đầy đủ), khẩu phần, ba chấm macro 6dp, kcal canh phải với đơn vị Mực Phụ. Hàng Thêm nhanh (S13) và hàng Món của tôi (S34) là dòng thẻ xác nhận S12 thu gọn; Sửa món (S14) là thẻ S12 mở lại. Không dựng kiểu hàng món thứ hai.

**The Absolute Log Rule.** Nhật ký lưu giá trị tuyệt đối của từng lần ăn. Sửa hay xoá một món đã lưu không bao giờ đổi các lần đã ghi, và màn phải nói điều đó ngay chỗ người dùng sửa (hộp `history`, chú thích xoá). Tạo món đã lưu từ S34 không ghi nhật ký, và nút nói rõ "Lưu vào Món của tôi".

**The Spec-Sheet Row Rule.** Mỗi hàng cài đặt nói giá trị đang dùng ngay trên hàng: trên S40 bằng dòng phụ, trên S41–S45 bằng số tabular canh phải với đơn vị Label Medium on-surface-var. Không phải mở một mục chỉ để biết app đang chạy với số gì.

**The One Dialog Rule.** Hộp thoại xác nhận chỉ có ở một chỗ: Xoá toàn bộ dữ liệu (S45), hai bước, bước 2 phải gõ "XOÁ". Mọi xoá khác là vuốt hoặc nút + snackbar Hoàn tác, và "Khôi phục mặc định" cũng không hỏi lại. Nút filled error (error / on-error) chỉ có ở "Xoá vĩnh viễn".

**The Same-Days Rule.** Kỳ đang diễn ra chỉ so với cùng số ngày của kỳ trước: tuần so với đúng các thứ tương ứng của tuần trước (T2–T5 với T2–T5), tháng so với đúng khoảng ngày đó của tháng trước (01–13/11 với 01–13/10), không bao giờ với cả kỳ trước. Chỉ so khi kỳ trước đủ dữ liệu trong lịch sử của người dùng: tuần cần ≥3 ngày ghi ở các thứ tương ứng (đều sau ngày bắt đầu dùng app), tháng cần ≥7 ngày ghi trong cùng khoảng ngày; không đủ thì nói vì sao bằng một dòng `info`, không vẽ bảng. Luật áp cho S32 và cho tuần đang diễn ra của S35, nơi dòng so sánh ghi "Cùng kỳ tuần trước".

## Do's and Don'ts

### Do:
- **Do** để số ở tâm vòng calo (kcal còn lại trên S10, mục tiêu kcal/ngày trên S02, Δ có dấu của ngày đã qua trên S33) là thứ lớn nhất trên màn (Display Large 57sp), một lần duy nhất; trên S35, màn không có vòng, chỗ đó là điểm tuần trong thẻ điểm; trên S31 là cân trung bình tuần; trên S15 là tổng nước của ngày. Trên S32 là kcal TB mỗi ngày của kỳ, chỉ khi kỳ có ≥3 ngày ghi.
- **Do** bật số tabular cho mọi chữ và đặt đơn vị ở Label Medium màu on-surface-var.
- **Do** phân tầng bằng surface, surface-low, surface-cont, surface-highest thay cho bóng (trên viewfinder của Camera: #000 và lớp che #000 62%, cũng không bóng).
- **Do** báo vượt mục tiêu bằng cả chữ, biểu tượng và màu: warn khi ≤10%, error khi >10%.
- **Do** giữ đoạn vòng và ô màu thẻ bữa cùng một token meal-*, xếp theo giờ ăn.
- **Do** giữ mọi vùng chạm từ 48dp và giữ "Chụp món" cách tối đa một chạm (hàng nút hoặc FAB).
- **Do** khi tách các tông bữa ở chế độ sáng, dịch sắc độ chứ không dịch độ sáng, giữ ≥3:1 so với rãnh vòng.
- **Do** dùng hổ phách cho mọi cờ cần kiểm tra số liệu, luôn kèm biểu tượng và chữ, và để Lưu vẫn bấm được.
- **Do** vẽ tác động lên ngày bằng ngữ pháp của vòng calo: tông bữa, đầu vuông, khe 3dp, đoạn sắp thêm 40% có viền trong 1.5dp, phần vượt trên làn riêng 6dp.
- **Do** ghim nút Lưu 56dp toàn chiều rộng ở đáy sheet và ghi rõ tên bữa cùng tổng kcal.
- **Do** xoá bằng vuốt + snackbar Hoàn tác, không bằng hộp thoại xác nhận; ngoại lệ duy nhất là Xoá toàn bộ dữ liệu (S45, 05 §5).
- **Do** dùng một ngữ pháp chọn cho mọi câu hỏi một lựa chọn: viền 1dp outline-var → secondary-container + viền 2dp primary, ô biểu tượng chuyển primary.
- **Do** ghim nút bước 56dp toàn chiều rộng ở đáy và chỉ bật khi dữ liệu bước hợp lệ.
- **Do** báo mục tiêu đã bị kẹp về mức an toàn bằng hổ phách, biểu tượng `info` và giá trị thực, và vẫn cho đi tiếp.
- **Do** hiện lỗi khoảng của ô nhập khi rời ô hoặc bấm Xong; trong lúc gõ chỉ tắt nút bước.
- **Do** giữ thẻ món trong chat rộng 304dp ở mọi trạng thái, đổi chờ / đã lưu / đã bỏ qua tại chỗ, và chỉ đổi "còn N kcal" trên thanh trên khi lưu.
- **Do** để bong bóng AI tách khỏi nền bằng tầng tonal; nếu cần tách rõ hơn ở chế độ sáng (hiện ~1.1:1), thêm viền 1dp outline-var, không thêm bóng.
- **Do** tách mọi thanh vượt mục tiêu ngay tại vạch: phần trong mục tiêu, khe 3dp, phần vượt màu trạng thái (The Goal-Line Split Rule).
- **Do** vẽ ngày "Thiếu" bằng Chàm Thiếu (`--under`), và để chú giải biểu đồ kiêm luôn bảng đếm ngày.
- **Do** để mỗi cột ngày là vùng chạm 48dp mở chi tiết ngày, và vẽ ngày chưa ghi bằng ô nét đứt + "—", không bằng cột 0.
- **Do** để thẻ số liệu không bao giờ chờ AI; mọi trạng thái của thẻ AI nói rõ số liệu phía trên vẫn đủ.
- **Do** vẽ mỗi tuần trên lịch S30 như biểu đồ 7 cột S35 thu nhỏ: cột mini 14dp trên một vạch mục tiêu chung của cả hàng, cùng màu Đạt / Thiếu / Vượt.
- **Do** đánh dấu hôm nay trên lịch S30 chỉ bằng viền 1.5dp primary (vòng số ngày và cột), không tô nền, để ở chế độ tối nó không lẫn với Thiếu.
- **Do** để ngày trước khi bắt đầu dùng app là số mờ không cột; không bao giờ tính là "chưa ghi".
- **Do** đọc cân theo tuần: Display Large là TB tuần, đường đậm nối TB tuần, từng lần cân là ô 5dp on-surface-var; tuần này chỉ thành điểm tuần khi có ≥2 lần cân.
- **Do** tính đã giảm / còn lại / dự kiến theo lần cân mới nhất, ghi "(theo lần cân dd/mm)" khi nó khác TB tuần, và tính ngày dự kiến từ hôm nay dạng "~dd/mm/yyyy".
- **Do** vẽ vạch mục tiêu cân chỉ ở đúng giá trị của nó; khi nằm ngoài khung, nói "dưới khung" trong chú giải.
- **Do** đưa lỗi nhập có cách sửa rõ ràng kèm nút sửa một chạm 48dp ("Có phải 74,1 kg?"), và đổi nút lưu thành "Thay bản ghi" khi ngày đó đã có số.
- **Do** cho ngày đã qua nói kết quả ở tâm vòng (loại ngày + Δ có dấu) và dùng ô chú giải của biểu đồ tuần trong chip loại ngày.
- **Do** viết macro của ngày đã qua bằng "thiếu X g" / "đạt" / "vượt X g", và ghi nhãn làn bằng ngày ("12/11 sau khi lưu") khi thêm món cho ngày khác hôm nay.
- **Do** để hộp nghi ghi thiếu đưa thẳng tới các bữa trống bằng nút thêm nhanh có giờ giữa khung bữa.
- **Do** ép Camera (S11) về scheme tối bất kể giao diện hệ thống; viewfinder và panel dưới #000, mọi nút và chữ đè lên ảnh đặt trên lớp che #000 62%.
- **Do** đặt chip giờ ăn · bữa của S12 ngay trên nút chụp 72dp, chạm để sửa, để thấy giờ sẽ ghi trước khi bấm.
- **Do** ghi chữ trạng thái trên nút flash ("Tự động" / "Bật" / "Tắt") và ghi nhãn chữ cho hai ô 48dp Thư viện / Ghi chú.
- **Do** cho bước phân tích nói thật: các bước quan sát được, đồng hồ "Đã chờ", và Hủy luôn có.
- **Do** giữ nguyên ảnh (không làm mờ) và ghi chú khi phân tích lỗi, kèm "Thử lại", "Nhập tay" và "Món của tôi".
- **Do** lấy giờ của ảnh thư viện từ EXIF, ghi nguồn "Giờ chụp ảnh", và nêu rõ ngày và bữa sẽ ghi khi giờ đó rơi vào ngày khác.
- **Do** đổi nút xin quyền thành "Mở Cài đặt" khi người dùng đã chọn "Không hỏi lại".
- **Do** dựng Thêm nhanh (S13) như sheet S12: thanh 52dp với dòng phụ "Hôm nay · còn N kcal" primary, chip giờ ăn · bữa có nguồn bằng chữ ("Lúc bấm thêm" khi mở từ "+" của S10; giờ giữa khung với `history` "Theo bữa X" khi mở từ "+" của thẻ bữa), và chân ghim tonal "Hỏi AI" trước outlined "Nhập tay".
- **Do** để một chạm vào hàng món đã lưu mở S12 với món đó ở ×1 (`chevron_right` cuối hàng); S12 mới là chỗ lưu.
- **Do** tìm không dấu và tô phần khớp trên tên có dấu bằng primary 700 gạch chân 1.5dp; khi không khớp, đưa hai lối thoát Hỏi AI / Nhập tay điền sẵn chuỗi tìm.
- **Do** kiểm tra "Số liệu lệch" bằng đúng |kcal − (4P+4C+9F)| / (4P+4C+9F) > 20%, hiện khi rời ô macro cuối, kèm phép tính, không chặn.
- **Do** vẽ một lần sửa món lên làn ngày: đoạn cũ giữ nguyên, phần tăng là đoạn chờ, phần giảm là đoạn rỗng nét đứt 1.5dp outline, kèm dòng "Trước khi sửa N kcal · Món này ±Δ kcal".
- **Do** xoá món đã ghi bằng `delete` trên thanh của Sửa món, không hộp thoại, và Hoàn tác trên S10.
- **Do** ghim Yêu thích lên đầu Món của tôi, và nói rõ trong form tạo / sửa món đã lưu rằng nhật ký không đổi.
- **Do** vẽ một ngày nước bằng thanh làn của S12: mỗi lần uống một đoạn primary đầu vuông, khe 3dp, làn dài bằng mục tiêu, vạch 2dp; mục tiêu chỉ nói bằng chữ một lần ("của 2.600 ml"), cách tính nằm sau một chạm.
- **Do** đưa nước trên mục tiêu sang làn riêng 6dp trên rãnh primary 18%, vẫn primary, với chữ trung tính "Đạt mục tiêu · +N ml".
- **Do** tính giờ nhắc nước theo 06 §4 (max(lần uống gần nhất, lần nhắc trước) + khoảng nhắc, kẹp trong khung; ngoài khung hoặc đã đạt thì đầu khung hôm sau), và để cả hàng nhắc 48dp mở S43; ngày đã qua không có hàng nhắc.
- **Do** ghi nguồn lần uống bằng chữ ("Trong app" / "Từ thông báo" / "Từ widget") và gắn "Đạt mục tiêu · N ml" vào đúng hàng làm tổng chạm mục tiêu.
- **Do** ghim +250 / +500 / lượng khác ở chân surface-cont trong vùng ngón cái, và với ngày đã qua nói rõ trên chân rằng lần thêm ghi lúc 14:30.
- **Do** để mỗi hàng cài đặt nói giá trị đang dùng: dòng phụ trên S40, số Title Medium tabular canh phải với đơn vị Label Medium on-surface-var trên S41–S45 (The Spec-Sheet Row Rule).
- **Do** đưa mọi thay đổi chạm tới mục tiêu calo (sửa hồ sơ, đổi tốc độ, ghi đè) qua sheet Đang dùng / Mới của S31, áp dụng từ hôm nay; ngày trước vẫn so với mục tiêu cũ.
- **Do** kẹp calo ghi đè theo thứ tự 02 §5: trần thâm hụt 25% TDEE trước, rồi sàn max(1.500, BMR); hộp hổ phách nêu giới hạn và giá trị thực, không chặn; ô ngoài 1.000–5.000 kcal hay macro tổng ≠ 100% mới là lỗi đỏ.
- **Do** giữ key đã lưu ở dạng che với 4 ký tự cuối; chỉ 401 tô đỏ ô API key, mất mạng hay 429 chỉ báo bằng dòng chữ dưới nút.
- **Do** báo thiếu quyền thông báo bằng dải secondary-container trung tính với nút "Cho phép", và nói chưa cấu hình ("Để sau") bằng lời trung tính trong dòng phụ.
- **Do** vẽ khung giờ bốn bữa thành làn 24 giờ cùng ngữ pháp đoạn của vòng calo (tông meal-*, đầu vuông, khe 3dp, vạch bây giờ), và sửa khung trùng bằng nút một chạm cạnh dòng lỗi.
- **Do** dùng hộp thoại xác nhận chỉ cho Xoá toàn bộ dữ liệu: bước 1 nói mất gì bằng số thật và đưa "Xuất bản sao lưu trước", bước 2 gõ "XOÁ" mới bật nút filled error "Xoá vĩnh viễn" (The One Dialog Rule).
- **Do** ghim trên S32 chỉ hàng chọn kỳ ‹ › (chuyển surface-cont khi cuộn); bộ chọn Tuần / Tháng cuộn đi cùng thân.
- **Do** báo chênh của TB kỳ so với mục tiêu theo The Three States Rule, có chip khi vượt, và bỏ qua hôm nay khi tìm ngày nhiều nhất / ít nhất của kỳ đang diễn ra.
- **Do** vẽ biểu đồ tháng bằng cột 6dp trong khe 336/N dp cùng ngữ pháp Đạt / Thiếu / Vượt, hôm nay chỉ viền, và để cả biểu đồ là một slider: chạm hoặc kéo bắt dính một ngày, hàng đọc số cố định bên dưới với ‹ › 48dp và "Mở ngày".
- **Do** vẽ TB của kỳ bằng đường liền 1dp primary cạnh vạch mục tiêu nét đứt, kèm "TB N" trong chú giải, chỉ khi có ≥3 ngày ghi.
- **Do** so kỳ đang diễn ra với cùng số ngày của kỳ trước, chỉ khi kỳ trước đủ ngày ghi, bằng mũi tên trung tính on-surface-var và câu "Mũi tên chỉ tăng hay giảm, không phải tốt hay xấu." (The Same-Days Rule).
- **Do** xếp một tuần vào tháng chứa ngày Thứ Năm của nó, và nói điều đó bằng một dòng Body Small dưới cân của tháng.
- **Do** cho người mới trên S32 thấy ngày cụ thể từng phần sẽ có số ("Sắp có số"), suy từ chính các luật đủ dữ liệu.

### Don't:
- **Don't** dùng xanh lá làm màu chủ đạo, nền kem/serif ấm, hay giao diện số liệu nền tối kiểu mono: người dùng đã loại cả ba. Camera ép tối (S11) không phải hướng đó: nó vẫn là scheme tối chàm của app, chỉ khoá ở một màn.
- **Don't** dùng màu đạm / tinh bột / béo cho thương hiệu, trạng thái hay bữa ăn.
- **Don't** đặt bóng đổ lên thẻ, ô, nút hay sheet; chỉ FAB và snackbar có bóng.
- **Don't** báo trạng thái chỉ bằng màu.
- **Don't** thêm trang trí không mang dữ liệu; độ tinh xảo đến từ độ chính xác số liệu.
- **Don't** dùng đỏ cho cờ kiểm tra số liệu hay cho điều chỉnh an toàn; đỏ chỉ là vượt >10%, macro vượt mục tiêu, lỗi nhập của ô nhập, lỗi gửi tin nhắn và nút "Xoá vĩnh viễn" (S45).
- **Don't** đánh dấu đoạn sắp thêm bằng sọc hay vạch chéo; dùng tông nhạt có viền.
- **Don't** tô nền hay viền cho ô khẩu phần chưa chọn; chỉ ô đang chọn có viên thuốc.
- **Don't** dùng `menu_book` cho lưu món hay `bookmark_*` cho thêm từ Món của tôi.
- **Don't** thêm dấu tick hay radio tròn vào thẻ chọn, và không tự sang bước khi chọn.
- **Don't** tô thang BMI theo màu phân loại; chỉ đoạn hiện tại là primary.
- **Don't** làm mờ thẻ món đã bỏ qua bằng opacity; đổi sang on-surface-var và giữ nút "Hoàn tác" 48dp.
- **Don't** tô ngày "Thiếu" bằng hổ phách hay đỏ; hổ phách trên biểu đồ tuần chỉ là cờ nghi ghi thiếu.
- **Don't** lồng hộp trong thẻ nhận xét AI; phân mục bằng tiêu đề, khoảng trắng và đường ngăn.
- **Don't** dựng thẻ cho tuần tương lai hay tuần trước khi dùng app; chúng chỉ là một hàng số ngày mờ 40dp.
- **Don't** tô thay đổi cân bằng xanh, đỏ hay hổ phách, dù tăng hay giảm; thay đổi cân là chữ trung tính.
- **Don't** đặt một lần cân lẻ vào chỗ Display Large, và không vẽ đường xu hướng giả khi chưa có 2 tuần có số.
- **Don't** kéo vạch mục tiêu cân vào mép khung khi giá trị thật nằm ngoài khung.
- **Don't** viết "còn" cho ngày đã qua, ở tâm vòng hay dưới macro.
- **Don't** dùng hổ phách cho chip "Thiếu" trên S33; hổ phách chỉ là cờ `warning` và hộp nghi ghi thiếu.
- **Don't** lồng hộp cho "dữ liệu đã đổi" trong thẻ tuần S33; đó là một hàng có đường ngăn.
- **Don't** dùng hổ phách cho thiếu sáng trên Camera; đó là gợi ý trung tính `brightness_low`, không phải "cần kiểm tra".
- **Don't** hiện phần trăm hay thanh tiến độ giả khi chờ AI; chỉ các bước thật và thời gian đã chờ.
- **Don't** mang #000 hay lớp che 62% của Camera sang màn khác; chúng là ngoại lệ của scrim, không phải màu bảng màu.
- **Don't** đặt bóng cho nút hay vòng căn đĩa trên viewfinder; tương phản đến từ lớp che và viền 1.5dp #000 60%.
- **Don't** cho chọn nhiều ảnh trong một lần chụp; một lần chụp là một ảnh.
- **Don't** thêm món ngay khi chạm một hàng của Thêm nhanh; hàng chỉ mở S12, không có nút thêm tức thì.
- **Don't** dùng nét đứt cho đoạn chờ, hay tô màu phần bị bỏ khi sửa giảm; nét đứt chỉ là thứ không có trong sổ.
- **Don't** để sửa hay xoá một món đã lưu đổi các lần đã ghi; nhật ký giữ giá trị tuyệt đối (xoá chỉ đưa `my_food_id` về null).
- **Don't** gọi nút "Tiếp" của Nhập tay là lưu, và không để FAB "Thêm món" của S34 ghi vào nhật ký.
- **Don't** thêm token cho ô tìm; nó là surface-highest ở cả hai chế độ.
- **Don't** tô nước uống quá mục tiêu bằng hổ phách hay đỏ, hay viết "vượt"; nước không có trạng thái vượt (08 §2), từ mục tiêu trở lên là Đạt.
- **Don't** dùng bút chì cho "Thêm lượng khác"; bút chì dành cho "Sửa", lượng khác là `add`.
- **Don't** tô "Xoá" đỏ trong sheet sửa lần uống; đỏ của xoá chỉ là nền vuốt xoá và nút filled error "Xoá vĩnh viễn" ở bước 2 xoá toàn bộ (S45).
- **Don't** thêm FAB trên S15, và không lồng hộp cho hàng nhắc (kể cả khi thiếu quyền); đó là một hàng có đường ngăn của thẻ tổng ngày.
- **Don't** đặt một lần uống lẻ vào chỗ Display Large; từng lần uống là Body Large on-surface-var.
- **Don't** dùng hộp thoại xác nhận ở chỗ nào khác ngoài Xoá toàn bộ dữ liệu (S45); "Khôi phục mặc định" dùng snackbar Hoàn tác.
- **Don't** dùng on-error hay nút filled error ở đâu khác ngoài "Xoá vĩnh viễn"; hàng "Xoá toàn bộ dữ liệu" và bước 1 của hộp thoại không đỏ.
- **Don't** tô đỏ hay hổ phách cho trạng thái chưa cấu hình (chưa có key, nhắc tắt, chưa sao lưu) hay cho dải thiếu quyền thông báo.
- **Don't** áp dụng thay đổi calo thẳng từ một hàng cài đặt, và không để đổi giờ bữa ghi lại bữa của món đã ghi.
- **Don't** ngắt dòng giữa tên bữa và giờ trong dòng phụ Bữa ăn của S40.
- **Don't** đặt điểm, xếp loại, chữ AI hay top món lên S32; điểm chỉ ở dòng phụ của hàng "Xem tổng kết tuần".
- **Don't** biến từng cột 6dp của biểu đồ tháng thành nút riêng, hay đặt ô đọc số nổi dưới ngón tay; hàng đọc số nằm cố định dưới biểu đồ.
- **Don't** viết dấu kép trong hàng đọc số ("Thiếu −940"); chữ đã nói chiều: "Thiếu 940", "Vượt 660".
- **Don't** so một tuần hay tháng đang diễn ra với cả kỳ trước, và không tô màu mũi tên so sánh.
- **Don't** hiện Display Large, vạch TB hay bảng so sánh khi chưa đủ dữ liệu; người mới có ghi chú `flag` và "Sắp có số".
- **Don't** vẽ nước của S32 thành cột theo ngày; đó là một làn TB một đoạn.
