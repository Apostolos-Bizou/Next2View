import { createI18n } from 'vue-i18n'
import el from './el.json'
import en from './en.json'

const savedLocale = typeof window !== 'undefined' ? localStorage.getItem('n2v_locale') : null

const i18n = createI18n({
  legacy: false,
  locale: savedLocale || 'el',
  fallbackLocale: 'el',
  messages: { el, en }
})

export default i18n
